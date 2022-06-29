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
package ch.bolkhuis.kasboek.components;

import ch.bolkhuis.kasboek.ApplicationSceneRoot;
import ch.bolkhuis.kasboek.core.AccountingEntity;
import ch.bolkhuis.kasboek.core.Receipt;
import ch.bolkhuis.kasboek.core.Transaction;
import ch.bolkhuis.kasboek.dialog.TransactionDialog;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.*;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * TransactionTableView is an implementation of the TableView class for Transactions. This implementation does not support
 * null valued Transactions but does not implement any checks YET for this TODO implement null checks.
 *
 * TODO add option to show only transactions from a specific date span
 */
public class TransactionTableView extends TableView<Transaction> implements MapChangeListener<Integer, Transaction> {
    private final ObservableMap<Integer, Transaction> m_items;
    private final ObservableMap<Integer, AccountingEntity> m_entities;
    private final ObservableMap<Integer, Receipt> m_receipts;

    private final boolean hideReceiptColumn;

    private final ApplicationSceneRoot appSceneRoot;

    /**
     * Creates a default TableView control with no content.
     *
     * <p>Refer to the {@link TableView} class documentation for details on the
     * default state of other properties.
     */
    public TransactionTableView(@NotNull ApplicationSceneRoot appSceneRoot,
                                @NotNull ObservableMap<Integer, AccountingEntity> m_entities,
                                @NotNull ObservableMap<Integer, Receipt> m_receipts) {
        if (appSceneRoot == null) { throw new NullPointerException(); }
        if (m_entities == null) { throw new NullPointerException(); }
        if (m_receipts == null) { throw new NullPointerException(); }

        m_items = FXCollections.observableHashMap();
        this.m_entities = m_entities;
        this.m_receipts = m_receipts;
        this.hideReceiptColumn = false;
        this.appSceneRoot = appSceneRoot;

        setEditable(false); // disable editing in this table. Transactions are edited in a specific dialog presented to the user
        m_items.addListener(this);

        initColumns();
        initChildren();
    }

    /**
     * Creates a default TableView control with no content.
     *
     * <p>Refer to the {@link TableView} class documentation for details on the
     * default state of other properties.
     */
    public TransactionTableView(@NotNull ApplicationSceneRoot appSceneRoot,
                                @NotNull ObservableMap<Integer, AccountingEntity> m_entities,
                                @NotNull ObservableMap<Integer, Receipt> m_receipts,
                                boolean hideReceiptColumn) {
        if (appSceneRoot == null) { throw new NullPointerException(); }
        if (m_entities == null) { throw new NullPointerException(); }
        if (m_receipts == null) { throw new NullPointerException(); }

        m_items = FXCollections.observableHashMap();
        this.m_entities = m_entities;
        this.m_receipts = m_receipts;
        this.hideReceiptColumn = hideReceiptColumn;
        this.appSceneRoot = appSceneRoot;

        setEditable(false); // disable editing in this table. Transactions are edited in a specific dialog presented to the user
        m_items.addListener(this);

        initColumns();
        initChildren();
    }

    /**
     * Creates a TableView with the content provided in the items ObservableList.
     * This also sets up an observer such that any changes to the items list
     * will be immediately reflected in the TableView itself.
     *
     * <p>Refer to the {@link TableView} class documentation for details on the
     * default state of other properties.
     *
     * @param m_items The items to insert into the TableView, and the list to watch
     *              for changes (to automatically show in the TableView).
     */
    public TransactionTableView(@NotNull ApplicationSceneRoot appSceneRoot,
                                @NotNull ObservableMap<Integer, Transaction> m_items,
                                @NotNull ObservableMap<Integer, AccountingEntity> m_entities,
                                @NotNull ObservableMap<Integer, Receipt> m_receipts) {
        if (appSceneRoot == null) { throw new NullPointerException(); }
        if (m_items == null) { throw new NullPointerException(); }
        if (m_entities == null) { throw new NullPointerException(); }
        if (m_receipts == null) { throw new NullPointerException(); }
        this.m_items = m_items;
        this.m_entities = m_entities;
        this.m_receipts = m_receipts;
        this.hideReceiptColumn = false;
        this.appSceneRoot = appSceneRoot;

        setEditable(false); // disable editing in this table. Transactions are edited in a specific dialog presented to the user
        m_items.addListener(this);

        initColumns();
        initChildren();
    }

    /**
     * Creates a TableView with the content provided in the items ObservableList.
     * This also sets up an observer such that any changes to the items list
     * will be immediately reflected in the TableView itself.
     *
     * <p>Refer to the {@link TableView} class documentation for details on the
     * default state of other properties.
     *
     * @param m_items The items to insert into the TableView, and the list to watch
     *              for changes (to automatically show in the TableView).
     */
    public TransactionTableView(@NotNull ApplicationSceneRoot appSceneRoot,
                                @NotNull ObservableMap<Integer, Transaction> m_items,
                                @NotNull ObservableMap<Integer, AccountingEntity> m_entities,
                                @NotNull ObservableMap<Integer, Receipt> m_receipts,
                                boolean hideReceiptColumn) {
        if (appSceneRoot == null) { throw new NullPointerException(); }
        if (m_items == null) { throw new NullPointerException(); }
        if (m_entities == null) { throw new NullPointerException(); }
        if (m_receipts == null) { throw new NullPointerException(); }
        this.m_items = m_items;
        this.m_entities = m_entities;
        this.m_receipts = m_receipts;
        this.hideReceiptColumn = hideReceiptColumn;
        this.appSceneRoot = appSceneRoot;

        setEditable(false); // disable editing in this table. Transactions are edited in a specific dialog presented to the user
        m_items.addListener(this);

        initColumns();
        initChildren();
    }

    /**
     * Create and set the columns for this TableView.
     */
    private void initColumns() {
        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Datum");
        TableColumn<Transaction, String> receiptColumn = new TableColumn<>("Bonnetje");
        TableColumn<Transaction, String> debtorColumn = new TableColumn<>("Debtor");
        TableColumn<Transaction, String> creditorColumn = new TableColumn<>("Creditor");
        TableColumn<Transaction, String> amountColumn = new TableColumn<>("Bedrag");
        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Beschrijving");

        // Set a cell factory for adding a ContextMenu for deleting a Transaction
        setRowFactory(param -> new TransactionTableRow());

        dateColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getDate().toString()));
        receiptColumn.setCellValueFactory(param -> {
            Integer receiptId = param.getValue().getReceiptId();
            String value;
            if (receiptId == null) {
                value = null;
            } else {
                Receipt receipt = m_receipts.get(param.getValue().getReceiptId());
                if (receipt != null) {
                    value = " (" + receipt.getDate() + ")";
                    value = receipt.getName() + value;
                } else {
                    value = "#ERROR, no such receipt id";
                }
            }

            return new ReadOnlyStringWrapper(value);
        });
        debtorColumn.setCellValueFactory(param -> m_entities.get(param.getValue().getDebtorId()).nameProperty());
        creditorColumn.setCellValueFactory(param -> m_entities.get(param.getValue().getCreditorId()).nameProperty());
        amountColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(NumberFormat.getCurrencyInstance(Locale.GERMANY).format(
                param.getValue().getAmount()
        )));
        descriptionColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getDescription()));

        if (hideReceiptColumn) {
            getColumns().setAll(
                    dateColumn,
                    debtorColumn,
                    creditorColumn,
                    amountColumn,
                    descriptionColumn
            );
        } else {
            getColumns().setAll(
                    dateColumn,
                    receiptColumn,
                    debtorColumn,
                    creditorColumn,
                    amountColumn,
                    descriptionColumn
            );
        }
    }

    /**
     * Initialises the backing {@code items} ObservableList from the values of the backing ObservableMap {@code m_items}.
     * This method is called by all constructors.
     */
    private void initChildren() {
        getItems().setAll(m_items.values()); // clears the ObservableList and then adds all Transactions if any
    }

    /**
     * Called after a change has been made to an ObservableMap.
     * This method is called on every elementary change (put/remove) once.
     * This means, complex changes like keySet().removeAll(Collection) or clear()
     * may result in more than one call of onChanged method.
     *
     * @param change the change that was made
     */
    @Override
    public void onChanged(Change<? extends Integer, ? extends Transaction> change) {
        // update the backing ObservableList items
        if (change.wasAdded()) {
            getItems().add(change.getValueAdded());
        }
        if (change.wasRemoved()){
            getItems().remove(change.getValueRemoved());
        }
    }

    private class TransactionTableRow extends TableRow<Transaction> {
        /**
         * The updateItem method should not be called by developers, but it is the
         * best method for developers to override to allow for them to customise the
         * visuals of the cell. To clarify, developers should never call this method
         * in their code (they should leave it up to the UI control, such as the
         * {@link ListView} control) to call this method. However,
         * the purpose of having the updateItem method is so that developers, when
         * specifying custom cell factories (again, like the ListView
         * {@link ListView#cellFactoryProperty() cell factory}),
         * the updateItem method can be overridden to allow for complete customisation
         * of the cell.
         *
         * <p>It is <strong>very important</strong> that subclasses
         * of Cell override the updateItem method properly, as failure to do so will
         * lead to issues such as blank cells or cells with unexpected content
         * appearing within them. Here is an example of how to properly override the
         * updateItem method:
         *
         * <pre>
         * protected void updateItem(T item, boolean empty) {
         *     super.updateItem(item, empty);
         *
         *     if (empty || item == null) {
         *         setText(null);
         *         setGraphic(null);
         *     } else {
         *         setText(item.toString());
         *     }
         * }
         * </pre>
         *
         * <p>Note in this code sample two important points:
         * <ol>
         *     <li>We call the super.updateItem(T, boolean) method. If this is not
         *     done, the item and empty properties are not correctly set, and you are
         *     likely to end up with graphical issues.</li>
         *     <li>We test for the <code>empty</code> condition, and if true, we
         *     set the text and graphic properties to null. If we do not do this,
         *     it is almost guaranteed that end users will see graphical artifacts
         *     in cells unexpectedly.</li>
         * </ol>
         *  @param item The new item for the cell.
         *
         * @param empty whether or not this cell represents data from the list. If it
         *              is empty, then it does not represent any domain data, but is a cell
         */
        @Override
        protected void updateItem(Transaction item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                MenuItem editItem = new MenuItem("Bewerken");
                MenuItem deleteItem = new MenuItem("Verwijderen");

                editItem.setOnAction(event -> {
                    TransactionDialog transactionDialog = new TransactionDialog(
                            getScene().getWindow(),
                            m_entities,
                            m_receipts,
                            item
                    );
                    Optional<Transaction> result = transactionDialog.showAndWait();
                    if (result.isPresent()) {
                        System.out.println("Transaction edited in Dialog. Not saved.");
                    }
                });
                deleteItem.setOnAction(event -> {
                    ButtonType yesType = new ButtonType("Verwijderen", ButtonBar.ButtonData.YES);
                    ButtonType noType = new ButtonType("Annuleren", ButtonBar.ButtonData.NO);
                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.getDialogPane().getButtonTypes().addAll(
                            noType,
                            yesType
                    );
                    dialog.setTitle("Transactie verwijderen");
                    dialog.setHeaderText("Transactie \"" + item.getDescription() + "\" verwijderen?");
                    dialog.setGraphic(null);
                    // set initial selected button
                    ((Button)dialog.getDialogPane().lookupButton(yesType)).setDefaultButton(false);
                    ((Button)dialog.getDialogPane().lookupButton(noType)).setDefaultButton(true);
                    Optional<ButtonType> result = dialog.showAndWait();

                    if (result.isPresent()) {
                        ButtonType resultButtonType = result.get();
                        if (resultButtonType.getButtonData().equals(ButtonBar.ButtonData.YES)) {
                            // delete transaction
                            if (appSceneRoot.getHuischLedger().removeTransaction(item) == null) {
                                System.err.println("The transaction could not be found and could therefore not be removed");
                            }
                        }
                        // do not delete the transaction
                    }
                });

                ContextMenu contextMenu = new ContextMenu(
                        editItem,
                        deleteItem
                );

                setContextMenu(contextMenu);
            } else {
                setContextMenu(null);
            }
        }
    }

}
