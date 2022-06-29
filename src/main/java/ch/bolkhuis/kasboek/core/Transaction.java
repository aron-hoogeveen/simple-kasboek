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
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.Reader;
import java.lang.UnsupportedOperationException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * The immutable class Transaction represents the movement of money from one entity to another (debtor and creditor).
 * This version of the Transaction class does not save the type of currency of the accompanied amount. It is therefore
 * the job of the class that uses this Transaction class to enforce the same currency over all Transactions.
 * It is possible, however not required, to set a unique identifier to an instance of this class that connects a real-
 * life receipt with this transaction ({@code receiptId}).
 * Transaction enforces a short description of at most 100 characters, to ensure a concise description.
 * The amount is a positive number.
 */
public final class Transaction implements Comparable<Transaction> {
    private final int id;
    private final @NotNull LocalDate date;
    private final int debtorId;
    private final int creditorId;
    private final Integer receiptId;
    private final double amount;
    private @NotNull final String description;

    /**
     * Do not use this constructor.
     */
    private Transaction() {
        throw new UnsupportedOperationException("Cannot instantiate empty Transaction.");
    }

    /**
     * Constructs a new Transaction without {@code receiptId}. Parameters {@code id}, {@code debtorId}, {@code creditorId}
     * and {@code description} need to comply with the contracts as specified in {@link Transaction#isCorrectId(int)},
     * {@link Transaction#isCorrectDebtorId(int)}, {@link Transaction#isCorrectCreditorId(int)} and
     * {@link #Transaction#isCorrectDescription(String)} respectively.
     *
     * @param id unique id
     * @param debtorId id of the involved debtor
     * @param creditorId id of the involved creditor
     * @param amount finite double
     * @param date the date this Transaction was constructed
     * @param description a concise description of this Transaction
     * @throws IllegalArgumentException when id or description is of illegal format
     * @see Transaction#isCorrectId(int)
     * @see Transaction#isCorrectDebtorId(int)
     * @see Transaction#isCorrectCreditorId(int)
     * @see Transaction#isCorrectDescription(String)
     */
    public Transaction(int id, int debtorId, int creditorId, double amount, @NotNull LocalDate date, @NotNull String description) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(description);

        if (!isCorrectId(id)) { throw new IllegalArgumentException(); }
        if (!isCorrectDebtorId(debtorId)) { throw new IllegalArgumentException(); }
        if (!isCorrectCreditorId(creditorId)) { throw new IllegalArgumentException(); }
        if (amount < 0) { throw new IllegalArgumentException("amount should not be negative"); }
        if (!Double.isFinite(amount)) { throw new IllegalArgumentException("amount must be finite"); }
        if (!isCorrectDescription(description)) { throw new IllegalArgumentException(); }

        this.id = id;
        this.debtorId = debtorId;
        this.creditorId = creditorId;
        this.amount = amount;
        this.receiptId = null;
        this.date = date;
        this.description = description;
    }

    /**
     * Constructs a new Transaction with an associated receiptId. The
     *
     * @param receiptId the associated receiptId
     * @throws IllegalArgumentException when of its arguments does not adhere to this class' contract
     * @see Transaction#Transaction(int, int, int, double, LocalDate, String)
     */
    public Transaction(int id, int debtorId, int creditorId, double amount, Integer receiptId, @NotNull LocalDate date,
                       @NotNull String description) {
        Objects.requireNonNull(date);
        Objects.requireNonNull(description);

        if (!isCorrectId(id)) { throw new IllegalArgumentException(); }
        if (!isCorrectDebtorId(debtorId)) { throw new IllegalArgumentException(); }
        if (!isCorrectCreditorId(creditorId)) { throw new IllegalArgumentException(); }
        if (amount < 0) { throw new IllegalArgumentException("amount should not be negative"); }
        if (!Double.isFinite(amount)) { throw new IllegalArgumentException("amount must be finite"); }
        if (!isCorrectDescription(description)) { throw new IllegalArgumentException(); }

        this.id = id;
        this.debtorId = debtorId;
        this.creditorId = creditorId;
        this.amount = amount;
        this.receiptId = receiptId;
        this.date = date;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    @NotNull
    public LocalDate getDate() {
        return date;
    }

    public int getDebtorId() {
        return debtorId;
    }

    public int getCreditorId() {
        return creditorId;
    }

    /**
     * Returns the value of field {@code amount}. This number is never negative.
     * @return field {@code amount}
     */
    public double getAmount() {
        return amount;
    }

    /**
     * gets field receiptId. Returns null, when no receiptId is set.
     */
    public Integer getReceiptId() {
        return receiptId;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (id != that.id) return false;
        if (debtorId != that.debtorId) return false;
        if (creditorId != that.creditorId) return false;
        if (Double.compare(that.amount, amount) != 0) return false;
        if (!date.equals(that.date)) return false;
        if (!receiptId.equals(that.receiptId)) return false;
        return description.equals(that.description);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + date.hashCode();
        result = 31 * result + debtorId;
        result = 31 * result + creditorId;
        result = 31 * result + (receiptId != null ? receiptId.hashCode() : 0);
        temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + description.hashCode();
        return result;
    }

    /**
     * Returns the description of this Transaction.
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * @see com.google.gson.Gson#toJson(Object, Type)
     */
    public static String toJson(Transaction transaction) {
        return CustomizedGson.gson.toJson(transaction);
    }

    public static Transaction fromJson(@NotNull Reader reader) {
        Objects.requireNonNull(reader);

        BufferedReader bufferedReader = new BufferedReader(reader);
        return CustomizedGson.gson.fromJson(bufferedReader, Transaction.class);
    }

    /**
     * @deprecated contract on ids changed, which made this method obsolete
     */
    @Deprecated
    public static boolean isCorrectId(int id) {
        return true;
    }

    /**
     * @deprecated contract on ids changed, which made this method obsolete
     */
    @Deprecated
    public static boolean isCorrectDebtorId(int id) {
        return true;
    }

    /**
     * @deprecated contract on ids changed, which made this method obsolete
     */
    @Deprecated
    public static boolean isCorrectCreditorId(int id) {
        return true;
    }

    public static boolean isCorrectDescription(String description) {
        return (description != null) && (!description.isBlank()) && (description.strip().length() <= 100) && (!description.contains("\n"))
                && (!description.contains("\r"));
    }

    public static boolean isCorrectAmount(double amount) {
        return amount >= 0;
    }

    /**
     * Returns the String representation of the {@code date} of this Transaction formatted as "dd-MM-uuuu".
     *
     * @return formatted date
     * @see DateTimeFormatter#ofPattern(String)
     */
    @NotNull
    public String getDateString() {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
    }

    /**
     * TODO add specific JavaDoc for this function.
     * A {@code receiptId} of {@code null} is considered smaller than a non-null {@code receiptId} so that Transactions
     * with no corresponding Receipt are sorted lower.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(@NotNull Transaction o) {
        Objects.requireNonNull(o);

        // date
        if (this.date.isBefore(o.date)) {
            return -1;
        } else if (this.date.isAfter(o.date)) {
            return 1;
        }

        // Equal dates. Check receiptId. See the JavaDoc of this method for the decision on null sorting
        if (this.receiptId == null) {
            if (o.receiptId != null) {
                return -1;
            }
        } else if (o.receiptId == null) {
            return 1;
        } else if (this.receiptId < o.receiptId) {
            return -1;
        } else if (this.receiptId > o.receiptId) {
            return 1;
        }

        // Equal receiptIds. Check debtorId
        if (this.debtorId < o.debtorId) {
            return -1;
        } else if (this.debtorId > o.debtorId) {
            return 1;
        }

        // Equal debtorIds. Check creditorId
        if (this.creditorId < o.creditorId) {
            return -1;
        } else if (this.creditorId > o.creditorId) {
            return 1;
        }

        // Equal creditorIds. Check id
        if (this.id < o.id) {
            return -1;
        } else if (this.id > o.id) {
            return 1;
        }

        // Equal ids. Check amount
        if (this.amount < o.amount) {
            return -1;
        } else if (this.amount > o.amount) {
            return 1;
        }

        // Equal amounts. Check description
        return this.description.compareTo(o.description);
    }

    /**
     * Returns a Transaction with the same values for all its fields, except for the {@code receiptId} field, which will
     * always be null.
     *
     * @return Transaction
     */
    @NotNull
    public Transaction clearReceiptId() {
        return new Transaction(
                id,
                debtorId,
                creditorId,
                amount,
                null, // remove the association with a receipt
                date,
                description
        );
    }
}
