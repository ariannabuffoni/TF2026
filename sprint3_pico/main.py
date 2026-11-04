from machine import Pin, time_pulse_us
import network
import time
import secretsHomeAri
from umqtt_simple import MQTTClient

# ===== CREDENZIALI WIFI =====
SSID     = secretsHomeAri.secrets['ssid']
PASSWORD = secretsHomeAri.secrets['pw']

# ===== CONFIGURAZIONE MQTT E TOPIC SPRINT 3 =====
MQTT_BROKER = "192.168.1.6"  
MQTT_PORT   = 1883
CLIENT_ID   = "picosonar_sprint3"

TOPIC_DISTANCE = b"mqttpicoTF26"
TOPIC_LED      = b"mqttledTF26"

# LED integrato del Pico W
led = Pin("LED", Pin.OUT)

# Variabili di stato per il lampeggio non bloccante
is_blinking = False
last_blink_time = 0
led_state = False

# ===== CONFIGURAZIONE SONAR HC-SR04 =====
TRIG = Pin(3, Pin.OUT)  # Pin GP3
ECHO = Pin(2, Pin.IN)   # Pin GP2

def connect_wifi():
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    wlan.connect(SSID, PASSWORD)
    print(f"[WIFI] Connessione a '{SSID}' in corso...")
    
    timeout = 20
    while not wlan.isconnected() and timeout > 0:
        print(".", end="")
        time.sleep(1)
        timeout -= 1
    print()
    
    if wlan.isconnected():
        print(f"[WIFI] Connesso! IP del Pico W: {wlan.ifconfig()[0]}")
    else:
        print("[WIFI ERRORE] Impossibile connettersi.")

def on_mqtt_message(topic, msg):
    global is_blinking
    print(f"[MQTT RICEVUTO] Topic: {topic.decode()} | Messaggio: {msg.decode()}")
    try:
        payload = msg.decode()
        # Se riceve l'ordine di accendere/lampeggiare
        if "blinkLed(true)" in payload:
            is_blinking = True
            print("-> LED Fisico: INIZIO LAMPEGGIO")
        # Se riceve l'ordine di spegnere
        elif "blinkLed(false)" in payload or "false" in payload:
            is_blinking = False
            led.off()
            print("-> LED Fisico: SPENTO")
    except Exception as e:
        print("Errore nella gestione del comando LED:", e)
        
def connect_mqtt():
    client = MQTTClient(
        client_id = CLIENT_ID,
        server    = MQTT_BROKER,
        port      = MQTT_PORT,
        keepalive = 60
    )
    client.set_callback(on_mqtt_message)
    client.connect()
    client.subscribe(TOPIC_LED)
    print(f"[MQTT] Connesso al broker e sottoscritto a: {TOPIC_LED.decode()}")
    return client

def misura_distanza():
    TRIG.low()
    time.sleep_us(2)
    TRIG.high()
    time.sleep_us(10)
    TRIG.low()
    durata = time_pulse_us(ECHO, 1, 30000)  # Timeout di 30ms
    if durata < 0:
        return None
    return (durata / 2) / 29.1

# ===== AVVIO SISTEMA =====
connect_wifi()
client = connect_mqtt()

# Lampeggio rapido di avvio avvenuto con successo
for _ in range(3):
    led.toggle()
    time.sleep(0.15)
led.off()

print("\n--- PICO W OPERATIVO: Invio dati sonar e ascolto LED attivi ---")

last_sonar_time = 0
SONAR_INTERVAL = 500

while True:
    try:
        # 1. Controlla continuamente i messaggi MQTT in arrivo (comandi LED)
        client.check_msg()
        
        # 2. Gestione non bloccante del lampeggio del LED
        current_time = time.ticks_ms()
        if is_blinking:
            if time.ticks_diff(current_time, last_blink_time) > 500:
                led_state = not led_state
                if led_state:
                    led.on()
                else:
                    led.off()
                last_blink_time = current_time
        
        # 3. Invio non bloccante delle letture del sonar basato su intervallo temporale
        if time.ticks_diff(current_time, last_sonar_time) > SONAR_INTERVAL:
            d = misura_distanza()
            if d is not None:
                d_int = round(d)
                value = "sonarreading(%d)" % d_int
                msg = "msg(sonarreading,event,picow,none,%s,0)" % value
                client.publish(TOPIC_DISTANCE, msg.encode())
                print(f"Pubblicato -> {TOPIC_DISTANCE.decode()}: {msg}")
            last_sonar_time = current_time
            
        # Un piccolissimo respiro per non sovraccagionare la CPU del Pico W (10ms)
        time.sleep_ms(10)
        
    except Exception as e:
        is_blinking = False   # stato incerto: LED spento
        led.off()
        print(f"[ERRORE LOOP] {e}. Tentativo di riconnessione...")
        time.sleep(3)
        try:
            client = connect_mqtt()
        except:
            pass
