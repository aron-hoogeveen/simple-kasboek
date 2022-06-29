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

import ch.bolkhuis.kasboek.exceptions.IllegalTemplateFormatException;
import ch.bolkhuis.kasboek.exceptions.UnsupportedVersionException;
import ch.bolkhuis.kasboek.gson.CustomizedGson;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * Class HuischLedger is a Ledger that has extra functionality to meet the demands of the Bolkhuisch. AccountingEntities
 * with negative ids are reserved by this class. It is possible to create AccountingEntities with negative ids, but if
 * that id collides with one of the reserved ids of this class that AccountingEntity will be overwritten at some point
 * without giving feedback.
 * This class must not add objects directly to the ObservableMaps of its parent class. Great care should be taken when
 * doing this when still doing this.
 */
public final class HuischLedger extends Ledger {
    private static final int placeholderEntityId = -1;
    private static final String templateVersionString = "<!-- version:1 -->";

    private final ObservableMap<Integer, Receipt> receipts;
    private int nextReceiptId = 0;

    /**
     * Default constructor
     */
    public HuischLedger() {
        this.receipts = FXCollections.observableHashMap();
    }

    /**
     * Create a copy of the old HuischLedger.
     *
     * @param old the HuischLedger to make a copy of
     */
    public HuischLedger(@NotNull HuischLedger old) {
        super(old);
        this.receipts = FXCollections.observableMap(old.receipts);
        this.nextReceiptId = old.nextReceiptId;
    }

    /**
     * Constructs a HuischLedger with initial AccountingEntities map
     *
     * @param accountingEntities the initial AccountingEntities
     */
    public HuischLedger(@NotNull ObservableMap<Integer, AccountingEntity> accountingEntities) {
        super(accountingEntities);
        this.receipts = FXCollections.observableHashMap();
    }

    /**
     * Constructs a HuischLedger with initial AccountingEntities map and Transactions map.
     *
     * @param accountingEntities the initial AccountingEntities
     * @param transactions the initial Transactions
     */
    public HuischLedger(@NotNull ObservableMap<Integer, AccountingEntity> accountingEntities,
                        @NotNull ObservableMap<Integer, Transaction> transactions) {
        super(accountingEntities, transactions);
        this.receipts = FXCollections.observableHashMap();
    }

    /**
     * Constructs a HuischLedger with initial AccountingEntities, Transactions and Receipts map.
     *
     * @param accountingEntities the initial AccountingEntities
     * @param transactions the initial Transactions
     * @param receipts the initial Receipts
     */
    public HuischLedger(@NotNull ObservableMap<Integer, AccountingEntity> accountingEntities,
                        @NotNull ObservableMap<Integer, Transaction> transactions,
                        @NotNull ObservableMap<Integer, Receipt> receipts) {
        super(accountingEntities, transactions);

        // Check if all receipts are correct
        for (Receipt receipt : receipts.values()) {
            // check for existing transactions
            for (int transactionId : receipt.getTransactionIdSet()) {
                if (!this.transactions.containsKey(transactionId)) {
                    throw new IllegalArgumentException("Receipt (id:" + receipt.getId() + ") contains a reference to " +
                            "transaction (id:" + transactionId + ") that does not exist");
                }
            }

            // update the nextReceiptId
            nextReceiptId = (receipt.getId() >= nextReceiptId) ? receipt.getId() + 1 : nextReceiptId;
        }
        this.receipts = Objects.requireNonNull(receipts);
    }

    /**
     * Generates an invoice for an ResidentEntity with id {@code inmateEntityId} and writes it to the file {@code out}.
     * Transactions that belong to a Receipt are grouped together and shown as one on the Invoice. The counter-entity
     * for the Receipts will the denoted as "various" (or the locale different version from the provided resource file)
     * <br />
     * <br />
     * This method uses template version 1.
     *
     * @param out the output file
     * @param template the template for the invoice
     * @param huischLedger the huischLedger containing the AccountingEntity
     * @param residentEntityId the id of the AccountingEntity to generate an invoice for
     * @param resourceBundle the ResourceBundle for supplying Locale specific Strings
     * @throws IOException when there are problems reading/writing the Files
     * @throws UnsupportedVersionException when the template version is not supported
     */
    private static void generateResidentInvoice(@NotNull File out, @NotNull File template, @NotNull HuischLedger huischLedger,
                                                int residentEntityId, @NotNull LocalDate from,
                                                @NotNull LocalDate to, @NotNull ResourceBundle resourceBundle,
                                                @NotNull String introText)
            throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(template);
        Objects.requireNonNull(huischLedger);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(resourceBundle);

        // Check that the provided inmateEntityId matches an AccountingEntity in the Ledger
        AccountingEntity accountingEntity = huischLedger.getAccountingEntityById(residentEntityId);
        if (accountingEntity == null) {
            throw new IllegalArgumentException("The provided residentEntityId has not corresponding AccountingEntity in the provided Ledger");
        }

        // Check that the provided Entity is an ResidentEntity
        if (!(accountingEntity instanceof ResidentEntity)) {
            throw new IllegalArgumentException("The provided residentEntityId does not belong to an inmate");
        }
        ResidentEntity residentEntity = (ResidentEntity) accountingEntity;
        // Get the end_balance after processing al selected transactions
        double endBalance = residentEntity.getPreviousBalance();

        // Update the string for the placeholderEntity
        // FIXME think of a different design for the placeholderentity
        huischLedger.accountingEntities.put(
                placeholderEntityId,
                new PlaceholderEntity(placeholderEntityId, resourceBundle.getString("various"))
        );

        // TODO change the used currency based on the provided Locale
        Currency currency = Currency.getInstance("EUR");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.GERMAN);
        numberFormat.setCurrency(currency);

        String templateString;
        // Read in the template
        try (BufferedReader reader = new BufferedReader(new FileReader(template))) {
            // make sure compatible versions are being provided
            String fileVersion = reader.readLine();
            if (!fileVersion.equals(templateVersionString)) {
                throw new UnsupportedVersionException("Version \"" + fileVersion + "\" is not supported");
            }

            StringBuilder templateStringBuilder = new StringBuilder();
            reader.lines().forEach((s) -> templateStringBuilder.append(s).append("\n"));
            templateString = templateStringBuilder.toString();

            // Make sure all fields are in the template
            List<String> fields = List.of(
                    "${name}",
                    "${intro_text}",
                    "${start_balance}",
                    "${end_balance}",
                    "${table_data}"
            );
            String finalTemplateString = templateString;
            fields.forEach(e -> {
                if (!finalTemplateString.contains(e)) {
                    throw new IllegalTemplateFormatException("Field '" + e + "' is missing from the provided template file");
                }
                if (StringUtils.countMatches(finalTemplateString, e) > 1) {
                    throw new IllegalTemplateFormatException("Field '" + e + "' is declared too many times");
                }
            });

            // Get all transactions
            Set<Transaction> standAloneTransactionsSet = huischLedger.getTransactionsOf(residentEntityId, from, to);
            Set<Transaction> standAloneTransactionsSetCopy = new HashSet<>(standAloneTransactionsSet);
//            TreeMap<Integer, Transaction> copyOfStandAloneTransactions = new TreeMap<>(standAloneTransactions);

            // Separate the Transactions that belong to a Receipt from the stand-alone Transactions
            TreeMap<Integer, List<Transaction>> receiptIdToTransactionList = new TreeMap<>();
            // loop through all existing receipts (do not loop all transactions and just assume that the corresponding receipt does exist)
            standAloneTransactionsSetCopy.forEach(transaction -> {
                huischLedger.receipts.forEach((r_id, receipt) -> {
                    // If there is no list of transactions for this receipt, initialize an empty list
                    receiptIdToTransactionList.computeIfAbsent(r_id, k -> new ArrayList<>());
                    if (receipt.getTransactionIdSet().contains(transaction.getId())) {
                        // Add the transaction to its correct receiptIdToTransactionList mapping
                        receiptIdToTransactionList.computeIfPresent(r_id, (integer, transactions) -> {
                            // Removes the transaction from the stand-alone list and adds it to its corresponding receipt
                            standAloneTransactionsSet.remove(transaction);
                            transactions.add(transaction);
                            return transactions;
                        });
                    }
                });
            });

            // Create stand-alone transactions for the grouped transactions per receipt
            receiptIdToTransactionList.forEach((r_id, transactions) -> {
                // FIXME make receipts getting more efficient by passing it higher up the method maybe?
                Receipt receipt = huischLedger.receipts.get(r_id);
                // Only create the representing transaction if the current accountingEntity is the payer
                if (receipt.getPayer() == accountingEntity.getId()) {
                    // create a stand-alone Transaction that resembles the result of all the transactions
                    // Calculate the resulting balance change
                    double balanceChange = 0;
                    for (Transaction t : transactions) {
                        if (t.getDebtorId() == accountingEntity.getId()) {
                            balanceChange += accountingEntity.debitBalanceChange(t.getAmount());
                        } else {
                            assert accountingEntity.getId() == t.getCreditorId() : "We should only have transactions that are connected to the accountinEntity";
                            // creditor
                            balanceChange += accountingEntity.creditBalanceChange(t.getAmount());
                        }
                    }

                    // fix the reversing of the amount on the invoice later on at the amountString
                    int debtorId, creditorId;
                    if (balanceChange < 0 && accountingEntity.accountType.isDebit()) {
                        debtorId = accountingEntity.getId();
                        creditorId = placeholderEntityId;
                    } else {
                        debtorId = placeholderEntityId;
                        creditorId = accountingEntity.getId();
                    }


                    // Create the transaction representing the receipt and add it to the stand-alone transactions
                    standAloneTransactionsSet.add(
                            new Transaction(
                                    -666,
                                    debtorId,
                                    creditorId,
                                    balanceChange,
                                    receipt.getDate(),
                                    receipt.getName()));
                }
            });

            // Generate the TABLE_DATA from the transactions
            StringBuilder tableDateStringBuilder = new StringBuilder();
            // Get the end_balance after processing al selected transactions
            List<Transaction> standAloneTransactionsList = new ArrayList<>(standAloneTransactionsSet);
            Collections.sort(standAloneTransactionsList);
            for (Transaction t : standAloneTransactionsList) {
                if (t == null) { return; }
                // Get the counter-entity.
                int counterEntityId = (t.getDebtorId() == residentEntityId) ? t.getCreditorId() : t.getDebtorId();
                AccountingEntity counterEntity = huischLedger.getAccountingEntityById(counterEntityId);

                // Use an empty string if the counterEntity is not available
                String counterEntityName = (counterEntity == null) ? "" : counterEntity.getName();

                // Format the amount (amount is larger than or equal to zero)
                double amount = t.getAmount();
                String amountString;

                if ((residentEntity.getAccountType().isDebit() && t.getDebtorId() == residentEntityId)
                        || (!residentEntity.getAccountType().isDebit() && t.getCreditorId() == residentEntityId)) {
                    amountString = numberFormat.format(amount);
                    endBalance += amount;
                } else {
                    amountString = numberFormat.format(-1 * amount);
                    endBalance -= amount;
                }

                // Add a table row for this transaction
                tableDateStringBuilder
                        .append("\t\t<tr>\n")
                        .append("\t\t\t<td>\n")
                        .append("\t\t\t\t")
                        .append(t.getDateString())
                        .append("\n\t\t\t</td>\n")
                        .append("\t\t\t<td>\n")
                        .append("\t\t\t\t")
                        .append(counterEntityName)
                        .append("\n\t\t\t</td>\n")
                        .append("\t\t\t<td>\n")
                        .append("\t\t\t\t")
                        .append(t.getDescription())
                        .append("\n\t\t\t</td>\n")
                        .append("\t\t\t<td>\n")
                        .append("\t\t\t\t")
                        .append(amountString)
                        .append("\n\t\t\t</td>\n")
                        .append("\t\t</tr>\n");
            }

            // parse any placeholders in the introtext
            introText = StringUtils.replace(introText, "${start_date}", from.toString());
            introText = StringUtils.replace(introText, "${end_date}", to.toString());

            // Populate field ${NAME}
            templateString = StringUtils.replace(templateString, "${name}", residentEntity.getName());
            templateString = StringUtils.replace(templateString, "${intro_text}", introText);
            templateString = StringUtils.replace(templateString, "${start_balance}", numberFormat.format(residentEntity.getPreviousBalance()));
            templateString = StringUtils.replace(templateString, "${end_balance}", numberFormat.format(endBalance));
            templateString = StringUtils.replace(templateString, "${table_data}", tableDateStringBuilder.toString());
        }

        // Write to the out file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(out))) {
            writer.write(templateString);
        } finally {
            // if creating the invoice succeeded, update the previous_balance
            huischLedger.updateAccountingEntity(residentEntity.setPreviousBalance(endBalance));
        }
    }

    /**
     * Creates an invoice using the default ResourceBundle of this class ( ).
     *
     * @param out the output file
     * @param template the template for the invoice
     * @param ledger the ledger containing the AccountingEntity
     * @param inmateEntityId the id of the AccountingEntity to generate an invoice for
     * @throws IOException when there are problems reading/writing the Files
     * @throws UnsupportedVersionException when the template version is not supported
     */
    public static void generateResidentInvoice(@NotNull File out, @NotNull File template, @NotNull HuischLedger ledger,
                                               int inmateEntityId, @NotNull LocalDate from, @NotNull LocalDate to,
                                               @NotNull String introText) throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(template);
        Objects.requireNonNull(ledger);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(introText);

        ResourceBundle defaultInvoiceResourceBundle = ResourceBundle.getBundle("HuischInvoiceStrings");
        generateResidentInvoice(out, template, ledger, inmateEntityId, from, to, defaultInvoiceResourceBundle, introText);
    }

    /**
     * Creates an invoice using the supplied {@code locale} to select the appropriate ResourceBundle
     * @param out the output file
     * @param template the template for the invoice
     * @param ledger the ledger containing the AccountingEntity
     * @param inmateEntityId the id of the AccountingEntity to generate an invoice for
     * @param locale the Locale to use
     * @throws IOException when there are problems reading/writing the Files
     * @throws UnsupportedVersionException when the template version is not supported
     * @throws MissingResourceException when no resource if found for the requested Locale
     */
    public static void generateResidentInvoice(@NotNull File out, @NotNull File template, @NotNull HuischLedger ledger,
                                               int inmateEntityId, @NotNull LocalDate from,
                                               @NotNull LocalDate to, @NotNull String introText,
                                               @NotNull Locale locale) throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(template);
        Objects.requireNonNull(ledger);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(introText);
        Objects.requireNonNull(locale);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("HuischInvoiceStrings", locale);
        generateResidentInvoice(out, template, ledger, inmateEntityId, from, to, resourceBundle, introText);
    }

    public @Deprecated TreeMap<Integer, Receipt> copyOfReceipts() {
        return new TreeMap<>(receipts);
    }

    /**
     * Returns an unmodifiable version of the ObservableMap of Receipts.
     *
     * @return unmodifiable map of Receipts
     */
    public ObservableMap<Integer, Receipt> getReceipts() {
        return FXCollections.unmodifiableObservableMap(receipts);
    }

    public int getAndIncrementNextReceiptId() {
        return nextReceiptId++;
    }

    /**
     * This method should never be used. It may make this HuischLedger unusable.
     *
     * @param id the value for nextReceiptId
     */
    public void setNextReceiptId(int id) {
        nextReceiptId = id;
    }

    /**
     * Add a Receipt to the map of Receipts. If the Receipt contains a non-empty set of Transaction ids, the corresponding
     * Transactions are checked for having the correct receiptId value. If one or more of those transactions do have a
     * receiptId set, but not the correct one, no Transaction is modified, and this method will throw an
     * IllegalArgumentException. This method also throws an IllegalArgumentException when there is already a Receipt with
     * the same id. This method is not save for asynchronous use.
     * @param receipt the Receipt to add
     * @throws IllegalArgumentException when the receipt is incorrect
     */
    public void addReceipt(@NotNull Receipt receipt) {
        Objects.requireNonNull(receipt);

        if (receipts.containsKey(receipt.getId())) {
            throw new IllegalArgumentException("There exists already a receipt with that ID");
        }

        // payer should exist
        if (!accountingEntities.containsKey(receipt.getPayer())) {
            throw new IllegalArgumentException("Payer must exist");
        }

        // Validate all transactions
        Set<Integer> transactionIdSet = new HashSet<>(); // set of transactions that do not have a reference to this receipt
        for (int i : receipt.getTransactionIdSet()) {
            if (transactions.containsKey(i)) {
                if (transactions.get(i).getReceiptId() != null) {
                    if (transactions.get(i).getReceiptId() != receipt.getId()) {
                        throw new IllegalArgumentException("This Receipt refers to a Transaction that says it belongs to a " +
                                "different Receipt");
                    }
                    transactionIdSet.add(i);
                }
            } else {
                throw new IllegalArgumentException("Receipt contains an unknown transaction id.");
            }
        }
        if (transactionIdSet.size() > 0) {
            // update the transaction
            // currently this is not supported
            throw new IllegalArgumentException("All transactions need to reference this Receipt");
        }

        receipts.put(receipt.getId(), receipt);
    }

    /**
     * Returns a JSON string that is parsable by {@link Ledger#fromJson(Reader)}.
     *
     * @param ledger the Ledger to convert to JSON
     * @return a JSON string representing {@code ledger}
     */
    public static String toJson(HuischLedger ledger) {
        return CustomizedGson.gson.toJson(ledger);
    }

    /**
     * Returns a new Ledger from a {@code reader} supplying a JSON String as generated by {@link Ledger#toJson(Ledger)}.
     *
     * @param reader the reader providing the JSON String
     * @return a Ledger as represented by the JSON String
     * @throws com.google.gson.JsonSyntaxException see GSON docs
     * @throws com.google.gson.JsonIOException see GSON docs
     */
    public static HuischLedger fromJson(@NotNull Reader reader) {
        Objects.requireNonNull(reader);

        BufferedReader bufferedReader = new BufferedReader(reader);
        return CustomizedGson.gson.fromJson(bufferedReader, HuischLedger.class);
    }

    @Override
    protected Transaction removeTransactionInternal(Transaction transaction) {
        Transaction removed = super.removeTransactionInternal(transaction);

        if (removed != null) {
            // If the transaction belonged to a Receipt, unregister it from that receipt
            if (removed.getReceiptId() != null) {
                Receipt receipt = receipts.get(removed.getReceiptId());
                if (receipt != null) {
                    receipt.unregisterTransaction(removed.getId());

                    // update the receipt
                    receipts.put(receipt.getId(), receipt);
                }
            }
        }

        return removed;
    }

    /**
     * Writes {@code ledger} to the provided {@code file}.
     *
     * @param file the file to write to
     * @param ledger the ledger that needs to be saved
     * @throws IOException when some IO exception occurs
     */
    public static void toFile(@NotNull File file, HuischLedger ledger) throws IOException {
        Objects.requireNonNull(file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HuischLedger.toJson(ledger));
        }
    }

    /**
     * Creates a new Ledger from a file as written by {@link Ledger#toFile(File, Ledger)}.
     *
     * @param file the file to read from
     * @return new Ledger as represented by the content of {@code file}
     * @throws IOException when some IO exception occurs
     */
    public static HuischLedger fromFile(@NotNull File file) throws IOException {
        Objects.requireNonNull(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return CustomizedGson.gson.fromJson(reader, HuischLedger.class);
        }
    }

    /**
     * Adds the MapChangeListener {@code listener} to the listeners of the accountingEntities.
     *
     * @param listener the MapChangeListener
     */
    public void addEntityListener(MapChangeListener<Integer, AccountingEntity> listener) {
        accountingEntities.addListener(listener);
    }

    /**
     * Removes the listener from the observable AccountingEntities.
     *
     * @param listener the listener to remove
     */
    public void removeEntityListener(MapChangeListener<Integer, AccountingEntity> listener) {
        accountingEntities.removeListener(listener);
    }

    /**
     * Adds the MapChangeListener {@code listener} to the listeners of the transactions.
     *
     * @param listener the MapChangeListener
     */
    public void addTransactionListener(MapChangeListener<Integer, Transaction> listener) {
        transactions.addListener(listener);
    }

    /**
     * Removes the listener from the observable Transactions.
     *
     * @param listener the listener to remove
     */
    public void removeTransactionListener(MapChangeListener<Integer, Transaction> listener) {
        transactions.removeListener(listener);
    }

    /**
     * Adds the MapChangeListener {@code listener} to the listeners of the receipts.
     *
     * @param listener the MapChangeListener
     */
    public void addReceiptListener(MapChangeListener<Integer, Receipt> listener) {
        receipts.addListener(listener);
    }

    /**
     * Removes the listener from the observable Receipts.
     *
     * @param listener the listener to remove
     */
    public void removeReceiptListener(MapChangeListener<Integer, Receipt> listener) {
        receipts.removeListener(listener);
    }

    @Override
    public void addTransaction(@NotNull Transaction transaction) {
        Objects.requireNonNull(transaction);

        // Check that the receipt exists
        if (transaction.getReceiptId() != null) {
            if (receipts.containsKey(transaction.getReceiptId())) {
                super.addTransaction(transaction);
                receipts.get(transaction.getReceiptId()).registerTransaction(transaction.getId());
            } else {
                throw new IllegalArgumentException("The transaction has a non-existent receiptId: (T.Id:" + transaction.getId() + ",T.rId:" + transaction.getReceiptId() + ")");
            }
        } else {
            super.addTransaction(transaction);
        }
    }

    /**
     * Removes the Receipt with id {@code receiptId}. Method {@link #removeReceiptAndRelatedTransactions(int)} also removes
     * any associated Transactions.
     *
     * @param receiptId the id of the Receipt to remove
     * @return the removed Receipt or {@code null} if there was no such Receipt
     */
    public Receipt removeReceipt(int receiptId) {
        return removeReceiptInternal(receiptId);
    }

    /**
     * Removes the provided Receipt from the saved Receipts if it exists. Method
     * {@link #removeReceiptAndRelatedTransactions(int)} also removes any associated Transactions.
     *
     * @param receipt the Receipt to remove
     * @return the removed Receipt or {@code null} if there was no such Receipt
     */
    public Receipt removeReceipt(Receipt receipt) {
        if (receipt == null) {
            return null;
        }

        Receipt r = receipts.get(receipt.getId());
        if (r != receipt) {
            return null; // the receipt with that ID is not equal to the provided receipt
        }

        return removeReceiptInternal(receipt.getId());
    }

    /**
     * Implementation of removing a receipt by receiptId. Removing a receipt will also cause any mentioned transactions
     * to be updated to not point to this receipt anymore. This does not guarantee that no transaction will still point to
     * this receiptId, for example when a transaction points to this receipt, but this receipt does not point to that
     * transaction.
     *
     * @param receiptId the id of the receipt to remove
     * @return the removed receipt
     */
    private Receipt removeReceiptInternal(int receiptId) {
        Receipt receipt = receipts.get(receiptId);
        if (receipt == null) {
            return null;
        }

        // the receipt exists. Remove all associations with the transactions
        for (int transactionId : receipt.getTransactionIdSet()) {
            Transaction transaction = transactions.get(transactionId);
            if (transaction != null) {
                // update the saved transaction
                transactions.put(transaction.getId(), transaction.clearReceiptId());
            }
        }

        return receipts.remove(receiptId);
    }

    /**
     * Removes the Receipt with id {@code receiptId} from the saved Receipts if it exists and furthermore also deletes
     * the corresponding Transactions.
     *
     * @param receiptId the id of the Receipt to remove
     * @throws Exception as long as this function is not implemented
     */
    public void removeReceiptAndRelatedTransactions(int receiptId) throws Exception {
        throw new Exception("Not yet implemented");
    }

    /**
     * Removes the provided Receipt from the saved Receipts if it exists and furthermore also deletes
     * the corresponding Transactions.
     *
     * @param receipt the Receipt to remove
     * @throws Exception as long as this function is not implemented
     */
    public void removeReceiptAndRelatedTransactions(Receipt receipt) throws Exception {
        throw new Exception("Not yet implemented");
    }

    public int getNextReceiptId() {
        return nextReceiptId;
    }
}
