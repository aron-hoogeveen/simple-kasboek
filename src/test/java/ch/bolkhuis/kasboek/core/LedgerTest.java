package ch.bolkhuis.kasboek.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LedgerTest {
    private Ledger ledger;
    private AccountingEntity dummy1;
    private AccountingEntity dummy2;

    @BeforeEach
    public void initialize() {
        ledger = new Ledger();

        dummy1 = new AccountingEntity(
                0,
                "Dummy een",
                AccountType.ASSET,
                0
        );
        dummy2 = new AccountingEntity(
                1,
                "Dummy twee",
                AccountType.LIABILITY,
                0
        );
    }

    @Test
    public void testDefaultConstructor() {
        assertEquals(0, ledger.getNextAccountingEntityId());
        assertEquals(0, ledger.getNextTransactionId());
        assertEquals(0, ledger.getAccountingEntities().size());
        assertEquals(0, ledger.getTransactions().size());
    }

    @Test
    public void testGetAndIncrementIds() {
        assertEquals(0, ledger.getNextAccountingEntityId());
        assertEquals(0, ledger.getAndIncrementNextAccountingEntityId());
        for (int i = 1; i < 10; i++) {
            assertEquals(i, ledger.getAndIncrementNextAccountingEntityId());
        }

        assertEquals(0, ledger.getNextTransactionId());
        assertEquals(0, ledger.getAndIncrementNextTransactionId());
        for (int i = 1; i < 10; i++) {
            assertEquals(i, ledger.getAndIncrementNextTransactionId());
        }
    }

    @Test
    public void testAccountingEntityAddition() {
        int id1 = ledger.getAndIncrementNextAccountingEntityId();
        AccountingEntity entity1 = new AccountingEntity(
                id1,
                "some name",
                AccountType.ASSET,
                0
        );
        int id2 = ledger.getAndIncrementNextAccountingEntityId();
        AccountingEntity entity2 = new AccountingEntity(
                id2,
                "some other name",
                AccountType.ASSET,
                0
        );
        AccountingEntity entity2DuplicateId = new AccountingEntity(
                id2,
                "different name",
                AccountType.EQUITY,
                12
        );

        // add entities
        ledger.addAccountingEntity(entity1);
        assertThrows(IllegalArgumentException.class, () -> ledger.addAccountingEntity(entity1)); // duplicate entity
        ledger.addAccountingEntity(entity2);
        assertThrows(IllegalArgumentException.class, () -> ledger.addAccountingEntity(entity2DuplicateId)); // duplicate id

        assertEquals(2, ledger.getAccountingEntities().size());
    }

    @Test
    public void testAddingAndRemovalOfTransactions() {
        ledger.addAccountingEntity(dummy1);
        ledger.addAccountingEntity(dummy2);

        Transaction t1 = new Transaction(
                0,
                dummy1.getId(),
                dummy2.getId(),
                50,
                LocalDate.now(),
                 "Transaction 1"
        );
        Transaction t2 = new Transaction(
                1,
                dummy2.getId(), // reversed creditor/debtor with respect to t1
                dummy1.getId(),
                50,
                LocalDate.now(),
                "Description 2"
        );

        assertEquals(0, ledger.getTransactions().size());
        ledger.addTransaction(t1);
        assertEquals(1, ledger.getTransactions().size());
        assertEquals(50.0, ledger.getAccountingEntities().get(dummy1.getId()).getBalance());
        assertEquals(50.0, ledger.getAccountingEntities().get(dummy2.getId()).getBalance());
        ledger.addTransaction(t2);
        assertEquals(2, ledger.getTransactions().size());
        assertEquals(0.0, ledger.getAccountingEntities().get(dummy1.getId()).getBalance());
        assertEquals(0.0, ledger.getAccountingEntities().get(dummy2.getId()).getBalance());
        assertEquals(t1, ledger.removeTransaction(t1.getId())); // remove by id
        assertEquals(1, ledger.getTransactions().size());
        assertEquals(-50.0, ledger.getAccountingEntities().get(dummy1.getId()).getBalance());
        assertEquals(-50.0, ledger.getAccountingEntities().get(dummy2.getId()).getBalance());
        assertThrows(IllegalArgumentException.class, () -> ledger.addTransaction(t2));
        assertEquals(t2, ledger.removeTransaction(t2)); // remove by Transaction
    }

    @Test
    public void testTransactionWithMissingEntities() {
        Transaction t1 = new Transaction(
                0,
                0,
                1,
                50,
                LocalDate.now(),
                "Some Description"
        );
        // both missing
        assertThrows(IllegalArgumentException.class, () -> ledger.addTransaction(t1));

        ledger.addAccountingEntity(dummy1);
        Transaction t2 = new Transaction(
                1,
                0,
                2,
                50,
                LocalDate.now(),
                "i8ulskdjf"
        );
        // creditor missing
        assertThrows(IllegalArgumentException.class, () -> ledger.addTransaction(t2));

        Transaction t3 = new Transaction(
                2,
                2,
                0,
                50,
                LocalDate.now(),
                "kldjf"
        );
        // debtor missing
        assertThrows(IllegalArgumentException.class, () -> ledger.addTransaction(t3));
    }

    @Test
    public void testUpdateAccountingEntity() {
        ledger.addAccountingEntity(dummy1);
        AccountingEntity edit = new AccountingEntity(
                dummy1.getId(),
                "Something else for sure",
                dummy1.getAccountType(),
                42
        );
        assertEquals(dummy1, ledger.updateAccountingEntity(edit));
    }

    @Test
    public void entityUpdateFailsWhenAccountTypeDiffers() {
        ledger.addAccountingEntity(dummy1); // AccountType = Asset
        AccountingEntity entity = new AccountingEntity(
                dummy1.getId(),
                "something elkse",
                AccountType.EQUITY,
                42
        );
        assertThrows(IllegalArgumentException.class, () -> ledger.updateAccountingEntity(entity));
    }

    @Test
    public void testObservableListsAreUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> ledger.getAccountingEntities().put(dummy1.getId(), dummy1));

        Transaction t1 = new Transaction(
                0,
                0,
                1,
                50,
                LocalDate.now(),
                "dkljfd"
        );
        assertThrows(UnsupportedOperationException.class, () -> ledger.getTransactions().put(t1.getId(), t1));

        // remove added transaction
        ledger.addAccountingEntity(dummy1);
        ledger.addAccountingEntity(dummy2);
        ledger.addTransaction(t1);
        assertThrows(UnsupportedOperationException.class, () -> ledger.getTransactions().remove(t1.getId()));
    }

    @Test
    public void testDuplicateEntityNamesNotAllowed() {
        AccountingEntity e1 = new AccountingEntity(
                0,
                "name",
                AccountType.ASSET,
                0
        );
        AccountingEntity e2 = new AccountingEntity(
                1,
                "name",
                AccountType.ASSET,
                0
        ); // identical
        AccountingEntity e3 = new AccountingEntity(
                2,
                "Name",
                AccountType.ASSET,
                0
        ); // Different case
        AccountingEntity e4 = new AccountingEntity(
                3,
                " Name ",
                AccountType.ASSET,
                0
        ); // trailing whitespace

        ledger.addAccountingEntity(e1);
        assertThrows(IllegalArgumentException.class, () -> ledger.addAccountingEntity(e2));
        assertThrows(IllegalArgumentException.class, () -> ledger.addAccountingEntity(e3));
        assertThrows(IllegalArgumentException.class, () -> ledger.addAccountingEntity(e4));

        AccountingEntity e5 = new AccountingEntity(
                4,
                "Different name",
                AccountType.ASSET,
                0
        );
        ledger.addAccountingEntity(e5);
        AccountingEntity e6 = new AccountingEntity(
                e5.getId(),
                "name", // already present
                AccountType.ASSET,
                45
        );
        assertThrows(IllegalArgumentException.class, () -> ledger.updateAccountingEntity(e6));
    }

    // TODO add tests for getTransactionsOf(int, LocalDate, LocalDate)

}