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
    private final int[] homePos = {0,0};
    private final int[] ioportPos = {4,0};
    private final int[] markerPos = {2,5};
    private static final int X = 0;
    private static final int Y = 1;    

    public Hold() {
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            mainSlots[i] = SlotState.FREE;
        }
    }

    @Override
    public synchronized int reserveNextFreeSlot() {
        for (int i = 0; i < NUM_MAIN_SLOTS; i++) {
            if (mainSlots[i] == SlotState.FREE) {
                mainSlots[i] = SlotState.RESERVED;
                return i + 1;
            }
        }
        return NO_SLOT_AVAILABLE;
    }

    @Override
    public synchronized void setSlotOccupied(int slotNumber) {
        if (isValidSlot(slotNumber)) {
            mainSlots[slotNumber - 1] = SlotState.OCCUPIED;
        }
    }

    @Override
    public synchronized void releaseSlot(int slotNumber) {
        if (isValidSlot(slotNumber)) {
            mainSlots[slotNumber - 1] = SlotState.FREE;
        }
    }

    @Override
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
    
    @Override
    public synchronized int getEntranceX(int slotNumber) {
    	if (isValidSlot(slotNumber)) {
    		return slotEntranceX[slotNumber-1];
        }
        return -1;
    }
    
    @Override
    public synchronized int getEntranceY(int slotNumber) {
    	if (isValidSlot(slotNumber)) {
    		return slotEntranceY[slotNumber-1];
        }
        return -1;
    }
    
    @Override
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
    
    @Override
    public SlotState getSlotState(int slotNumber) {
    	if (isValidSlot(slotNumber)) {
    		return mainSlots[slotNumber-1];
        }
        return null;
    }

	@Override
	public synchronized int getHomeX() {
		return homePos[X];
	}

	@Override
	public synchronized int getHomeY() {
		return homePos[Y];
	}

	@Override
	public synchronized int getIOPortX() {
		return ioportPos[X];
	}

	@Override
	public synchronized int getIOPortY() {
		return ioportPos[Y];
	}

	@Override
	public synchronized int getMarkerX() {
		return markerPos[X];
	}

	@Override
	public synchronized int getMarkerY() {
		return markerPos[Y];
	}
}