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

import ch.bolkhuis.kasboek.gson.CustomizedGson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import javafx.beans.property.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Objects;

/**
 * The immutable class AccountingEntry resembles an account with value. Some fields adhere to the contracts specified
 * in the functions isCorrectXXX() where XXX is equal to the fields' name.
 */
public class AccountingEntity implements Comparable<AccountingEntity> {
    protected final int id;
    @NotNull protected final ReadOnlyStringProperty name;
    @NotNull protected final AccountType accountType;
    @NotNull protected final ReadOnlyDoubleProperty balance;
    /**
     * Constructs a new AccountingEntry with {@code id} and {@code name}.
     * <br />
     * For the constraints of {@code name} see {@link AccountingEntity#isCorrectName(String)}.
     *
     * @param id a unique identifier
     * @param name the name of this AccountingEntry
     * @param accountType the AccountType of this AccountingEntity
     * @param balance a finite balance
     * @see AccountingEntity#isCorrectName(String)
     * @see Double#isFinite(double)
     */
    public AccountingEntity(int id, @NotNull String name, @NotNull AccountType accountType, double balance) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(accountType);

        if (!isCorrectName(name)) {
            throw new IllegalArgumentException("Illegal name");
        }
        if (!Double.isFinite(balance)) {
            throw new IllegalArgumentException("balance must be finite");
        }

        // Strip the string of the whitespaces before setting
        this.id = id;
        this.name = new SimpleStringProperty(name.strip());
        this.accountType = accountType;
        this.balance = new SimpleDoubleProperty(balance);
    }

    public int getId() {
        return id;
    }

    @NotNull
    public final ReadOnlyStringProperty nameProperty() { return name; }
    @NotNull
    public String getName() { return name.get(); }

    @NotNull
    public final ReadOnlyDoubleProperty balanceProperty() { return balance; }
    public double getBalance() { return balance.get(); }

    @NotNull
    public AccountType getAccountType() {
        return accountType;
    }

    /**
     * Returns a new AccountingEntity with {@code amount} added to the balance of this AccountingEntity.
     *
     * @deprecated use {@link AccountingEntity#debit(double)} and {@link AccountingEntity#credit(double)} instead
     * @param amount the amount to add to balance. Could be negative
     * @return new AccountingEntity with the added balance
     */
    @NotNull
    @Deprecated
    public AccountingEntity addBalance(double amount) {

        return new AccountingEntity(id, name.get(), accountType, balance.get() + amount);
    }

    /**
     * Returns a duplicate of this AccountingEntity after debiting {@code amount}.
     *
     * @param amount the amount to debit
     * @return duplicate of AccountingEntity
     * @throws IllegalArgumentException when amount is negative
     */
    @NotNull
    public AccountingEntity debit(double amount) {
        if (amount < 0) { throw new IllegalArgumentException("You should not debit a negative amount, credit instead"); }

        double newEndBalance = this.balance.get() + debitBalanceChange(amount);
        if (!Double.isFinite(newEndBalance)) {
            throw new ArithmeticException("The new balance must be a finite number");
        }
        return new AccountingEntity(id, name.get(), accountType, newEndBalance);
    }

    /**
     * Returns a duplicate of this AccountingEntity after crediting {@code amount}.
     *
     * @param amount the amount to credit
     * @return duplicate of AccountingEntity
     * @throws IllegalArgumentException when amount is negative
     */
    @NotNull
    public AccountingEntity credit(double amount) {
        if (amount < 0) { throw new IllegalArgumentException("You should not credit a negative amount, debit instead"); }

        double newEndBalance = this.balance.get() + creditBalanceChange(amount);
        if (!Double.isFinite(newEndBalance)) {
            throw new ArithmeticException("The new balance must be a finite number");
        }
        return new AccountingEntity(id, name.get(), accountType, newEndBalance);
    }

    /**
     * Calculate the change in balance when {@code amount} is debited on this AccountingEntity.
     *
     * @param amount the amount to debit
     * @return the balance change
     */
    public double debitBalanceChange(double amount) {
        if (amount < 0) { throw new IllegalArgumentException("You should not debit a negative amount"); }
        return (accountType.isDebit() ? amount : -1 * amount);
    }

    /**
     * Calculate the change in balance when {@code amount} is credited on this AccountingEntity.
     *
     * @param amount the amount to credit
     * @return the balance change
     */
    public double creditBalanceChange(double amount) {
        if (amount < 0) { throw new IllegalArgumentException("You should not credit a negative amount"); }
        return (accountType.isDebit() ? -1 * amount : amount);
    }

    /**
     * @see com.google.gson.Gson#toJson(Object, Type)
     */
    public static String toJson(AccountingEntity accountingEntity) {
        return CustomizedGson.gson.toJson(accountingEntity);
    }

    /**
     * Returns a new AccountingEntity that is compatible with the JSON returned from {@link AccountingEntity#toJson(AccountingEntity)}.
     *
     * @param reader input Reader
     * @return new AccountingEntity
     * @throws JsonSyntaxException see GSON docs
     * @throws JsonIOException see GSON docs
     */
    public static AccountingEntity fromJson(@NotNull Reader reader) {
        Objects.requireNonNull(reader);

        BufferedReader bufferedReader = new BufferedReader(reader);
        return CustomizedGson.gson.fromJson(bufferedReader, AccountingEntity.class);
    }

    /**
     *
     *
     * @param o1 the first object to be compared.
     * @param o the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compareTo(@NotNull AccountingEntity o) {
        Objects.requireNonNull(o);

        // compare name
        if (this.getName().compareToIgnoreCase(o.getName()) > 0) {
            return 1;
        } else if (this.getName().compareToIgnoreCase(o.getName()) < 0) {
            return -1;
        }

        // compare balance
        if (this.getBalance() > o.getBalance()) {
            return 1;
        } else if (this.getBalance() < o.getBalance()) {
            return -1;
        }

        // compare AccountType
        if (this.getAccountType().compareTo(o.getAccountType()) > 0) {
            return 1;
        } else if (this.getAccountType().compareTo(o.getAccountType()) < 0) {
            return -1;
        }

        return (this.getId() - o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountingEntity that = (AccountingEntity) o;

        if (id != that.id) return false;
        if (Double.compare(that.balance.get(), balance.get()) != 0) return false;
        if (!name.get().equals(that.name.get())) return false;
        return accountType == that.accountType;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + name.get().hashCode();
        result = 31 * result + accountType.hashCode();
        temp = Double.doubleToLongBits(balance.get());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Returns whether the provided name is a correct name for constructing a new AccountingEntity. Name constraints are:<br />
     * - Not null;<br />
     * - Not empty;<br />
     * - Not whitespace only;<br />
     * - Max length of 256 characters;<br />
     * - Does not contain newline characters.<br />
     * - Is all alphabetical.<br />
     *
     * @param name the name to check
     * @return true if this is a correct name, false otherwise
     */
    public static boolean isCorrectName(String name) {
        return (name != null) && (!name.isBlank()) && (name.strip().length() <= 256) && (!name.contains("\n"))
                && (!name.contains("\r")) && (name.strip().matches("[a-zA-Z ]*"));
    }

    /**
     * Returns the name of this AccountingEntity.
     */
    @Override
    public String toString() {
        return name.get();
    }
}
