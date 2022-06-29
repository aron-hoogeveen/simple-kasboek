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

import ch.bolkhuis.kasboek.core.Receipt;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ReceiptTypeAdapter extends TypeAdapter<Receipt> {
    private enum FieldNames {
        ID("id"),
        NAME("name"),
        TRANSACTIONS_ID_SET("transaction_id_set"),
        DATE("date"),
        PAYER("payer");

        final String name;

        FieldNames(String name) {
            this.name = name;
        }

        public int getValue() {
            return ordinal() + 1;
        }
    }
    @Override
    public void write(JsonWriter jsonWriter, Receipt receipt) throws IOException {
        if (receipt == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name(FieldNames.ID.name);
        jsonWriter.value(receipt.getId());
        jsonWriter.name(FieldNames.NAME.name);
        jsonWriter.value(receipt.getName());
        jsonWriter.name(FieldNames.TRANSACTIONS_ID_SET.name);
        jsonWriter.beginArray();
        Set<Integer> transactionIdSet = receipt.getTransactionIdSet();
        if (transactionIdSet.size() > 0) {
            for (Integer i : transactionIdSet) {
                CustomizedGson.gson.getAdapter(Integer.class).write(jsonWriter, i);
            }
        }
        jsonWriter.endArray();
        jsonWriter.name(FieldNames.DATE.name);
        CustomizedGson.gson.getAdapter(LocalDate.class).write(jsonWriter, receipt.getDate());
        jsonWriter.name(FieldNames.PAYER.name);
        jsonWriter.value(receipt.getPayer());
        jsonWriter.endObject();
    }

    @Override
    public Receipt read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        int id = 0;
        String name = null;
        Set<Integer> transactionIdSet = new HashSet<>();
        LocalDate date = null;
        int payer = 0;

        jsonReader.beginObject();
        int fields = 0;
        while (jsonReader.hasNext()) {
            if (jsonReader.peek() == JsonToken.NAME) {
                String fieldName = jsonReader.nextName();
                if (fieldName.equals(FieldNames.ID.name)) {
                    fields |= FieldNames.ID.getValue();
                    id = jsonReader.nextInt();
                }
                else if (fieldName.equals(FieldNames.NAME.name)) {
                    fields |= FieldNames.NAME.getValue();
                    name = jsonReader.nextString();
                }
                else if (fieldName.equals(FieldNames.TRANSACTIONS_ID_SET.name)) {
                    fields |= FieldNames.TRANSACTIONS_ID_SET.getValue();
                    jsonReader.beginArray();
                    while(jsonReader.peek() != JsonToken.END_ARRAY) {
                        transactionIdSet.add(CustomizedGson.gson.getAdapter(Integer.class).read(jsonReader));
                    }
                    jsonReader.endArray();
                }
                else if (fieldName.equals(FieldNames.DATE.name)) {
                    fields |= FieldNames.DATE.getValue();
                    date = CustomizedGson.gson.getAdapter(LocalDate.class).read(jsonReader);
                }
                else if (fieldName.equals(FieldNames.PAYER.name)) {
                    fields |= FieldNames.PAYER.getValue();
                    payer = jsonReader.nextInt();
                }
                else {
                    jsonReader.skipValue();
                }
            }
        }
        jsonReader.endObject();

        int fieldCheck = 0;
        for (FieldNames fieldName : FieldNames.values()) {
            fieldCheck |= fieldName.getValue();
        }

        if (fieldCheck == fields) {
            return new Receipt(
                    id,
                    Objects.requireNonNull(name),
                    Objects.requireNonNull(transactionIdSet),
                    Objects.requireNonNull(date),
                    payer);
        }
        throw new IOException("Not all required fields are available");
    }
}
