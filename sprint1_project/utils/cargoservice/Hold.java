package cargoservice;

public class Hold {
    public enum SlotState {
        FREE,
        RESERVED,
        OCCUPIED
    }

    public static final int NUM_MAIN_SLOTS = 4;
    public static final int NO_SLOT_AVAILABLE = -1;

    private final SlotState[] mainSlots = new SlotState[NUM_MAIN_SLOTS];
    private SlotState slot5 = SlotState.FREE;

    public Hold() {
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            mainSlots[i] = SlotState.FREE;
        }
    }

    // Riserva il primo slot libero [1,4] o restituisce -1 se la stiva è piena
    public synchronized int reserveNextFreeSlot() {
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            if (mainSlots[i] == SlotState.FREE) {
                mainSlots[i] = SlotState.RESERVED;
                return i + 1;
            }
        }
        return NO_SLOT_AVAILABLE;
    }

    // Occupa uno slot 
    public synchronized void setSlotOccupied(int slotNumber) {
        if (isValidSlot(slotNumber)) {
            mainSlots[slotNumber - 1] = SlotState.OCCUPIED;
        }
    }

    // Libera uno slot
    public synchronized void releaseSlot(int slotNumber) {
        if (isValidSlot(slotNumber)) {
            mainSlots[slotNumber - 1] = SlotState.FREE;
        }
    }

    // Controlla se la stiva è piena (nessuno slot in stato FREE)
    public synchronized boolean isFull() {
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            if (mainSlots[i] == SlotState.FREE) {
                return false;
            }
        }
        return true;
    }

    // Restituisce lo stato di uno slot principale (1-4)
    public synchronized SlotState getSlotState(int slotNumber) {
        if (isValidSlot(slotNumber)) {
            return mainSlots[slotNumber - 1];
        }
        return null;
    }

    // Restituisce lo stato dello slot temporaneo 5
    public synchronized SlotState getSlot5State() {
        return slot5;
    }

    // Imposta lo stato dello slot temporaneo 5 
    public synchronized void setSlot5State(SlotState state) {
        this.slot5 = state;
    }

    // Controlla che il numero dello slot sia compreso tra 1 e 4
    private boolean isValidSlot(int slotNumber) {
        return slotNumber >= 1 && slotNumber <= NUM_MAIN_SLOTS;
    }
    
    // Ritorna una stringa formattata per i messaggi QAk (es. "FREE-RESERVED-FREE-FREE")
    public String getHoldStateString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            sb.append(mainSlots[i]);
            if (i < NUM_MAIN_SLOTS - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString(); // Restituisce es: "[RESERVED, FREE, FREE, FREE]"
    }

    // Restituisce la rappresentazione estesa per log e Display
    public synchronized String getStatusString() {
        StringBuilder sb = new StringBuilder("HOLD_STATUS:");
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            sb.append(String.format("[%d:%s]", i + 1, mainSlots[i]));
        }
        sb.append(String.format("|[5:%s]", slot5));
        return sb.toString();
    }
}