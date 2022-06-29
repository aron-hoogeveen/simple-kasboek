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

import ch.bolkhuis.kasboek.core.Transaction;
import ch.bolkhuis.kasboek.gson.CustomizedGson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;

/**
 * @version v0.2-pre-alpha
 * @author Aron Hoogeveen
 */
public class TransactionTypeAdapter extends TypeAdapter<Transaction> {
    private enum FieldNames {
        ID("id"),
        DATE("date"),
        DEBTOR_ID("debtor_id"),
        CREDITOR_ID("creditor_id"),
        RECEIPT_ID("receipt_id"),
        AMOUNT("amount"),
        DESCRIPTION("description");

        private final String name;

        FieldNames(String name) {
            this.name = name;
        }

        /**
         * Returns the ordinal + 1 for use in ORing.
         *
         * @return value greater than zero
         */
        private int getValue() {
            return ordinal() + 1;
        }

//        public String getName() {
//            return name;
//        }
    }

    @Override
    public void write(JsonWriter jsonWriter, Transaction transaction) throws IOException {
        if (transaction == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.beginObject();
        jsonWriter.name(FieldNames.ID.name);
        jsonWriter.value(transaction.getId());
        jsonWriter.name(FieldNames.DATE.name);
        CustomizedGson.gson.getAdapter(LocalDate.class).write(jsonWriter, transaction.getDate());
        jsonWriter.name(FieldNames.DEBTOR_ID.name);
        jsonWriter.value(transaction.getDebtorId());
        jsonWriter.name(FieldNames.CREDITOR_ID.name);
        jsonWriter.value(transaction.getCreditorId());
        jsonWriter.name(FieldNames.AMOUNT.name);
        jsonWriter.value(transaction.getAmount());
        jsonWriter.name(FieldNames.RECEIPT_ID.name);
        if (transaction.getReceiptId() == null)
            jsonWriter.nullValue();
        else
            jsonWriter.value(transaction.getReceiptId());
        jsonWriter.name(FieldNames.DESCRIPTION.name);
        jsonWriter.value(transaction.getDescription());
        jsonWriter.endObject();
    }

    @Override
    public Transaction read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        Transaction transaction;
        int id = 0;
        LocalDate date = null;
        int debtorId = 0;
        int creditorId = 0;
        Integer receiptId = null; // could be absent, so defaults to null
        double amount = 0;
        String description = null;

        jsonReader.beginObject();
        int fields = 0;
        while (jsonReader.hasNext()) {
            if (jsonReader.peek() == JsonToken.NAME) {
                String fieldName = jsonReader.nextName();
                if (fieldName.equals(FieldNames.ID.name)) {
                    fields |= FieldNames.ID.getValue();
                    id = jsonReader.nextInt();
                }
                else if (fieldName.equals(FieldNames.DATE.name)) {
                    fields |= FieldNames.DATE.getValue();
                    date = CustomizedGson.gson.getAdapter(LocalDate.class).read(jsonReader);
                }
                else if (fieldName.equals(FieldNames.DEBTOR_ID.name)) {
                    fields |= FieldNames.DEBTOR_ID.getValue();
                    debtorId = jsonReader.nextInt();
                }
                else if (fieldName.equals(FieldNames.CREDITOR_ID.name)) {
                    fields |= FieldNames.CREDITOR_ID.getValue();
                    creditorId = jsonReader.nextInt();
                }
                else if (fieldName.equals(FieldNames.AMOUNT.name)) {
                    fields |= FieldNames.AMOUNT.getValue();
                    amount = jsonReader.nextDouble();
                }
                else if (fieldName.equals(FieldNames.DESCRIPTION.name)) {
                    fields |= FieldNames.DESCRIPTION.getValue();
                    description = jsonReader.nextString();
                }
                else if (fieldName.equals(FieldNames.RECEIPT_ID.name)) {
                    fields |= FieldNames.RECEIPT_ID.getValue();
                    if (jsonReader.peek() == JsonToken.NULL) {
                        jsonReader.nextNull();
                    }
                    else
                        receiptId = jsonReader.nextInt();
                }
                else {
                    jsonReader.skipValue();
                }
            }
            else if (jsonReader.peek() == JsonToken.END_OBJECT) {
                // The following values will not be part of this Transaction
                break;
            }
            else {
                // unrecognized field
                throw new IOException("Did not expect token " + jsonReader.peek());
            }
        }
        jsonReader.endObject();

        // It is ok if no receiptId is read, so a little hack on the next line
        fields |= FieldNames.RECEIPT_ID.getValue();

        // only construct if all fields are available
        int fieldCheck = 0;
        for (FieldNames fieldName : FieldNames.values()) {
            fieldCheck |= fieldName.getValue();
        }
        if (fields == fieldCheck) {
            // construct Transaction
            return (receiptId == null) ? new Transaction(id, debtorId, creditorId, amount, date, description) :
                    new Transaction(id, debtorId, creditorId, amount, receiptId, date, description);
        }
        throw new IOException("Not all required fields are available");
    }
}
