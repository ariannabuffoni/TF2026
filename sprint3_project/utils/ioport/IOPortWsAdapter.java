package ioport;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import it.unibo.kactor.ActorBasic;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.msg.ApplMessage;
import unibo.basicomm23.utils.CommUtils;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IOPortWsAdapter {
    private final ActorBasic ownerActor;
    private final ConcurrentLinkedQueue<WsContext> connectedClients = new ConcurrentLinkedQueue<>();
    private volatile String lastHoldMsg    = null;
    private volatile String lastWorkingMsg = null;
    private volatile String lastSensorMsg  = null;
    
    public IOPortWsAdapter(ActorBasic ownerActor, int port) {
        this.ownerActor = ownerActor;

        Javalin app = Javalin.create(config -> {
        	config.staticFiles.add("utils/gui", Location.EXTERNAL);
            
            config.jetty.wsFactoryConfig(wsFactory -> {
                wsFactory.setIdleTimeout(Duration.ZERO);
            });
        }).start(port);

        app.ws("/ws/ioport", ws -> {
        	ws.onConnect(ctx -> {
                connectedClients.add(ctx);
                System.out.println("GUI Client connesso via WebSocket!");
                if (lastWorkingMsg != null) ctx.send(lastWorkingMsg);
                if (lastHoldMsg    != null) ctx.send(lastHoldMsg);
                if (lastSensorMsg  != null) ctx.send(lastSensorMsg);
            });

            ws.onClose(ctx -> {
                connectedClients.remove(ctx);
                System.out.println("GUI Client disconnesso.");
            });

            ws.onMessage(ctx -> {
                String rawMsg = ctx.message();
                System.out.println("ricevuto messaggio: " + rawMsg);
                try {
                    IApplMessage msg = new ApplMessage(rawMsg);
                    ownerActor.sendMsgToMyself(msg);
                } catch (Exception e) {
                    System.err.println("Errore parsing IApplMessage: " + e.getMessage());
                }
            });
        });
    }

    public void updateHoldStatus(String state, String hold, String msg) {
        String cleanState = state != null ? state.replace("'", "") : "IDLE";
        String cleanHold  = hold != null ? hold.replace("'", "") : "[]";
        String cleanMsg   = msg != null ? msg.replace("'", "") : "";

        String content = String.format("hold_info('%s', '%s', '%s')", cleanState, cleanHold, cleanMsg);
        IApplMessage applMsg = CommUtils.buildDispatch("ioport", "hold_status", content, "webgui");
        lastHoldMsg = applMsg.toString();
        broadcast(lastHoldMsg);
    }

    public void updateWorkingState(String workingState) {
        String content = String.format("'%s'", workingState);
        IApplMessage applMsg = CommUtils.buildDispatch("ioport", "working_state", content, "webgui");
        lastWorkingMsg = applMsg.toString();
        broadcast(lastWorkingMsg);
    }

    public void updateSensorState(boolean occupied) {
        String content = String.format("updateioport(%b)", occupied);
        IApplMessage applMsg = CommUtils.buildDispatch("ioport", "updateioport", content, "webgui");
        lastSensorMsg = applMsg.toString();
        broadcast(lastSensorMsg);
    }
    
    public void broadcast(String message) {
        for (WsContext client : connectedClients) {
            if (client.session.isOpen()) {
                client.send(message);
            }
        }
    }
}