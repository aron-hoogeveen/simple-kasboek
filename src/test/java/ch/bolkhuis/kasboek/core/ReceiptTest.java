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

import javafx.collections.ObservableSet;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptTest {

    @Test
    public void transactionIdSetIsUnmodifiable() {
        Receipt receipt = new Receipt(
                0,
                "test",
                Set.of(
                        0,
                        1,
                        2
                ),
                LocalDate.now(),
                8
        );

        ObservableSet<Integer> observableSet = receipt.getTransactionIdSet();
        assertThrows(UnsupportedOperationException.class, () -> observableSet.add(3));
    }

    @Test
    public void registerNonRegisteredId() {
        Receipt receipt = new Receipt(
                0,
                "test",
                new HashSet<>(), // empty set
                LocalDate.now(),
                0
        );

        assertFalse(receipt.containsTransactionId(0));
        receipt.registerTransaction(0);
        assertTrue(receipt.containsTransactionId(0));
    }

    @Test
    public void unregisterPreRegisteredId() {
        Receipt receipt = new Receipt(
                0,
                "test",
                Set.of(3), // empty set, unmodifiable
                LocalDate.now(),
                0
        );

        assertTrue(receipt.containsTransactionId(3));
        receipt.unregisterTransaction(3);
        assertFalse(receipt.containsTransactionId(3));
    }
}