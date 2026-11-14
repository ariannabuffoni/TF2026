package sonar;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import it.unibo.kactor.ActorBasic;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.msg.ApplMessage;
import unibo.basicomm23.utils.CommUtils;

/**
 * Collegamento MQTT con il Pico W, indipendente dai client MQTT del framework qak.
 * Sul filo viaggiano messaggi IApplMessage:  msg(MSGID,MSGTYPE,SENDER,RECEIVER,CONTENT,SEQNUM)
 *
 *  - Pico -> qak : EVENTO   msg(sonarreading,event,picow,none,sonarreading(D),N)   su picosonarTF26
 *                  L'adapter lo valida e lo traduce in un DISPATCH locale
 *                  sonarreading(D) verso l'attore owner (il sonar).
 *  - qak -> Pico : DISPATCH msg(blinkLed,dispatch,sonar,picow,blinkLed(true|false),N) su mqttledTF26
 *                  (QoS 1, retained: il Pico lo riceve anche dopo una riconnessione).
 *
 * Si riconnette e si risottoscrive da solo; un messaggio strano viene ignorato
 * e non chiude mai la connessione.
 */
public class PicoMqttAdapter implements MqttCallback {

    private static final String PICO_NAME = "picow";

    private static final Pattern READING =
            Pattern.compile("sonarreading\\s*\\(\\s*(-?\\d+(?:\\.\\d+)?)\\s*\\)");

    private final ActorBasic owner;
    private final String sonarTopic;
    private final String ledTopic;
    private final MqttClient client;
    private final MqttConnectOptions opts;

    private volatile boolean subscribed = false;
    private volatile String lastLed = null;

    public PicoMqttAdapter(ActorBasic owner, String brokerUrl,
                           String sonarTopic, String ledTopic) throws Exception {
        this.owner = owner;
        this.sonarTopic = sonarTopic;
        this.ledTopic = ledTopic;

        // clientId univoco: due istanze non si buttano fuori a vicenda
        String id = "qak_pico_" + Long.toHexString(System.nanoTime());
        this.client = new MqttClient(brokerUrl, id, new MemoryPersistence());
        this.client.setCallback(this);

        this.opts = new MqttConnectOptions();
        this.opts.setCleanSession(true);
        this.opts.setKeepAliveInterval(30);
        this.opts.setConnectionTimeout(5);
        this.opts.setAutomaticReconnect(false);   // la riconnessione la gestisce il thread sotto

        Thread t = new Thread(this::connectionLoop, "PicoMqttAdapter");
        t.setDaemon(true);
        t.start();
    }

    // ---------------------------------------------------------------- connessione
    private void connectionLoop() {
        while (true) {
            try {
                if (!client.isConnected()) {
                    subscribed = false;
                    client.connect(opts);
                    System.out.println("[PicoMqttAdapter] connesso al broker");
                }
                if (client.isConnected() && !subscribed) {
                    client.subscribe(sonarTopic, 0);
                    subscribed = true;
                    System.out.println("[PicoMqttAdapter] sottoscritto a " + sonarTopic);
                    String led = lastLed;
                    if (led != null) publishNow(led);   // riallinea il LED dopo (ri)connessione
                }
            } catch (Throwable e) {
                subscribed = false;
                System.out.println("[PicoMqttAdapter] connessione/sottoscrizione fallita: " + e);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                return;
            }
        }
    }

    // ---------------------------------------------------------------- qak -> Pico
    /** flag = "true" | "false" */
    public void publishLed(String flag) {
        String f = flag == null ? "false" : flag.replace("'", "").trim();
        lastLed = f;
        if (client.isConnected()) publishNow(f);
        else System.out.println("[PicoMqttAdapter] non connesso: LED=" + f + " verra' inviato alla riconnessione");
    }

    private void publishNow(String flag) {
        try {
            // dispatch IApplMessage: msg(blinkLed,dispatch,sonar,picow,blinkLed(true),N)
            IApplMessage cmd = CommUtils.buildDispatch(
                    owner.getName(), "blinkLed", "blinkLed(" + flag + ")", PICO_NAME);
            MqttMessage m = new MqttMessage(cmd.toString().getBytes(StandardCharsets.UTF_8));
            m.setQos(1);
            m.setRetained(true);
            client.publish(ledTopic, m);
        } catch (Throwable e) {
            System.out.println("[PicoMqttAdapter] publish LED fallita: " + e);
        }
    }

    // ---------------------------------------------------------------- Pico -> qak
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            String value = extractReading(payload);
            if (value != null) {
                IApplMessage msg = CommUtils.buildDispatch(
                        PICO_NAME, "sonarreading", "sonarreading(" + value + ")", owner.getName());
                owner.sendMsgToMyself(msg);
            }
        } catch (Throwable e) {
            // MAI rilanciare: farebbe chiudere la connessione al client Paho
            System.out.println("[PicoMqttAdapter] errore su messaggio ignorato: " + e);
        }
    }

    /** Valida il messaggio come IApplMessage (evento sonarreading) e ne estrae la distanza. */
    private String extractReading(String payload) {
        String content = payload;
        try {
            IApplMessage m = new ApplMessage(payload);
            if (!"sonarreading".equals(m.msgId()) || !"event".equals(m.msgType())) {
                System.out.println("[PicoMqttAdapter] messaggio ignorato (non e' l'evento sonarreading): " + payload);
                return null;
            }
            content = m.msgContent();
        } catch (Throwable e) {
            // parser non riuscito: ripiego sulla regex, cosi' un messaggio valido non va perso
            System.out.println("[PicoMqttAdapter] parsing IApplMessage fallito (" + e + "), uso il ripiego");
        }
        Matcher mt = READING.matcher(content);
        return mt.find() ? mt.group(1) : null;
    }

    @Override
    public void connectionLost(Throwable cause) {
        subscribed = false;
        System.out.println("[PicoMqttAdapter] connessione persa: " + cause);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) { }
}