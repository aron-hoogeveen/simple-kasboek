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

import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

/**
 * AccountType is an enumeration that indicates what type a specific accounting entity is. See
 * <a href="https://www.principlesofaccounting.com/account-types/" target="_top">PrinciplesOfAccounting.com</a> for the
 * different accounting types.
 */
public enum AccountType {
    @SerializedName("expense")
    EXPENSE(true, "Expense"),
    @SerializedName("asset")
    ASSET(true, "Asset"),
    @SerializedName("dividend")
    DIVIDEND(true, "Dividend"),
    @SerializedName("liability")
    LIABILITY(false, "Liability"),
    @SerializedName("revenue")
    REVENUE(false, "Revenue"),
    @SerializedName("equity")
    EQUITY(false, "Equity"),
    @SerializedName("nonexistent")
    NON_EXISTENT(true, "Non existent");

    private final boolean debit;
    private final String name;

    AccountType(boolean isDebit, String name) {
        this.debit = isDebit;
        this.name = name;
    }

    /**
     * Returns {@code true} when this AccountType is a debit account, {@code false} when this AccountType is a credit
     * account.
     */
    public boolean isDebit() {
        return debit;
    }


    @Override
    public String toString() {
        return name;
    }
}
