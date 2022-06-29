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
package ch.bolkhuis.kasboek.dialog;

import ch.bolkhuis.kasboek.App;
import ch.bolkhuis.kasboek.ApplicationSceneRoot;
import ch.bolkhuis.kasboek.components.TransactionTableView;
import ch.bolkhuis.kasboek.core.AccountingEntity;
import ch.bolkhuis.kasboek.core.HuischLedger;
import ch.bolkhuis.kasboek.core.Receipt;
import ch.bolkhuis.kasboek.core.Transaction;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ViewReceiptDialog is a "Dialog" for viewing and editing a {@link Receipt}.
 *
 * @author Aron Hoogeveen
 */
public class ViewReceiptDialog extends AbstractDialog<Receipt> implements SetChangeListener<Integer> {
    private final ObservableMap<Integer, Transaction> transactionObservableMap = FXCollections.observableHashMap();
    private final ApplicationSceneRoot appSceneRoot;

    // Nodes
    private final Label nameLabel = new Label();
    private final TextField nameTextField = new TextField();
    private final Label dateLabel = new Label();
    private final DatePicker datePicker = new DatePicker();
    private final Label payerLabel = new Label();
    private final SearchableComboBox<AccountingEntity> payerComboBox = new SearchableComboBox<>();
    private final Label transactionsLabel = new Label();
    private final TransactionTableView transactionTableView;
    private final Button addTransactionButton = new Button();
    private final Button importTransactionButton = new Button();

    /**
     * Creates a new AbstractDialog and initialises its owner and the old T to load.
     *
     * @param owner owner to be used for the stage
     * @param appSceneRoot the ApplicationSceneRoot
     * @param old   T to be edited
     */
    public ViewReceiptDialog(@NotNull Window owner, @NotNull ApplicationSceneRoot appSceneRoot, @NotNull Receipt old) {
        super(owner, old);
        if (old == null) { throw new NullPointerException(); }
        if (appSceneRoot == null) { throw new NullPointerException(); }

        this.appSceneRoot = appSceneRoot;

        // populate the observable map with all transactions that belong to the receipt
        for (Map.Entry<Integer, Transaction> entry : appSceneRoot.getHuischLedger().getTransactions().entrySet()) {
            if (old.getTransactionIdSet().contains(entry.getKey())) {
                transactionObservableMap.put(entry.getKey(), entry.getValue());
            }
        }

        transactionTableView = new TransactionTableView(
                appSceneRoot,
                transactionObservableMap,
                appSceneRoot.getHuischLedger().getAccountingEntities(),
                appSceneRoot.getHuischLedger().getReceipts(),
                true // hide the column with value receipt, because we are only viewing transactions of one receipt
        );

        // Listen for changes in the accompanied transactions
        old.getTransactionIdSet().addListener(this);

        initAppearance();
        initBehaviour();
    }

    /**
     * Initialises and sets a root to a new Scene and sets the scene to be used by the stage.
     */
    @Override
    protected void initAppearance() {
        GridPane rootGridPane = new GridPane();
        rootGridPane.setPadding(new Insets(10));
        rootGridPane.setVgap(10);
        rootGridPane.setHgap(5);

        // Set the text of the labels
        nameLabel.setText("Naam:");
        dateLabel.setText("Datum:");
        payerLabel.setText("Betaler:");
        transactionsLabel.setText("Transacties:");

        // Set simple fields
        nameTextField.setText(old.getName());
        datePicker.setValue(old.getDate());

        // set searchable combo boxes
        List<AccountingEntity> accountingEntitiesList = new ArrayList<>(appSceneRoot.getHuischLedger().getAccountingEntities().values());
        ObservableList<AccountingEntity> accountingEntityObservableList = FXCollections.observableList(accountingEntitiesList);
        payerComboBox.setItems(accountingEntityObservableList);
        payerComboBox.getSelectionModel().select(appSceneRoot.getHuischLedger().getAccountingEntities().get(old.getPayer()));
        payerComboBox.setDisable(true); // The Receipt already contains transactions connected to this payer, and therefore this value must not change.

        // the transactionTableView is set in the constructor because of the way of constructing it.

        // Transaction buttons
        addTransactionButton.setText("Toevoegen");
        addTransactionButton.setOnAction(new AddTransactionEventHandler());
        importTransactionButton.setText("Importeren");
        importTransactionButton.setTooltip(new Tooltip("Importeer een bestaande transactie, die nog niet is " +
                "gefactureerd en ook nog geen verbonden transactie heeft."));
        importTransactionButton.setOnAction(new ImportTransactionEventHandler());

        // row 0
        rootGridPane.add(nameLabel, 0, 0);
        rootGridPane.add(nameTextField, 1, 0);
        // empty column
        rootGridPane.add(dateLabel, 3, 0);
        rootGridPane.add(datePicker, 4, 0);
        // row 1
        rootGridPane.add(payerLabel, 0, 1);
        rootGridPane.add(payerComboBox, 1, 1);
        // row 2
        rootGridPane.add(transactionsLabel, 0, 2);
        rootGridPane.add(addTransactionButton, 1, 2);
        rootGridPane.add(importTransactionButton, 2, 2);
        rootGridPane.add(transactionTableView, 0, 3, 5, 1);

        Scene scene = new Scene(rootGridPane);
        scene.getStylesheets().add(App.CSS_STYLES);
        stage.setScene(scene);
        stage.setTitle("Bewerken van bonnetje: " + old.getName());
        stage.titleProperty().bindBidirectional(nameTextField.textProperty(), new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return "Bewerken van bonnetje: " + object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });
        stage.sizeToScene();
    }

    /**
     * Sets behaviours of components that need them. E.g. sets action handlers.
     */
    @Override
    protected void initBehaviour() {

    }

    /**
     * Called after a change has been made to an ObservableSet.
     * This method is called on every elementary change (add/remove) once.
     * This means, complex changes like removeAll(Collection) or clear()
     * may result in more than one call of onChanged method.
     *
     * @param change the change that was made
     */
    @Override
    public void onChanged(SetChangeListener.Change<? extends Integer> change) {
        // whenever a Transaction is removed from the receipt, remove it from the transactionObservableMap
        // whenever a Transaction is added to the receipt, add it to the transactionObservableMap
        if (change.wasAdded()) {
            if (change.getElementAdded() == null)
                return;
            int index = change.getElementAdded();
            Transaction transaction = appSceneRoot.getHuischLedger().getTransactions().get(index);
            // do not add null valued transactions
            if (transaction == null)
                return;
            transactionObservableMap.put(index, transaction);
        }
        if (change.wasRemoved()) {
            if (change.getElementRemoved() == null)
                return;
            int index = change.getElementRemoved();
            transactionObservableMap.remove(index); // remove the Transaction with id index
        }
    }

    private class AddTransactionEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            showTransactionDialog();
        }

        private void showTransactionDialog() {
            TransactionDialog transactionDialog = new TransactionDialog(
                    stage,
                    appSceneRoot.getHuischLedger().getAccountingEntities(),
                    appSceneRoot.getHuischLedger().getReceipts(),
                    appSceneRoot.getHuischLedger().getAndIncrementNextTransactionId(), // FIXME only increment nextTransactionId after a success of adding the transaction
                    old.getId() // will not produce nptr exception since we checked old at construction
            );
            transactionDialog.setInitialDate(old.getDate()); // Set the initial date equal to the date of the receipt

            Optional<Transaction> result = transactionDialog.showAndWait();

            if (result.isPresent()) {
                Transaction transactionResult = result.get();
                if (containsPayer(transactionResult)) {
                    // possibly correct transaction. Try to add it to the HuischLedger to the correct receipt.
                    try {
                        appSceneRoot.getHuischLedger().addTransaction(transactionResult);
                    } catch (Exception e) {
                        System.err.println("Failed to add the transaction to the HuischLedger and corresponding receipt");
                    }
                } else {
                    Dialog<ButtonType> errorDialog = new Dialog<>();
                    errorDialog.getDialogPane().getButtonTypes().add(
                            new ButtonType("OK", ButtonBar.ButtonData.OK_DONE)
                    );
                    errorDialog.setContentText("De debtor of creditor van deze transactie moet gelijk zijn aan '"
                            + payerComboBox.getSelectionModel().getSelectedItem().getName() + "'");
                    errorDialog.showAndWait();
                    showTransactionDialog();
                }
            }
        }
    }

    private class ImportTransactionEventHandler implements  EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            System.err.println("ImportTransactionEventHandler is not yet implemented");
        }
    }

    /**
     * Returns whether {@code transaction} contains the payer as either debtor or creditor.
     *
     * @param transaction the Transaction to check
     * @return {@code true} when either debtorId or creditorId equals payer
     */
    private boolean containsPayer(Transaction transaction) {
        return (transaction.getDebtorId() == old.getPayer() || transaction.getCreditorId() == old.getPayer());
    }
}
