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

import ch.bolkhuis.kasboek.core.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

/**
 * @version v0.2-pre-alpha
 * @author Aron Hoogeveen
 */
public class CustomizedGson {
    private final static GsonBuilder builder = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(Transaction.class, new TransactionTypeAdapter())
            .registerTypeAdapter(AccountingEntity.class, new AccountingEntityTypeAdapter())
            .registerTypeAdapter(ResidentEntity.class, new InmateEntityTypeAdapter())
            .registerTypeAdapter(HuischLedger.class, new HuischLedgerTypeAdapter())
            .registerTypeAdapter(Ledger.class, new LedgerTypeAdapter())
            .registerTypeAdapter(Receipt.class, new ReceiptTypeAdapter())
            .setPrettyPrinting(); // FIXME REMOVE THIS BEFORE PUBLISHING

    public final static Gson gson = builder.create();
}
