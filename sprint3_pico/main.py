from machine import Pin, time_pulse_us
import machine
import network
import time
import ubinascii
import secretsHomeAri
# import secretsHotspotAri
from umqtt_simple import MQTTClient

# ===== CREDENZIALI WIFI =====
SSID     = secretsHomeAri.secrets['ssid']
PASSWORD = secretsHomeAri.secrets['pw']
# SSID     = secretsHotspotAri.secrets['ssid']
# PASSWORD = secretsHotspotAri.secrets['pw']

# ===== MQTT =====
MQTT_BROKER = "192.168.1.7"   # IP del PC col broker: DEVE coincidere con quello nel .qak
MQTT_PORT   = 1883
# client id univoco per scheda: evita che due client con lo stesso id si "buttino fuori" a vicenda
CLIENT_ID   = b"picosonar_s3_" + ubinascii.hexlify(machine.unique_id())
KEEPALIVE   = 30              # secondi
PING_EVERY  = 10000           # ms: ping al broker anche quando non ci sono letture valide

TOPIC_DISTANCE = b"mqttpicoTF26"
TOPIC_LED      = b"mqttledTF26"

# ===== SONAR =====
TRIG = Pin(3, Pin.OUT)
ECHO = Pin(2, Pin.IN)
SAMPLE_EVERY  = 200           # ms tra una misura e l'altra
PUBLISH_EVERY = 1000          # ms tra una pubblicazione e l'altra (mediana delle misure)
# Valore inviato se NESSUN eco valido nell'intervallo (sensore scollegato / puntato nel vuoto).
# Essendo > DFREE, il .qak lo interpreta come possibile guasto (dopo 3 s).
# Metti None per tornare al vecchio comportamento (nessun messaggio).
NO_ECHO_CM = 400

led = Pin("LED", Pin.OUT)
is_blinking = False
led_state = False
last_blink_time = 0

wlan = network.WLAN(network.STA_IF)
client = None


# ---------------------------------------------------------------- WIFI
def connect_wifi(timeout_s=20):
    """Ritorna True/False, non solleva eccezioni."""
    wlan.active(True)
    wlan.config(pm=0xa11140)          # disattiva il risparmio energetico del chip WiFi
    if wlan.isconnected():
        return True
    try:
        wlan.disconnect()
    except Exception:
        pass
    wlan.connect(SSID, PASSWORD)
    print("[WIFI] Connessione a '%s'..." % SSID)
    while not wlan.isconnected() and timeout_s > 0:
        time.sleep(1)
        timeout_s -= 1
    if wlan.isconnected():
        print("[WIFI] Connesso, IP Pico:", wlan.ifconfig()[0])
        return True
    print("[WIFI ERRORE] Connessione fallita")
    return False


# ---------------------------------------------------------------- MQTT
def on_mqtt_message(topic, msg):
    global is_blinking
    print("[MQTT RX]", topic, msg)
    try:
        payload = msg.decode().strip()
        if "true" in payload:
            is_blinking = True
        elif "false" in payload:
            is_blinking = False
            led.off()
    except Exception as e:
        print("Errore comando LED:", e)


def close_client():
    global client
    if client is not None:
        try:
            client.disconnect()
        except Exception:
            try:
                client.sock.close()
            except Exception:
                pass
    client = None


def connect_mqtt():
    global client
    close_client()                    # chiude il vecchio socket (evita perdite di memoria/socket)
    c = MQTTClient(client_id=CLIENT_ID, server=MQTT_BROKER,
                   port=MQTT_PORT, keepalive=KEEPALIVE)
    c.set_callback(on_mqtt_message)
    c.connect()
    c.subscribe(TOPIC_LED)
    client = c
    print("[MQTT] Connesso a %s, sottoscritto a %s" % (MQTT_BROKER, TOPIC_LED.decode()))
    for _ in range(3):                # feedback visivo: connessione MQTT riuscita
        led.toggle()
        time.sleep(0.15)
    led.off()


# ---------------------------------------------------------------- SONAR
def misura_distanza():
    TRIG.low()
    time.sleep_us(2)
    TRIG.high()
    time.sleep_us(10)
    TRIG.low()
    durata = time_pulse_us(ECHO, 1, 30000)
    if durata < 0:
        return None
    return (durata / 2) / 29.1


def mediana(valori):
    s = sorted(valori)
    return s[len(s) // 2]


# ---------------------------------------------------------------- AVVIO
while not connect_wifi():
    time.sleep(2)

samples = []
last_sample = 0
last_publish = 0
last_ping = 0
print("\n--- PICO W: avvio loop ---")

while True:
    try:
        if not wlan.isconnected():
            raise OSError("WiFi caduto")
        if client is None:
            connect_mqtt()
            last_ping = time.ticks_ms()

        now = time.ticks_ms()

        # 1. comandi in arrivo
        client.check_msg()

        # 2. lampeggio non bloccante
        if is_blinking and time.ticks_diff(now, last_blink_time) > 500:
            led_state = not led_state
            led.value(1 if led_state else 0)
            last_blink_time = now

        # 3. campionamento (ogni 200 ms)
        if time.ticks_diff(now, last_sample) >= SAMPLE_EVERY:
            last_sample = now
            d = misura_distanza()
            if d is not None:
                samples.append(d)

        # 4. pubblicazione (ogni 1 s): mediana delle misure -> meno rumore
        if time.ticks_diff(now, last_publish) >= PUBLISH_EVERY:
            last_publish = now
            if samples:
                d_int = round(mediana(samples))
            elif NO_ECHO_CM is not None:
                d_int = NO_ECHO_CM
            else:
                d_int = None
            samples = []
            if d_int is not None:
                msg = "msg(sonarreading,event,picow,none,sonarreading(%d),0)" % d_int
                client.publish(TOPIC_DISTANCE, msg.encode())
                last_ping = now       # una publish vale come attività verso il broker
                print("TX", d_int)

        # 5. keepalive esplicito (serve anche se non si pubblica)
        if time.ticks_diff(now, last_ping) >= PING_EVERY:
            client.ping()
            last_ping = now

        time.sleep_ms(10)

    except Exception as e:
        # stato incerto: LED spento; lo stato giusto verrà reinviato dal .qak
        is_blinking = False
        led.off()
        print("[ERRORE LOOP]", e, "-> riconnessione")
        close_client()
        time.sleep(2)
        if not wlan.isconnected():
            connect_wifi()