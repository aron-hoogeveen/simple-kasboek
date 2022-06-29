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

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class Receipt holds a collection of Transaction ids from related Transactions.
 *
 * TODO add field that indicates that the payer is already invoiced for this Receipt. If the field is true, than this Receipt cannot be edited
 */
public class Receipt implements Comparable<Receipt> {
    private final int id;
    private @NotNull final String name;
    private @NotNull final ObservableSet<Integer> transactionIdSet;
    private final @NotNull LocalDate date;
    private final int payer;

    /**
     * Creates a Receipt. Fail-fast. This method copies the provided transactionIdSet to a modifiable
     * set, to ensure that the ObservableSet remains modifiable.
     *
     * @param id the identifier
     * @param name the name
     * @param transactionIdSet a Set with integers that correspond to related Transaction ids // FIXME bug, user can use an unmodifiable set as backing set. Ensure modifiable sets are provided!
     * @param date the date this receipt 'happened'
     */
    public Receipt(int id, @NotNull String name, Set<Integer> transactionIdSet, @NotNull LocalDate date, int payer) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(date);

        this.id = id;
        this.name = name;
        if (transactionIdSet == null) {
            this.transactionIdSet = FXCollections.observableSet(new HashSet<>());
        } else {
            // ensure the set is modifiable
            this.transactionIdSet = FXCollections.observableSet(new HashSet<>(transactionIdSet));
        }
        this.date = date;
        this.payer = payer;
    }

    public int getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns an unmodifiable version of the observable Transaction Set.
     *
     * @return unmodifiable observable set
     */
    @NotNull
    public ObservableSet<Integer> getTransactionIdSet() {
        return FXCollections.unmodifiableObservableSet(transactionIdSet);
    }

    @NotNull
    public LocalDate getDate() {
        return date;
    }

    public int getPayer() {
        return payer;
    }

    /**
     * Returns {@code true} if the set did not already contain the transaction id.
     *
     * @param transactionId the id of the Transaction to register with this Receipt
     * @return {@code true} if the set did not already contain this Transaction
     */
    public boolean registerTransaction(int transactionId) {
        return transactionIdSet.add(transactionId);
    }

    /**
     * Unregisters the transaction with id {@code transactionId} from this Receipt.
     *
     * @param transactionId the id of the Transaction to unregister
     * @return true if the set contained the id
     */
    public boolean unregisterTransaction(int transactionId) {
        return transactionIdSet.remove(transactionId);
    }

    /**
     * Returns whether {@code transactionIdSet} contains the id.
     *
     * @param transactionId the id to check
     * @return whether the set contains the id
     */
    public boolean containsTransactionId(int transactionId) {
        return transactionIdSet.contains(transactionId);
    }

    @Override
    public String toString() {
        return name + " (" + date + ")";
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(@NotNull Receipt o) {
        Objects.requireNonNull(o);

        // date
        int comp = this.date.compareTo(o.date);
        if (comp != 0) {
            return comp;
        }

        // payer
        comp = this.payer - o.payer;
        if (comp != 0) {
            return comp;
        }

        // description
        comp = this.name.compareTo(o.name);
        if (comp != 0) {
            return comp;
        }

        // id
        return this.id - o.id;
    }

    /**
     * Returns if {@code o} is considered equal to this Receipt. Field {@code transactionIdSet} is not considered when
     * comparing.
     *
     * @param o the object to compare to
     * @return true if this and o are considered equal, else false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Receipt receipt = (Receipt) o;

        if (id != receipt.id) return false;
        if (payer != receipt.payer) return false;
        if (!name.equals(receipt.name)) return false;
        return date.equals(receipt.date);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + payer;
        return result;
    }
}
