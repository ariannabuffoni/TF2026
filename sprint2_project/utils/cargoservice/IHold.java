package cargoservice;

public interface IHold {
	public enum SlotState {
        FREE,
        RESERVED,
        OCCUPIED
    }

    public static final int NUM_MAIN_SLOTS = 4;
    public static final int NO_SLOT_AVAILABLE = -1;
    
    // Riserva il primo slot libero [1,4] o restituisce NO_SLOT_AVAILABLE (-1) se la stiva è piena
    public int reserveNextFreeSlot();
    
    // Libera uno slot
    public void releaseSlot(int slotNumber);
    // Occupa uno slot 
    public void setSlotOccupied(int slotNumber);
    
    // Restituisce la coordinata x dell'entrata dello slot indicato
    public int getEntranceX(int slotNumber);
    // Restituisce la coordinata y dell'entrata dello slot indicato
    public int getEntranceY(int slotNumber);
    
    // Restituisce coordinate HOME
    public int getHomeX();
    public int getHomeY();
    
    // Restituisce coordinate IOPort
    public int getIOPortX();
    public int getIOPortY();
    
    // Restituisce coordinate marker/slot5
    public int getMarkerX();
    public int getMarkerY();
    
    // Ritorna una stringa formattata per i messaggi QAk (es. [RESERVED, FREE, FREE, FREE])
    public String getHoldStateString();
    
    // Controlla se la stiva è piena (nessuno slot in stato FREE)
    public boolean isFull();
    
    // Per test
    public SlotState getSlotState(int slotNumber);
}
