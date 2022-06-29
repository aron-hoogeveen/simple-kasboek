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
import ch.bolkhuis.kasboek.PreferencesStrings;
import ch.bolkhuis.kasboek.core.AccountingEntity;
import ch.bolkhuis.kasboek.core.HuischLedger;
import ch.bolkhuis.kasboek.core.ResidentEntity;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.*;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.textfield.CustomTextField;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.prefs.Preferences;

/**
 * InvoicingDialog is the class used for showing a dialog in which the user can supply all information needed for
 * generating invoices for one or more ResidentEntities.
 */
public class InvoicingDialog {

    private final Stage stage;
    private final Preferences preferences;
    private final HuischLedger huischLedger;
    
    private final FileProperty targetDirectoryFileProperty = new FileProperty();
    private final FileProperty templateFileProperty = new FileProperty();

    /*
     * Scene components
     */
    private final Label introTextLabel = new Label();
    private final TextArea introTextTextArea = new TextArea();
    private final Label informationLabel = new Label();
    private final Label targetDirectoryLabel = new Label();
    private final Label templateFileLabel = new Label();
    private final CustomTextField targetDirectoryTextField = new CustomTextField();
    private final CustomTextField templateFileTextField = new CustomTextField();
    private final Label startDateLabel = new Label();
    private final Label endDateLabel = new Label();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final ListSelectionView<ResidentEntity> listSelectionView = new ListSelectionView<>();
    private final Button submitButton = new Button();

    /**
     * Constructor.
     */
    public InvoicingDialog(@NotNull final Preferences preferences,
                           @NotNull final HuischLedger huischLedger,
                           final Window owner) {
        this.preferences = Objects.requireNonNull(preferences);
        this.huischLedger = Objects.requireNonNull(huischLedger);

        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Facturen Maken");

        initComponents(huischLedger);
    }

    /**
     * Creates and sets the components to a new Scene and calls {@code setScene} on the Stage with that scene.
     */
    private void initComponents(@NotNull final HuischLedger huischLedger) {
        Objects.requireNonNull(huischLedger);

        GridPane root = new GridPane();
        root.setMaxWidth(1280);
        root.setMaxHeight(720);

        informationLabel.setText("Schuif alle Huischgenoten naar rechts voor welke je een factuur wil maken. De " +
                "'introtekst' ondersteunt de parameters '${start_date}' en '${end_date}' voor als je in je factuur de " +
                "gefactureerde periode wil aangeven (Bijvoorbeeld: \"Dit is de factuur van ${start_date} tot " +
                "${end_date}\"). Let op: voordat de facturen gegenereerd worden, wordt de 'doelmap' eerst leeg gemaakt. " +
                "Hierbij worden alle bestanden en submappen in die map verwijderd.");
        introTextLabel.setText("Introtekst:");
        targetDirectoryLabel.setText("Uitvoermap:");
        templateFileLabel.setText("Template bestand:");
        startDateLabel.setText("Start datum:");
        endDateLabel.setText("Eind datum:");
        submitButton.setText("Factureren");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setOnAction(new SubmitEventHandler());

        informationLabel.setWrapText(true);
        introTextTextArea.setWrapText(true);

        // targetDirectory
        targetDirectoryTextField.setPromptText("Selecteer een uitvoer locatie...");
        templateFileTextField.setPromptText("Selecteer de template...");
        Image image1 = new Image("icons8-folder-24.png");
        Image image2 = new Image("icons8-folder-24.png");
        ImageView imageView1 = new ImageView(image1);
        ImageView imageView2 = new ImageView(image2);
        imageView1.setCursor(Cursor.HAND);
        imageView2.setCursor(Cursor.HAND);
        imageView1.setOnMouseClicked(new TargetDirectorySelectionEventHandler());
        imageView2.setOnMouseClicked(new TemplateFileSelectionEventHandler());
        targetDirectoryTextField.setRight(imageView1);
        targetDirectoryTextField.setEditable(false);
        targetDirectoryTextField.setTooltip(new Tooltip("Tekstinput wordt momenteel niet ondersteunt voor dit component"));
        targetDirectoryFileProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                targetDirectoryTextField.setText(newValue.getAbsolutePath());
            } else {
                targetDirectoryTextField.setText(null);
            }
        });

        // init the date pickers
        // For convenience disable all dates dat are before the selected start_date for the endDatePicker (although it
        // would perfectly be legal to allow dates that are before that day. However that would result in no transactions
        // being selected
        endDatePicker.setDayCellFactory(param -> new DateCell() {
            /**
             * {@inheritDoc}
             *
             * @param item
             * @param empty
             */
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && startDatePicker.getValue() != null) {
                    setDisable(item.isBefore(startDatePicker.getValue()));
                }
            }
        });
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && endDatePicker.getValue() == null) {
                endDatePicker.setValue(newValue); // set a default date for the end date to improve user experience
            }
        });
        startDatePicker.setMaxWidth(Double.MAX_VALUE);
        endDatePicker.setMaxWidth(Double.MAX_VALUE);

        // Set the default text for the intro text
        introTextTextArea.setText(preferences.get(
                PreferencesStrings.INVOICINGDIALOG_INTRO_TEXT,
                PreferencesStrings.INVOICINGDIALOG_DEFAULT_INTRO_TEXT
        ));

        templateFileTextField.setRight(imageView2);
        templateFileTextField.setEditable(false);
        templateFileTextField.setTooltip(new Tooltip("Tekstinput wordt momenteel niet ondersteunt voor dit component"));
        templateFileProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                templateFileTextField.setText(newValue.getAbsolutePath());
            } else {
                templateFileTextField.setText(null);
            }
        });

        // Initialize the ListSelectionView
        // Get all residents
        ObservableList<ResidentEntity> residentEntities = FXCollections.observableArrayList();
        for (AccountingEntity entity : huischLedger.getAccountingEntities().values()) {
            if (entity instanceof ResidentEntity) {
                residentEntities.add((ResidentEntity) entity);
            }
        }
        listSelectionView.setSourceItems(residentEntities);
        listSelectionView.setPadding(new Insets(0));

        // put the information text inside a scrollpane
        ScrollPane scrollPane = new ScrollPane(informationLabel);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(70);
        scrollPane.setMinHeight(70);
        scrollPane.setMaxHeight(100);

        root.add(scrollPane, 0, 0, 2, 1);
        root.add(startDateLabel, 0, 1);
        root.add(endDateLabel, 1, 1);
        root.add(startDatePicker, 0, 2);
        root.add(endDatePicker, 1, 2);
        root.add(introTextLabel, 0, 3);
        root.add(introTextTextArea, 0, 4, 2, 1);
        root.add(templateFileLabel, 0, 5);
        root.add(templateFileTextField, 1, 5);
        root.add(targetDirectoryLabel, 0, 6);
        root.add(targetDirectoryTextField, 1, 6);
        root.add(listSelectionView, 0, 7, 2, 1);
        root.add(submitButton, 0, 8);

        root.setStyle("-fx-padding: 10px;");
        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().addAll(
                    App.CSS_STYLES
            );
        } catch (Exception e) {
            System.err.println("Could not load/set the stylesheet for InvoicingDialog");
        }

        stage.setScene(scene);
        stage.sizeToScene();
    }

    /**
     * Calls the {@code showAndWait} method on the Stage of this InvoicingDialog.
     */
    public void showAndWait() {
        stage.showAndWait();
    }

    private class TargetDirectorySelectionEventHandler implements EventHandler<MouseEvent> {
        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(MouseEvent event) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Uitvoermap");
            try {
                if (targetDirectoryFileProperty.getValue() == null) {
                    directoryChooser.setInitialDirectory(new File(preferences.get(
                            PreferencesStrings.APPLICATIONSCENEROOT_FILE_CHOOSER_DIRECTORY,
                            PreferencesStrings.APPLICATIONSCENEROOT_DEFAULT_FILE_CHOOSER_DIRECTORY
                    )));
                } else {
                    directoryChooser.setInitialDirectory(targetDirectoryFileProperty.getValue());
                }
            } catch (Exception e) {
                System.err.println("Something went wrong while setting the initial directory for 'directoryChooser' in " +
                        "class InvoicingDialog: " + e.getMessage());
            }
            File newTargetDir = directoryChooser.showDialog(stage);

            if (newTargetDir != null) {
                targetDirectoryFileProperty.set(newTargetDir);
            }
        }
    }

    private class TemplateFileSelectionEventHandler implements EventHandler<MouseEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(MouseEvent event) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecteer Template Bestand");
            try {
                if (templateFileProperty.getValue() == null) {
                    fileChooser.setInitialDirectory(new File(preferences.get(
                            PreferencesStrings.APPLICATIONSCENEROOT_FILE_CHOOSER_DIRECTORY,
                            PreferencesStrings.APPLICATIONSCENEROOT_DEFAULT_FILE_CHOOSER_DIRECTORY
                    )));
                } else {
                    fileChooser.setInitialDirectory(templateFileProperty.getValue().getParentFile());
                }
            }
            catch (Exception e) {
                System.out.println("Something went wrong while setting the initial directory for 'fileChooser' in class" +
                        " InvoicingDialog: " + e.getMessage());
            }
            File newTemplateFile = fileChooser.showOpenDialog(stage);

            if (newTemplateFile != null) {
                templateFileProperty.set(newTemplateFile);
            }
        }
    }

    private class SubmitEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            File targetDir = targetDirectoryFileProperty.getValue();
            File templateFile = templateFileProperty.getValue();

            // check target directory
            if (targetDir == null) {
                ErrorDialog errorDialog = new ErrorDialog("Kies aub een uitvoermap");
                errorDialog.showAndWait();
                return;
            }

            // check template file
            if (templateFile == null) {
                ErrorDialog errorDialog = new ErrorDialog("Kies aub een template");
                errorDialog.showAndWait();
                return;
            }

            if (!targetDir.isDirectory()) {
                System.err.println("Selected file is not a directory");
                return;
            }

            if (!templateFile.isFile()) {
                System.err.println("Selected file is not a file");
                return;
            }

            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                ErrorDialog errorDialog = new ErrorDialog("Kies een start en eind datum");
                errorDialog.showAndWait();
                return;
            }

            // Check if the directory contains files, remove if the user gives permission
            String[] targetDirFileList = targetDir.list();
            if (Objects.requireNonNull(targetDirFileList, "NPE, a file is provided that is not a directory where a directory was expected").length > 0) {
                Dialog<ButtonType> dialog = new Dialog<>();
                ButtonType yesButtonType = new ButtonType("Verwijderen", ButtonBar.ButtonData.YES);
                ButtonType noButtonType = new ButtonType("Annuleren", ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getDialogPane().getButtonTypes().setAll(
                        yesButtonType,
                        noButtonType
                );
                ((Button) dialog.getDialogPane().lookupButton(noButtonType)).setDefaultButton(true);
                dialog.setGraphic(null);
                dialog.setHeaderText("De geselecteerde map bevat nog bestanden. Wil je verder gaan en alle bestanden " +
                        "verwijderen of annuleren en een andere doelmap kiezen?");
                Optional<ButtonType> result = dialog.showAndWait();

                if (result.isPresent()) {
                    ButtonType resultButtonType = result.get();
                    if (!resultButtonType.equals(yesButtonType)) {
                        // do not delete the contents. Let the user select another empty targetDir
                        return;
                    }
                } else {
                    System.err.println("The result of the dialog is not present yet (InvoicingDialog)");
                    return;
                }

                try {
                    FileUtils.cleanDirectory(targetDir);
                } catch (IOException ioException) {
                    System.err.println("An error occured during cleaning of the target directory. Cancelling the " +
                            "invoicing operation...");
                    ioException.printStackTrace();
                    return;
                }
            }

            // generate an invoice for all selected ResidentEntities
            ObservableList<ResidentEntity> targetItems = listSelectionView.getTargetItems();
            if (targetItems == null) {
                ErrorDialog errorDialog = new ErrorDialog("De targetItems heeft 'null' teruggegeven. Laat dit de ontwikkelaar weten. (InvoicingDialog)");
                errorDialog.showAndWait();
                System.err.println("listSelectionView.getTargetItems() returned 'null'. See the docs of ListSelectionView how this is caused");
                return;
            }

            boolean allInvoicesSuccessfullyGenerated = true;
            for (ResidentEntity residentEntity : targetItems) {
                String name = residentEntity.getName();
                if (!name.matches("[a-zA-Z ]*")) {
                    // illegal name, not safe for processing. Only allow alphabetic characters and spaces for simplicity
                    allInvoicesSuccessfullyGenerated = false;
                    System.err.println("Encountered a ResidentEntity with a name that is not supported by the InvoicingDialog (" +
                            "name:" + residentEntity.getName() + ")");
                    continue; // skip this residentEntity
                }
                File out = FileUtils.getFile(targetDir, residentEntity.getName() + ".html");
                try {
                    HuischLedger.generateResidentInvoice(
                            out,
                            templateFile,
                            huischLedger,
                            residentEntity.getId(),
                            startDatePicker.getValue(),
                            endDatePicker.getValue(),
                            introTextTextArea.getText()
                    );
                } catch (Exception exception) {
                    allInvoicesSuccessfullyGenerated = false;
                    System.err.println("An error occurred while generating the invoice for ResidentEntity with name '" +
                            residentEntity.getName() + "'");
                    exception.printStackTrace();
                }
            }

            if (!allInvoicesSuccessfullyGenerated) {
                ErrorDialog errorDialog = new ErrorDialog("Een of meerdere facturen konden niet gegenereerd " +
                        "worden. Zie de console voor een stacktrace of errorbericht.");
                errorDialog.showAndWait();
            } else {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                dialog.setGraphic(null);
                dialog.setHeaderText("Alle facturen zijn successvol gegenereerd");
                dialog.showAndWait();
            }

            stage.hide(); // close the invoicing dialog
        }
    }

    private static class FileProperty implements ObservableObjectValue<File> {
        private File item = null;
        Vector<ChangeListener<File>> changeListeners = new Vector<>();
        Vector<InvalidationListener> invalidationListeners = new Vector<>();

        /**
         * Returns the current value of this {@code ObservableObjectValue<T>}.
         *
         * @return The current value
         */
        @Override
        public File get() {
            return item;
        }

        public void set(File file) {
            File old = this.item;
            this.item = file;

            fireChange(old, this.item);
        }

        /**
         * Adds a {@link ChangeListener} which will be notified whenever the value
         * of the {@code ObservableValue} changes. If the same listener is added
         * more than once, then it will be notified more than once. That is, no
         * check is made to ensure uniqueness.
         * <p>
         * Note that the same actual {@code ChangeListener} instance may be safely
         * registered for different {@code ObservableValues}.
         * <p>
         * The {@code ObservableValue} stores a strong reference to the listener
         * which will prevent the listener from being garbage collected and may
         * result in a memory leak. It is recommended to either unregister a
         * listener by calling {@link #removeListener(ChangeListener)
         * removeListener} after use.
         *
         * @param listener The listener to register
         * @throws NullPointerException if the listener is null
         * @see #removeListener(ChangeListener)
         */
        @Override
        public void addListener(ChangeListener<? super File> listener) {
            changeListeners.add((ChangeListener<File>) listener);
        }

        /**
         * Removes the given listener from the list of listeners that are notified
         * whenever the value of the {@code ObservableValue} changes.
         * <p>
         * If the given listener has not been previously registered (i.e. it was
         * never added) then this method call is a no-op. If it had been previously
         * added then it will be removed. If it had been added more than once, then
         * only the first occurrence will be removed.
         *
         * @param listener The listener to remove
         * @throws NullPointerException if the listener is null
         * @see #addListener(ChangeListener)
         */
        @Override
        public void removeListener(ChangeListener<? super File> listener) {
            changeListeners.remove(listener);
        }

        /**
         * Returns the current value of this {@code ObservableValue}
         *
         * @return The current value
         */
        @Override
        public File getValue() {
            return get();
        }

        /**
         * Adds an {@link InvalidationListener} which will be notified whenever the
         * {@code Observable} becomes invalid. If the same
         * listener is added more than once, then it will be notified more than
         * once. That is, no check is made to ensure uniqueness.
         * <p>
         * Note that the same actual {@code InvalidationListener} instance may be
         * safely registered for different {@code Observables}.
         * <p>
         * The {@code Observable} stores a strong reference to the listener
         * which will prevent the listener from being garbage collected and may
         * result in a memory leak. It is recommended to either unregister a
         * listener by calling {@link #removeListener(InvalidationListener)
         * removeListener} after use.
         *
         * @param listener The listener to register
         * @throws NullPointerException if the listener is null
         * @see #removeListener(InvalidationListener)
         */
        @Override
        public void addListener(InvalidationListener listener) {
            invalidationListeners.add(listener);
        }

        /**
         * Removes the given listener from the list of listeners, that are notified
         * whenever the value of the {@code Observable} becomes invalid.
         * <p>
         * If the given listener has not been previously registered (i.e. it was
         * never added) then this method call is a no-op. If it had been previously
         * added then it will be removed. If it had been added more than once, then
         * only the first occurrence will be removed.
         *
         * @param listener The listener to remove
         * @throws NullPointerException if the listener is null
         * @see #addListener(InvalidationListener)
         */
        @Override
        public void removeListener(InvalidationListener listener) {
            invalidationListeners.remove(listener);
        }

        private void fireChange(File oldValue, File newValue) {
            for (ChangeListener<File> cl : changeListeners) {
                cl.changed(this, oldValue, newValue);
            }
        }

        private void fireInvalidation() {
            for (InvalidationListener il : invalidationListeners) {
                il.invalidated(this);
            }
        }

    }
}
