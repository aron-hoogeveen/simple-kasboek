package ch.bolkhuis.kasboek.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class AccountingEntityTest {
    private AccountingEntity globalEntity;
    private final int id = 0;
    private final String name = "Some Asset";
    private final AccountType accountType = AccountType.ASSET;
    private final double balance = 25.27;

    @BeforeEach
    public void initialise() {
        globalEntity = new AccountingEntity(
                id,
                name,
                accountType,
                balance
        );
    }

    @Test
    public void testConstructorNotNullParameters() {
        assertThrows(NullPointerException.class, () -> new AccountingEntity(
                0,
                null,
                AccountType.ASSET,
                0
        ));
        assertThrows(NullPointerException.class, () -> new AccountingEntity(
                0,
                "Name",
                null,
                0
        ));
    }

    @Test
    public void testNameChecking() {
        assertDoesNotThrow(() -> AccountingEntity.isCorrectName("Name name"));

        // non-alphabetical character
        assertFalse(AccountingEntity.isCorrectName("Na0me"));
        // only whitespace characters
        assertFalse(AccountingEntity.isCorrectName("    \n\t\r"));
        // null value
        assertFalse(AccountingEntity.isCorrectName(null));
        // empty
        assertFalse(AccountingEntity.isCorrectName(""));
        // newline characters
        assertFalse(AccountingEntity.isCorrectName("Hello\nmy name is gerrit"));
    }

    @Test
    public void equalsSelf() {
        assertEquals(globalEntity, globalEntity);
    }

    @Test
    public void equalsOther() {
        AccountingEntity other = new AccountingEntity(
                id,
                name,
                accountType,
                balance
        );
        assertEquals(globalEntity, other);
    }

    @Test
    public void notEqualsOtherId() {
        AccountingEntity other = new AccountingEntity(
                id + 1,
                name,
                accountType,
                balance
        );
        assertNotEquals(globalEntity, other);
    }

    @Test
    public void notEqualsOtherName() {
        AccountingEntity other = new AccountingEntity(
                id,
                name + "j",
                accountType,
                balance
        );
        assertNotEquals(globalEntity, other);
    }

    @Test
    public void notEqualsOtherType() {
        AccountingEntity other = new AccountingEntity(
                id,
                name,
                AccountType.EQUITY,
                balance
        );
        assertNotEquals(globalEntity, other);
    }

    @Test
    public void notEqualsOtherBalance() {
        AccountingEntity other = new AccountingEntity(
                id,
                name,
                accountType,
                balance + 0.1
        );
        assertNotEquals(globalEntity, other);
    }

    @Test
    public void testAmountNaN() {
        assertThrows(IllegalArgumentException.class, () -> new AccountingEntity(
                id,
                name,
                accountType,
                Double.NaN
        ));
    }

    @Test
    public void testAmountInf() {
        assertThrows(IllegalArgumentException.class, () -> new AccountingEntity(
                id,
                name,
                accountType,
                Double.NEGATIVE_INFINITY
        ));
        assertThrows(IllegalArgumentException.class, () -> new AccountingEntity(
                id,
                name,
                accountType,
                Double.POSITIVE_INFINITY
        ));
    }

    @Test
    public void debitPositiveAmountResultsInFiniteNumber() {
        double amount = 25.12;
        double newBalance = balance + amount;
        AccountingEntity other = new AccountingEntity(
                id,
                name,
                accountType,
                newBalance
        );

        globalEntity = globalEntity.debit(amount);

        assertEquals(globalEntity, other);
    }

    @Test
    public void debitThatResultsInNotFiniteNumberThrowsArithmeticException() {
        AccountingEntity entity1 = new AccountingEntity(
                id,
                name,
                accountType,
                Double.MAX_VALUE
        );
        assertThrows(ArithmeticException.class, () -> entity1.debit(Double.MAX_VALUE));
    }

    @Test
    public void debitNegativeAmountThrows() {
        assertThrows(IllegalArgumentException.class, () -> globalEntity.debit(-1));
    }

    @Test
    public void creditPositiveAmountResultsInFiniteNumber() {
        double amount = 25.88;
        double newBalance = balance - amount;
        AccountingEntity other = new AccountingEntity(
                id,
                name,
                accountType,
                newBalance
        );

        globalEntity = globalEntity.credit(amount);

        assertEquals(globalEntity, other);
    }

    @Test
    public void creditThatResultsInNotFiniteNumberThrowsArithmeticException() {
        AccountingEntity entity1 = new AccountingEntity(
                id,
                name,
                accountType,
                -1 * Double.MAX_VALUE
        );
        assertThrows(ArithmeticException.class, () -> entity1.credit(Double.MAX_VALUE));
    }

    @Test
    public void creditNegativeAmountThrows() {
        assertThrows(IllegalArgumentException.class, () -> globalEntity.credit(-1));
    }

//    @ParameterizedTest
//    @CsvSource({
//        "0, Gerrit, Asset, 0",
//    })
//    public void testCompareTo(ArgumentsAccessor arguments) {
//
//    }

}