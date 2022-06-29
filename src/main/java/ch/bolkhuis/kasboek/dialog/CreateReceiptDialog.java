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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.*;

public class CreateReceiptDialog extends AbstractDialog<Receipt> {
    private final ObservableMap<Integer, AccountingEntity> accountingEntityObservableMap;

    private final int receiptId;

    private final Label nameLabel = new Label();
    private final TextField nameTextField = new TextField();
    private final Label dateLabel = new Label();
    private final DatePicker datePicker = new DatePicker();
    private final Label payerLabel = new Label();
    private final SearchableComboBox<AccountingEntity> payerSearchableComboBox = new SearchableComboBox<>();
    private final Button submitButton = new Button();

    /**
     * Creates a new AbstractDialog and initialises its owner.
     *
     * @param owner owner to be used for the stage
     */
    public CreateReceiptDialog(@NotNull Window owner, @NotNull ObservableMap<Integer, AccountingEntity> entities,
                               int receiptId) {
        super(owner);
        if (entities == null) { throw new NullPointerException(); }

        this.receiptId = receiptId;
        this.accountingEntityObservableMap = entities;

        initAppearance();
        initBehaviour();
    }

    /**
     * Initialises and sets a root to a new Scene and sets the scene to be used by the stage.
     */
    @Override
    protected void initAppearance() {
        GridPane rootGridPane = new GridPane();
        rootGridPane.setVgap(10);
        rootGridPane.setHgap(5);
        rootGridPane.setPadding(new Insets(10));

        // set the text properties of the components
        nameLabel.setText("Naam:");
        dateLabel.setText("Datum:");
        payerLabel.setText("Betaald door:");

        nameTextField.setPromptText("Bierrun makro"); // example
        // set no initial date for the DatePicker to prevent the user for forgetting to fill in the correct date
        payerSearchableComboBox.setPromptText("Met wie z'n rekening is betaald?");

        submitButton.setText("Opslaan");
        submitButton.setMaxWidth(Double.MAX_VALUE);

        nameTextField.setBorder(errorBorder);
        datePicker.setBorder(errorBorder);
        payerSearchableComboBox.setBorder(errorBorder);

        List<AccountingEntity> accountingEntitiesList = new ArrayList<>(accountingEntityObservableMap.values());
        ObservableList<AccountingEntity> observableList = FXCollections.observableList(accountingEntitiesList);
        payerSearchableComboBox.setItems(observableList);

        rootGridPane.add(nameLabel, 0, 0);
        rootGridPane.add(nameTextField, 1, 0);
        rootGridPane.add(dateLabel, 0, 1);
        rootGridPane.add(datePicker, 1, 1);
        rootGridPane.add(payerLabel, 0, 2);
        rootGridPane.add(payerSearchableComboBox, 1, 2);
        rootGridPane.add(submitButton, 0, 4, 2, 1);

        Scene scene = new Scene(rootGridPane);
        scene.getStylesheets().add(App.CSS_STYLES);
        stage.setScene(scene);
        stage.setTitle("Bonnetje maken");
        stage.sizeToScene();
    }

    /**
     * Sets behaviours of components that need them. E.g. sets action handlers.
     */
    @Override
    protected void initBehaviour() {
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isBlank()) {
                nameTextField.setBorder(errorBorder);
            } else {
                nameTextField.setBorder(correctBorder);
            }
        });
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                datePicker.setBorder(errorBorder);
            } else {
                datePicker.setBorder(correctBorder);
            }
        });
        payerSearchableComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                payerSearchableComboBox.setBorder(errorBorder);
            } else {
                payerSearchableComboBox.setBorder(correctBorder);
            }
        });

        submitButton.setOnAction(new InputProcessingEventHandler());
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
            String name = nameTextField.getText();
            LocalDate date = datePicker.getValue();
            AccountingEntity payer = payerSearchableComboBox.getSelectionModel().getSelectedItem();

            try {
                result = Optional.of(new Receipt(
                        receiptId,
                        name,
                        new HashSet<>(),
                        date,
                        payer.getId()
                ));
                stage.hide();
                return;
            } catch (Exception ignored) {}

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
