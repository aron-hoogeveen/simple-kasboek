/*
 * Copyright (C) 2020 Aron Hoogeveen
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ch.bolkhuis.kasboek.core;

import org.jetbrains.annotations.NotNull;

/**
 * Class ResidentEntity represents an {@link AccountingEntity} that is a resident of the Bolkhuisch.
 * TODO refactor name of this class to 'ResidentEntity'
 */
public class ResidentEntity extends AccountingEntity {
    /**
     * The vale of {@code balance} at the time the last invoice was generated for this ResidentEntity
     */
    private final double previousBalance;
    /**
     * Constructs a new AccountingEntry with {@code id} and {@code name}.
     * <br />
     * For the constraints of {@code id} and {@code name} see {@link AccountingEntity#isCorrectId(int)} and
     * {@link AccountingEntity#isCorrectName(String)} respectively. An Inmate is always an {@link AccountType#LIABILITY}.
     *
     * @param id           a unique identifier
     * @param name         the name of this AccountingEntry
     * @param previousBalance the value of endBalance at the time the last invoice was generated for this ResidentEntity
     * @param balance   the current balance of this ResidentEntity
     * @see AccountingEntity#isCorrectId(int)
     * @see AccountingEntity#isCorrectName(String)
     */
    public ResidentEntity(int id, @NotNull String name, double previousBalance, double balance) {
        super(id, name, AccountType.LIABILITY, balance);

        this.previousBalance = previousBalance;
    }

    public double getPreviousBalance() {
        return previousBalance;
    }

    @Override
    public @NotNull ResidentEntity debit(double amount) {
        if (amount < 0) { throw new IllegalArgumentException("You should not debit a negative amount, credit instead"); }

        double balance = (accountType.isDebit()) ? this.balance.get() + amount : this.balance.get() - amount;
        return new ResidentEntity(id, name.get(), previousBalance, balance);
    }

    @Override
    public @NotNull ResidentEntity credit(double amount) {
        if (amount < 0) { throw new IllegalArgumentException("You should not credit a negative amount, debit instead"); }

        double balance = (accountType.isDebit()) ? this.balance.get() - amount : this.balance.get() + amount;
        return new ResidentEntity(id, name.get(), previousBalance, balance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ResidentEntity that = (ResidentEntity) o;

        return Double.compare(that.previousBalance, previousBalance) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(previousBalance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Returns a ResidentEntity with the same properties as this ResidentEntity except for the value of the {@code previousBalance}
     * property.
     *
     * @param d the value to set {@code previousBalance} to
     * @return the new ResidentEntity
     */
    public ResidentEntity setPreviousBalance(double d) {
        return new ResidentEntity(
                id,
                getName(),
                d,
                getBalance()
        );
    }
}
