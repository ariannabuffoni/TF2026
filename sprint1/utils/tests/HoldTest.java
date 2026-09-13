package tests;

import static org.junit.Assert.*;
import org.junit.*;

import cargoservice.Hold;

public class HoldTest {
    private Hold hold;

    @Before
    public void init() {
        hold = new Hold();
    }

    @Test
    public void testSequentialReservation() {
        assertEquals(1, hold.reserveNextFreeSlot());
        assertEquals(2, hold.reserveNextFreeSlot());
        assertEquals(Hold.SlotState.RESERVED, hold.getSlotState(1));
        assertEquals(Hold.SlotState.RESERVED, hold.getSlotState(2));
    }

    @Test
    public void testIsFullCondition() {
        assertFalse(hold.isFull());
        for (int i = 0; i < Hold.NUM_MAIN_SLOTS; i++) {
            hold.reserveNextFreeSlot();
        }
        assertTrue(hold.isFull());
    }

    @Test
    public void testReservationFailureWhenFull() {
        for (int i = 0; i < Hold.NUM_MAIN_SLOTS; i++) {
            hold.reserveNextFreeSlot();
        }
        assertEquals(Hold.NO_SLOT_AVAILABLE, hold.reserveNextFreeSlot());
    }

    @Test
    public void testSlotStateTransition() {
        int reservedSlot = hold.reserveNextFreeSlot();
        assertEquals(1, reservedSlot);
        assertEquals(Hold.SlotState.RESERVED, hold.getSlotState(1));

        hold.setSlotOccupied(reservedSlot);
        assertEquals(Hold.SlotState.OCCUPIED, hold.getSlotState(1));
    }

    @Test
    public void testReleaseSlot() {
        int reservedSlot = hold.reserveNextFreeSlot();
        hold.releaseSlot(reservedSlot);
        assertEquals(Hold.SlotState.FREE, hold.getSlotState(reservedSlot));
        assertFalse(hold.isFull());
    }
}