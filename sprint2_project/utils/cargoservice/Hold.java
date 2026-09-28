package cargoservice;

import java.util.HashMap;
import java.util.Map;

public class Hold {
    public static final int MAX_SLOTS = 4;

    // Struttura ausiliaria per coordinate cartesiane
    public record Position(int x, int y) {}

    // Stato di ciascun slot
    public enum SlotStatus { FREE, RESERVED, OCCUPIED }

    // Mappa topologica immutabile delle posizioni fisse sulla griglia
    private final Position homePos = new Position(0, 0);
    private final Position ioPortPos = new Position(0, 4);
    private final Position slot5Pos = new Position(4, 2);

    private final Map slotCoordinates = new HashMap<>();
    private final Map slotStates = new HashMap<>();

    public Hold() {
        // Inizializzazione coordinate cartesiane dei 4 slot della stiva
        slotCoordinates.put(1, new Position(1, 1));
        slotCoordinates.put(2, new Position(3, 1));
        slotCoordinates.put(3, new Position(1, 3));
        slotCoordinates.put(4, new Position(3, 3));

        // Inizializzazione degli stati a FREE
        for (int i = 1; i <= MAX_SLOTS; i++) {
            slotStates.put(i, SlotStatus.FREE);
        }
    }

    // --- Metodi di accesso alle posizioni fisse (Read-Only) ---
    public Position getHomePosition() { return homePos; }
    public Position getIOPortPosition() { return ioPortPos; }
    public Position getSlot5Position() { return slot5Pos; }

    public int getSlotX(int slotId) {
        Position pos = slotCoordinates.get(slotId);
        return pos != null ? pos.x() : -1;
    }

    public int getSlotY(int slotId) {
        Position pos = slotCoordinates.get(slotId);
        return pos != null ? pos.y() : -1;
    }

    // --- Metodi di manipolazione di stato (usati da cargoservice) ---
    public synchronized boolean isFull() {
        return slotStates.values().stream().noneMatch(s -> s == SlotStatus.FREE);
    }

    public synchronized int reserveFirstFree() {
        for (int i = 1; i <= MAX_SLOTS; i++) {
            if (slotStates.get(i) == SlotStatus.FREE) {
                slotStates.put(i, SlotStatus.RESERVED);
                return i;
            }
        }
        return -1; // Stiva piena
    }

    public synchronized void releaseSlot(int slotId) {
        if (slotStates.containsKey(slotId) && slotStates.get(slotId) == SlotStatus.RESERVED) {
            slotStates.put(slotId, SlotStatus.FREE);
        }
    }

    public synchronized void setSlotOccupied(int slotId) {
        if (slotStates.containsKey(slotId)) {
            slotStates.put(slotId, SlotStatus.OCCUPIED);
        }
    }

    public synchronized String displayStatus() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= MAX_SLOTS; i++) {
            if (i > 1) sb.append(",");
            sb.append(i).append(":").append(slotStates.get(i));
        }
        return sb.toString();
    }
}