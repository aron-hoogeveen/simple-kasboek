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
import ch.bolkhuis.kasboek.core.ResidentEntity;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Objects;

/**
 * @version v0.2-pre-alpha
 * @author Aron Hoogeveen
 */
public class InmateEntityTypeAdapter extends TypeAdapter<ResidentEntity> {
    private enum FieldNames {
        ID("id"),
        NAME("name"),
        PREVIOUS_BALANCE("previous_balance"),
        BALANCE("balance");

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
    }

    @Override
    public void write(JsonWriter jsonWriter, ResidentEntity residentEntity) throws IOException {
        if (residentEntity == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.beginObject();
        jsonWriter.name(FieldNames.ID.name).value(residentEntity.getId());
        jsonWriter.name(FieldNames.NAME.name).value(residentEntity.getName());
        jsonWriter.name(FieldNames.PREVIOUS_BALANCE.name).value(residentEntity.getPreviousBalance());
        jsonWriter.name(FieldNames.BALANCE.name).value(residentEntity.getBalance());
        jsonWriter.endObject();
    }

    @Override
    public ResidentEntity read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        int id = 0;
        String name = null;
        AccountType accountType = null;
        double previousBalance = 0;
        double balance = 0;
        // fields is used for checking if all fields are available
        int fields = 0;

        jsonReader.beginObject();
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
                else if (fieldName.equals(FieldNames.PREVIOUS_BALANCE.name)) {
                    fields |= FieldNames.PREVIOUS_BALANCE.getValue();
                    previousBalance = jsonReader.nextDouble();
                }
                else if (fieldName.equals(FieldNames.BALANCE.name)) {
                    fields |= FieldNames.BALANCE.getValue();
                    balance = jsonReader.nextDouble();
                }
                else {
                    // unknown field
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

        if (fields == fieldCheck) {
            return new ResidentEntity(
                    id,
                    Objects.requireNonNull(name, "name should not be null at this point"),
                    previousBalance,
                    balance);
        }
        throw new IOException("Not all required fields are available");
    }
}
