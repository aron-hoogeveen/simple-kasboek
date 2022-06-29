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
package ch.bolkhuis.kasboek.gson;

import ch.bolkhuis.kasboek.core.AccountType;
import ch.bolkhuis.kasboek.core.AccountingEntity;
import ch.bolkhuis.kasboek.core.ResidentEntity;
import ch.bolkhuis.kasboek.core.Ledger;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;

public class GsonTester {
    private static Ledger ledger = new Ledger();

    public static void main(String[] args) {
        AccountingEntity inmate1 = new ResidentEntity(
                ledger.getAndIncrementNextAccountingEntityId(),
                "Klaas",
                0,
                0
        );
        AccountingEntity inmate2 = new ResidentEntity(
                ledger.getAndIncrementNextAccountingEntityId(),
                "Truus",
                0,
                0
        );
        AccountingEntity bank = new AccountingEntity(
                ledger.getAndIncrementNextAccountingEntityId(),
                "Bank",
                AccountType.ASSET,
                0
        );

        ledger.addAccountingEntity(inmate1);
        ledger.addAccountingEntity(inmate2);
        ledger.addAccountingEntity(bank);

        // try to write this to a file
        try {
            File file = new File("out/GsonTester_ledger.json");
            Ledger.toFile(file, ledger);

            // Try to construct it again
            ledger = Ledger.fromFile(file);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.err.println("A general IOException occurred");
            return;
        } catch (JsonSyntaxException jsonSyntaxException) {
            jsonSyntaxException.printStackTrace();
            System.err.println("JSON Syntax malformation: " + jsonSyntaxException.getMessage());
            return;
        }

        System.out.println(Ledger.toJson(ledger));
    }
}
