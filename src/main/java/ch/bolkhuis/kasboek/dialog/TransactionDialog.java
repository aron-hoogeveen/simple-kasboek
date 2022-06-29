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
import ch.bolkhuis.kasboek.core.AccountingEntity;
import ch.bolkhuis.kasboek.core.Receipt;
import ch.bolkhuis.kasboek.core.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDialog extends AbstractDialog<Transaction> {

    private final int newId;
    private final Integer receiptId;
    private final ObservableMap<Integer, AccountingEntity> accountingEntityObservableMap;
    private final ObservableMap<Integer, Receipt> receiptObservableMap;

    private final Label dateLabel = new Label();
    private final DatePicker datePicker = new DatePicker();
    private final Label debtorLabel = new Label();
    private final SearchableComboBox<AccountingEntity> debtorComboBox = new SearchableComboBox<>();
    private final Label creditorLabel = new Label();
    private final SearchableComboBox<AccountingEntity> creditorComboBox = new SearchableComboBox<>();
    private final Label receiptLabel = new Label();
    private final SearchableComboBox<Receipt> receiptComboBox = new SearchableComboBox<>();
    private final Label amountLabel = new Label();
    private final TextField amountTextField = new TextField();
    private final Label descriptionLabel = new Label();
    private final TextField descriptionTextField = new TextField();

    private final Button submitButton = new Button();

    /**
     * Creates a new AbstractDialog and initialises its owner.
     *
     * @param owner owner to be used for the stage
     * @param id the id used for the new Transaction
     */
    public TransactionDialog(
            @NotNull Window owner,
            @NotNull ObservableMap<Integer, AccountingEntity> accountingEntities,
            @NotNull ObservableMap<Integer, Receipt> receipts,
            int id
    ) {
        super(owner);

        this.accountingEntityObservableMap = accountingEntities;
        this.receiptObservableMap = receipts;
        this.newId = id;
        this.receiptId = null;

        initAppearance();
        initBehaviour();
    }

    public TransactionDialog(
            @NotNull Window owner,
            @NotNull ObservableMap<Integer, AccountingEntity> accountingEntities,
            @NotNull ObservableMap<Integer, Receipt> receipts,
            int id,
            int receiptId
    ) {
        super (owner);

        this.accountingEntityObservableMap = accountingEntities;
        this.receiptObservableMap = receipts;
        this.newId = id;
        this.receiptId = receiptId;

        initAppearance();
        initBehaviour();
    }

    /**
     * Creates a new AbstractDialog and initialises its owner and the old T to load.
     *
     * @param owner owner to be used for the stage
     * @param old   T to be edited
     */
    public TransactionDialog(
            @NotNull Window owner,
            @NotNull ObservableMap<Integer, AccountingEntity> accountingEntities,
            @NotNull ObservableMap<Integer, Receipt> receipts,
            @NotNull Transaction old
    ) {
        super(owner, old);

        this.accountingEntityObservableMap = accountingEntities;
        this.receiptObservableMap = receipts;
        this.newId = -1; // default id for when the id of old is used
        this.receiptId = null;

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

        // Set the labels' text
        dateLabel.setText("Datum:");
        debtorLabel.setText("Debtor:");
        creditorLabel.setText("Creditor:");
        receiptLabel.setText("Receipt:");
        amountLabel.setText("Bedrag:");
        descriptionLabel.setText("Beschrijving:");
        submitButton.setText("Opslaan");

        // Set the prompts for the TextFields
        amountTextField.setPromptText("Het bedrag in euros");
        descriptionTextField.setPromptText("Beschrijving van deze transactie");

        // Set the possible values for the ComboBoxes
        List<AccountingEntity> accountingEntities = new ArrayList<>(accountingEntityObservableMap.values());
        ObservableList<AccountingEntity> accountingEntityObservableList = FXCollections.observableList(accountingEntities);
        debtorComboBox.setItems(accountingEntityObservableList);
        creditorComboBox.setItems(accountingEntityObservableList);
        List<Receipt> receipts = new ArrayList<>(receiptObservableMap.values());
        ObservableList<Receipt> receiptObservableList = FXCollections.observableList(receipts);
//        receiptObservableList.add(null); // It is totally valid for a transaction to not be attached to a Receipt
        // TODO add a null item to the BEGIN of the items list so the user can undo a faulty selection
        receiptComboBox.setItems(receiptObservableList);

        // Set the initial values if old is not null
        if (old != null) {
            datePicker.setValue(old.getDate());
            debtorComboBox.getSelectionModel().select(accountingEntityObservableMap.get(old.getDebtorId()));
            creditorComboBox.getSelectionModel().select(accountingEntityObservableMap.get(old.getCreditorId()));
//            if (old.getReceiptId() != null) {
            receiptComboBox.getSelectionModel().select(receiptObservableMap.get(old.getReceiptId()));
//            }
            amountTextField.setText(String.valueOf(old.getAmount()));
            descriptionTextField.setText(old.getDescription());

            // Disable all fields that will not be used (stay the same)
            debtorComboBox.setDisable(true);
            creditorComboBox.setDisable(true);
            amountTextField.setDisable(true);
            receiptComboBox.setDisable(true);
        } else {
            // Set the date for the DatePicker to today
            datePicker.setValue(LocalDate.now());
            if (receiptId != null) {
                // set a mandatory receipt for this new Transaction
                receiptComboBox.getSelectionModel().select(receiptObservableMap.get(receiptId));
                receiptComboBox.setDisable(true);
            }
        }

        // Set the maximum widths of all inputs to Double.MAX_VALUE for equal widths
        datePicker.setMaxWidth(Double.MAX_VALUE);
        debtorComboBox.setMaxWidth(Double.MAX_VALUE);
        creditorComboBox.setMaxWidth(Double.MAX_VALUE);
        receiptComboBox.setMaxWidth(Double.MAX_VALUE);
        amountTextField.setMaxWidth(Double.MAX_VALUE);
        descriptionTextField.setMaxWidth(Double.MAX_VALUE);
        submitButton.setMaxWidth(Double.MAX_VALUE);

        // Add all components to the rootGridPane
        rootGridPane.add(dateLabel, 0, 0);
        rootGridPane.add(datePicker, 1, 0);
        rootGridPane.add(receiptLabel, 2, 0);
        rootGridPane.add(receiptComboBox, 3, 0);
        rootGridPane.add(debtorLabel, 0, 1);
        rootGridPane.add(debtorComboBox, 1, 1);
        rootGridPane.add(creditorLabel, 2, 1);
        rootGridPane.add(creditorComboBox, 3, 1);
        rootGridPane.add(amountLabel, 0, 2);
        rootGridPane.add(amountTextField, 1, 2);
        rootGridPane.add(descriptionLabel, 0, 3);
        rootGridPane.add(descriptionTextField, 1, 3, 3, 1);
        rootGridPane.add(submitButton, 2, 5, 2, 1);

        // Set borders indicating which inputs require a selection
        debtorComboBox.setBorder(errorBorder);
        creditorComboBox.setBorder(errorBorder);
        amountTextField.setBorder(errorBorder);
        descriptionTextField.setBorder(errorBorder);

        Scene scene = new Scene(rootGridPane);
        scene.getStylesheets().add(App.CSS_STYLES);
        stage.setScene(scene);
        stage.sizeToScene();
    }

    /**
     * Sets behaviours of components that need them. E.g. sets action handlers.
     */
    @Override
    protected void initBehaviour() {
        descriptionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Transaction.isCorrectDescription(newValue))
                descriptionTextField.setBorder(errorBorder);
            else
                descriptionTextField.setBorder(correctBorder);
        });

        amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(numberRegex))
                amountTextField.setBorder(errorBorder);
            else {
                try {
                    if (!Transaction.isCorrectAmount(Double.parseDouble(newValue)))
                        amountTextField.setBorder(errorBorder);
                    else
                        amountTextField.setBorder(correctBorder);
                } catch (Exception e) {
                    amountTextField.setBorder(errorBorder);
                }
            }
        });

        debtorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                debtorComboBox.setBorder(errorBorder);
            else
                debtorComboBox.setBorder(correctBorder);
        });

        creditorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                creditorComboBox.setBorder(errorBorder);
            else
                creditorComboBox.setBorder(correctBorder);
        });

        submitButton.setOnAction(new InputProcessingEventHandler());
    }

    /**
     * Sets the title of the scene based on whether an old Transaction is provided.
     */
    private void setTitle() {
        if (old == null)
            stage.setTitle("Transactie toevoegen");
        else
            stage.setTitle("Transactie bewerken");
    }

    @Override
    public void show() {
        setTitle();
        stage.show();
    }

    @Override
    public Optional<Transaction> showAndWait() {
        setTitle();
        stage.showAndWait();
        return result;
    }

    /**
     * Calls {@code setValue} on the DatePicker with value {@code date}.
     * @param date the date to set the DatePicker to
     */
    public void setInitialDate(@NotNull LocalDate date) {
        if (date == null) { throw new NullPointerException(); }
        datePicker.setValue(date);
    }

    private class InputProcessingEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            LocalDate date = datePicker.getValue();
            AccountingEntity debtor = debtorComboBox.getValue();
            AccountingEntity creditor = creditorComboBox.getValue();
            Receipt receipt = receiptComboBox.getValue();
            Integer receiptId = (receipt == null) ? null : receipt.getId();
            String amountString = amountTextField.getText();
            String description = descriptionTextField.getText();

            // Validate the inputs
            try {
                if (Transaction.isCorrectAmount(Double.parseDouble(amountString)) && Transaction.isCorrectDescription(description)) {
                    if (old == null) {
                        result = Optional.of(new Transaction(
                                newId,
                                debtor.getId(), // could cause NullPointerException but it will be caught by the try-catch
                                creditor.getId(), // same
                                Double.parseDouble(amountString),
                                receiptId,
                                date,
                                description
                        ));
                    } else {
                        result = Optional.of(new Transaction(
                                old.getId(),
                                old.getDebtorId(),
                                old.getCreditorId(),
                                old.getAmount(),
                                old.getReceiptId(),
                                date,
                                description
                        ));
                    }
                    stage.hide();
                    return;
                }
            }
            catch (Exception ignored) {}

            // Some inputs are illegal
            Dialog<ButtonType> errorDialog = new Dialog<>();
            ButtonType buttonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            errorDialog.getDialogPane().getButtonTypes().add(buttonType);
            errorDialog.setTitle("Error");
            errorDialog.setContentText("Een of meerdere inputs hebben illegale waarden. Vul alsjeblieft legale " +
                    "waarden in.");
            errorDialog.showAndWait();
        }
    }
}
