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

import ch.bolkhuis.kasboek.core.AccountingEntity;
import ch.bolkhuis.kasboek.core.ResidentEntity;
import ch.bolkhuis.kasboek.core.Ledger;
import ch.bolkhuis.kasboek.core.Transaction;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import javafx.collections.FXCollections;

import java.io.IOException;
import java.util.*;

/**
 * @version v0.2-pre-alpha
 * @author Aron Hoogeveen
 */
public class LedgerTypeAdapter extends TypeAdapter<Ledger> {
    private enum FieldNames {
        TRANSACTIONS("transactions"),
        ACCOUNTING_ENTITIES("accounting_entities");

        final String name;

        FieldNames(String name) {
            this.name = name;
        }

        public int getValue() {
            return ordinal() + 1;
        }
    }

    @Override
    public void write(JsonWriter jsonWriter, Ledger ledger) throws IOException {
        if (ledger == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name(FieldNames.TRANSACTIONS.name);
        jsonWriter.beginArray();
        Map<Integer, Transaction> transactionSortedMap = ledger.getTransactions();
        if (transactionSortedMap.size() > 0) {
            for (Transaction t : transactionSortedMap.values()) {
                CustomizedGson.gson.getAdapter(Transaction.class).write(jsonWriter, t);
            }
        }
        jsonWriter.endArray();
        jsonWriter.name(FieldNames.ACCOUNTING_ENTITIES.name);
        jsonWriter.beginArray();
        Map<Integer, AccountingEntity> entries = ledger.getAccountingEntities();
        if (entries.size() > 0) {
            for (AccountingEntity a : entries.values()) {
                // Safe the type in order to be able to correctly deserialize
                if (a instanceof ResidentEntity) {
                    jsonWriter.beginObject();
                    jsonWriter.name("type");
                    jsonWriter.value(ResidentEntity.class.getCanonicalName());
                    jsonWriter.name("object");
                    CustomizedGson.gson.getAdapter(ResidentEntity.class).write(jsonWriter, (ResidentEntity)a);
                    jsonWriter.endObject();
                } else {
                    jsonWriter.beginObject();
                    jsonWriter.name("type");
                    jsonWriter.value(AccountingEntity.class.getCanonicalName());
                    jsonWriter.name("object");
                    CustomizedGson.gson.getAdapter(AccountingEntity.class).write(jsonWriter, a);
                    jsonWriter.endObject();
                }
            }
        }
        jsonWriter.endArray();
        jsonWriter.endObject();
    }

    @Override
    public Ledger read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        HashMap<Integer, Transaction> transactions = new HashMap<>();
        HashMap<Integer, AccountingEntity> accountingEntities = new HashMap<>();

        jsonReader.beginObject();
        int fields = 0;
        while (jsonReader.hasNext()) {
            if (jsonReader.peek() == JsonToken.NAME) {
                String fieldName = jsonReader.nextName();
                if (fieldName.equals(FieldNames.TRANSACTIONS.name)) {
                    fields |= FieldNames.TRANSACTIONS.getValue();
                    jsonReader.beginArray();
                    while (jsonReader.peek() != JsonToken.END_ARRAY) {
                        Transaction transaction = CustomizedGson.gson.fromJson(jsonReader, Transaction.class);
                        if (transactions.put(Objects.requireNonNull(transaction).getId(), transaction) != null) {
                            throw new IOException("Transactions with the same id are not allowed");
                        }
                    }
                    jsonReader.endArray();
                }
                else if (fieldName.equals(FieldNames.ACCOUNTING_ENTITIES.name)) {
                    fields |= FieldNames.ACCOUNTING_ENTITIES.getValue();
                    jsonReader.beginArray();
                    while (jsonReader.peek() != JsonToken.END_ARRAY) {
                        jsonReader.beginObject();
                        if (!jsonReader.nextName().equals("type")) {
                            throw new IOException("missing required field 'type' for AccountingEntity");
                        }
                        String canonicalName = jsonReader.nextString();
                        AccountingEntity entity;
                        if (canonicalName.equals(AccountingEntity.class.getCanonicalName())) {
                            if (!jsonReader.nextName().equals("object")) {
                                throw new IOException("missing required field 'object' for AccountingEntity");
                            }
                            entity = CustomizedGson.gson.fromJson(jsonReader, AccountingEntity.class);
                        }
                        else if (canonicalName.equals(ResidentEntity.class.getCanonicalName())) {
                            if (!jsonReader.nextName().equals("object")) {
                                throw new IOException("missing required field 'object' for AccountingEntity");
                            }
                            entity = CustomizedGson.gson.fromJson(jsonReader, ResidentEntity.class);
                        }
                        else {
                            throw new IOException("type '" + canonicalName + "' not recognized");
                        }

                        // Do not allow overwriting of AccountingEntities
                        if (accountingEntities.put(Objects.requireNonNull(entity, "parsed entity cannot be null").getId(), entity) != null) {
                            throw new IOException("AccountingEntities with the same id are not allowed");
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();
                }
                else {
                    // unrecognized JsonToken
                    jsonReader.skipValue();
                }
            }
            else if (jsonReader.peek() == JsonToken.END_OBJECT) {
                break;
            }
            else {
                throw new IOException("Did not expect token " + jsonReader.peek());
            }
        }
        jsonReader.endObject();

        int fieldCheck = 0;
        for (FieldNames fieldName : FieldNames.values()) {
            fieldCheck |= fieldName.getValue();
        }

        if (fieldCheck == fields) {
            return new Ledger(FXCollections.observableMap(accountingEntities), FXCollections.observableMap(transactions));
        }
        throw new IOException("Not all required fields are available");
    }
}
