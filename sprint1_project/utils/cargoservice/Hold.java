package cargoservice;

import cargoservice.IHold.SlotState;

public class Hold implements IHold{

    private final SlotState[] mainSlots = new SlotState[NUM_MAIN_SLOTS];
    /*
     * INGRESSI SCELTI:
     * - slot1 = (0,2)
     * - slot2 = (0,3)
     * - slot3 = (2,2)
     * - slot4 = (2,3)
     */
    private final int[] slotEntranceX = {0,0,2,2};
    private final int[] slotEntranceY = {2,3,2,3};

    public Hold() {
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            mainSlots[i] = SlotState.FREE;
        }
    }

    public synchronized int reserveNextFreeSlot() {
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            if (mainSlots[i] == SlotState.FREE) {
                mainSlots[i] = SlotState.RESERVED;
                return i + 1;
            }
        }
        return NO_SLOT_AVAILABLE;
    }

    public synchronized void setSlotOccupied(int slotNumber) {
        if (isValidSlot(slotNumber)) {
            mainSlots[slotNumber - 1] = SlotState.OCCUPIED;
        }
    }

    public synchronized void releaseSlot(int slotNumber) {
        if (isValidSlot(slotNumber)) {
            mainSlots[slotNumber - 1] = SlotState.FREE;
        }
    }

    public synchronized boolean isFull() {
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            if (mainSlots[i] == SlotState.FREE) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidSlot(int slotNumber) {
        return slotNumber >= 1 && slotNumber <= NUM_MAIN_SLOTS;
    }
    
    public synchronized int getEntranceX(int slotNumber) {
    	if (isValidSlot(slotNumber)) {
    		return slotEntranceX[slotNumber-1];
        }
        return -1;
    }
    
    public synchronized int getEntranceY(int slotNumber) {
    	if (isValidSlot(slotNumber)) {
    		return slotEntranceY[slotNumber-1];
        }
        return -1;
    }
    
    public synchronized String getHoldStateString() {
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
    
    public SlotState getSlotState(int slotNumber) {
    	if (isValidSlot(slotNumber)) {
    		return mainSlots[slotNumber-1];
        }
        return null;
    }
}