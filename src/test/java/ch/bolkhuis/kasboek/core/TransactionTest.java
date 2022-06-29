package ch.bolkhuis.kasboek.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Description copied from the {@link Transaction} class:
 *
 * The immutable class Transaction represents the movement of money from one entity to another (debtor and creditor).
 * This version of the Transaction class does not save the type of currency of the accompanied amount. It is therefore
 * the job of the class that uses this Transaction class to enforce the same currency over all Transactions.
 * It is possible, however not required, to set a unique identifier to an instance of this class that connects a real-
 * life receipt with this transaction ({@code receiptId}).
 * Transaction enforces a short description of at most 100 characters, to ensure a concise description.
 */
class TransactionTest {
    private Transaction globalTransaction;

    private final int ID = 0;
    private final int DEBTOR_ID = 0;
    private final int CREDITOR_ID = 0;
    private final double AMOUNT = 23.88;
    private final Integer RECEIPT_ID = null;
    private final LocalDate DATE = LocalDate.now();
    private final String DESCRIPTION = "Sample Transaction";

    @BeforeEach
    public void initialize() {
        globalTransaction = new Transaction(ID, DEBTOR_ID, CREDITOR_ID, AMOUNT, DATE, DESCRIPTION);
    }

    @Test
    public void testGetters() {
        assertEquals(ID, globalTransaction.getId());
        assertEquals(DEBTOR_ID, globalTransaction.getDebtorId());
        assertEquals(CREDITOR_ID, globalTransaction.getCreditorId());
        assertEquals(AMOUNT, globalTransaction.getAmount());
        assertEquals(RECEIPT_ID, globalTransaction.getReceiptId());
        assertEquals(DATE, globalTransaction.getDate());
        assertEquals(DESCRIPTION, globalTransaction.getDescription());
    }

    @Test
    public void testParameterValidationMethods() {
//        assertTrue(Transaction.isCorrectId(0));
//        assertFalse(Transaction.isCorrectId(-1));
//
//        assertTrue(Transaction.isCorrectDebtorId(0));
//        assertFalse(Transaction.isCorrectDebtorId(-1));
//
//        assertTrue(Transaction.isCorrectCreditorId(0));
//        assertFalse(Transaction.isCorrectCreditorId(-1));

        assertTrue(Transaction.isCorrectDescription("ydcnVc2ElZMbFX3j0IeHOWIeoPIy1TWmWTSb83wKNbjHQaBu02vmcydP0ijQ5Bst2EPPl6PrFp7epAQF86NZ8vPTWabG3pxGslaj"));
        assertTrue(Transaction.isCorrectDescription("h̷͔͖̫̪͙̊e̸̞̝͚̍͜l̸̺͐͆͑́̋̕l̸̝̲̾ͅô̷̙͕͈̗̲͑̃́͘ ̵͛̀̑͒̋̈́͜t̶̤͙̣͖́͗̏̿̃͝h̵̦̦̫̯̺̪͋̒̐̾͗i̴̜̹̯͌s̸̢̪͎͔̹̔̍̒̕͜"));
        assertFalse(Transaction.isCorrectDescription("    "));
        assertFalse(Transaction.isCorrectDescription(null));
        assertFalse(Transaction.isCorrectDescription("newline\ncharacter"));
        assertFalse(Transaction.isCorrectDescription("the other\rnewline character"));
        assertFalse(Transaction.isCorrectDescription("ydcnVc2ElZMbFX3j0IeHOWIeoPIy1TWmWTSb83wKNbjHQaBu02vmcydP0ijQ5Bst2EPPl6PrFp7epAQF86NZ8vPTWabG3pxGslajd")); // 101 characters
    }

    @Test
    public void toJsonOnEmptyObject() {
        Assertions.assertDoesNotThrow(() -> Transaction.toJson(null));
    }

    @Test
    public void comparabeEqualTransactions() {
        Transaction t1 = new Transaction(
                0,
                0,
                0,
                0,
                null,
                LocalDate.parse("2020-02-02"),
                "Description"
        );
        assertEquals(0, t1.compareTo(t1));
    }

    @Test
    public void compareDifferentDates() {
        Transaction t1 = new Transaction(
                0,
                0,
                0,
                0,
                null,
                LocalDate.parse("2020-01-01"),
                "Description"
        );
        Transaction t2 = new Transaction(
                0,
                0,
                0,
                0,
                null,
                LocalDate.parse("2020-01-02"),
                "Description"
        );

        assertTrue(t1.compareTo(t2) < 0 && t2.compareTo(t1) > 0);
    }

    @Test
    public void compareNullReceipt() {
        Transaction t1 = new Transaction(
                0,
                0,
                0,
                0,
                2,
                LocalDate.parse("2020-01-01"),
                "Description"
        );
        Transaction t2 = new Transaction(
                0,
                0,
                0,
                0,
                null,
                LocalDate.parse("2020-01-01"),
                "Description"
        );

        assertTrue(t1.compareTo(t2) > 0 && t2.compareTo(t1) < 0);
    }

    @Test
    public void throwOnNonFiniteDoubleNumberInConstructors() {
        // positive infinity
        assertThrows(IllegalArgumentException.class, () -> new Transaction(
                0,
                0,
                1,
                Double.POSITIVE_INFINITY,
                null,
                LocalDate.now(),
                "Some description"
        ));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(
                0,
                0,
                1,
                Double.POSITIVE_INFINITY,
                LocalDate.now(),
                "Some description"
        ));

        // negative infinity
        assertThrows(IllegalArgumentException.class, () -> new Transaction(
                0,
                0,
                1,
                Double.NEGATIVE_INFINITY,
                null,
                LocalDate.now(),
                "Some description"
        ));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(
                0,
                0,
                1,
                Double.NEGATIVE_INFINITY,
                LocalDate.now(),
                "Some description"
        ));

        // NaN
        assertThrows(IllegalArgumentException.class, () -> new Transaction(
                0,
                0,
                1,
                Double.NaN,
                null,
                LocalDate.now(),
                "Some description"
        ));
        assertThrows(IllegalArgumentException.class, () -> new Transaction(
                0,
                0,
                1,
                Double.NaN,
                LocalDate.now(),
                "Some description"
        ));
    }
}