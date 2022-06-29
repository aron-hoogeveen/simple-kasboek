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
public class AccountingEntityTypeAdapter extends TypeAdapter<AccountingEntity> {
    private enum FieldNames {
        ID("id"),
        NAME("name"),
        ACCOUNT_TYPE("account_type"),
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
    public void write(JsonWriter jsonWriter, AccountingEntity accountingEntity) throws IOException {
        if (accountingEntity == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.beginObject();
        jsonWriter.name(FieldNames.ID.name).value(accountingEntity.getId());
        jsonWriter.name(FieldNames.NAME.name).value(accountingEntity.getName());
        jsonWriter.name(FieldNames.ACCOUNT_TYPE.name);
        CustomizedGson.gson.getAdapter(AccountType.class).write(jsonWriter, accountingEntity.getAccountType());
        jsonWriter.name(FieldNames.BALANCE.name).value(accountingEntity.getBalance());
        jsonWriter.endObject();
    }

    @Override
    public AccountingEntity read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        int id = 0;
        String name = null;
        AccountType accountType = null;
        double balance = 0;
        double endBalance = 0;
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
                else if (fieldName.equals(FieldNames.ACCOUNT_TYPE.name)) {
                    fields |= FieldNames.ACCOUNT_TYPE.getValue();
                    accountType = CustomizedGson.gson.fromJson(jsonReader, AccountType.class);
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
            return new AccountingEntity(
                    id,
                    Objects.requireNonNull(name, "name should not be null at this point"),
                    Objects.requireNonNull(accountType, "accountType should not be null at this point"),
                    balance);
        }
        throw new IOException("Not all required fields are available");
    }
}
