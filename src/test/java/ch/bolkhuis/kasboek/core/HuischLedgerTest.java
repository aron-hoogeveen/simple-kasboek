package ch.bolkhuis.kasboek.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class HuischLedgerTest {

    private HuischLedger ledger;
    private ResidentEntity dummy1;
    private ResidentEntity dummy2;

    @BeforeEach
    public void initialize() {
        ledger = new HuischLedger();
        dummy1 = new ResidentEntity(
                0,
                "Gerrit",
                0,
                0
        );
        dummy2 = new ResidentEntity(
                1,
                "Truus",
                0,
                0
        );
    }

    @Test
    public void testConstructor() {
        assertEquals(0, ledger.getReceipts().size());
        assertEquals(0, ledger.getAccountingEntities().size());
        assertEquals(0, ledger.getTransactions().size());
    }

    @Test
    public void testReceiptId() {
        assertEquals(0, ledger.getNextReceiptId());
        assertEquals(0, ledger.getAndIncrementNextReceiptId());
        assertEquals(1, ledger.getAndIncrementNextReceiptId());
    }

    @Test
    public void testUnmodifiabilityOfReceipts() {
        assertThrows(UnsupportedOperationException.class, () -> ledger.getReceipts().put(0, null));
    }

    @Test
    public void testAddingReceipts() {
        ledger.addAccountingEntity(dummy1);
        Receipt receipt1 = new Receipt(
                0,
                "Empty receipt",
                null,
                LocalDate.now(),
                0
        );
        assertEquals(0, ledger.getReceipts().size());
        assertDoesNotThrow(() -> ledger.addReceipt(receipt1));
        assertEquals(1, ledger.getReceipts().size());
        assertThrows(IllegalArgumentException.class, () -> ledger.addReceipt(receipt1));

        // add receipt with unknown payer
        Receipt receipt2 = new Receipt(
                1,
                "Empty",
                null,
                LocalDate.now(),
                42
        );
        assertThrows(IllegalArgumentException.class, () -> ledger.addReceipt(receipt2));
    }

    // TODO create test for receipt that contains references to transactions that belong to different receipts.

}