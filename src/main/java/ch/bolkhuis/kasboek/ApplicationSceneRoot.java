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
package ch.bolkhuis.kasboek;

import ch.bolkhuis.kasboek.components.AccountingEntityTreeTableView;
import ch.bolkhuis.kasboek.components.ReceiptTableView;
import ch.bolkhuis.kasboek.components.TransactionTableView;
import ch.bolkhuis.kasboek.core.*;
import ch.bolkhuis.kasboek.dialog.*;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.prefs.*;

/**
 * ApplicationSceneRoot is the root for the main Scene of the Application class. The root presents the user with an
 * overview of a single HuischLedger.
 *
 * The HuischLedger is loaded from a File.
 *
 * @author Aron Hoogeveen
 */
public class ApplicationSceneRoot extends BorderPane {
    // Preferences keys
//    public static final String PREF_FILE_CHOOSER_DIRECTORY = "FileChooserDirectory";
//    // Preferences default values
//    public static final String PREF_DEFAULT_FILE_CHOOSER_DIRECTORY = System.getProperty("user.home");
//    private final static String PREF_NODE_NAME = "/ch/bolkhuis/kasboek/ApplicationSceneRoot";

    private final HuischLedger huischLedger;
    private File huischLedgerFile; // this file can be updated, so no final here

    private final App app;

    // Preferences object for this ApplicationSceneRoot
    private final Preferences preferences;

    // MapChangeListeners used for observing unsaved changes
    private final AccountingEntityMapChangeListener entityMapChangeListener = new AccountingEntityMapChangeListener();
    private final TransactionMapChangeListener transactionMapChangeListener = new TransactionMapChangeListener();
    private final ReceiptMapChangeListener receiptMapChangeListener = new ReceiptMapChangeListener();
    /**
     * This field MUST NOT be changed other then by calling setUnsavedChanges(boolean)!
     */
    private boolean unsavedChanges = false;

    /**
     * Default constructor.
     */
    public ApplicationSceneRoot(@NotNull App app, @NotNull HuischLedger huischLedger, File huischLedgerFile) {
        if (app == null) { throw new NullPointerException(); }
        if (huischLedger == null) { throw new NullPointerException(); }

        this.app = app;
        this.huischLedger = huischLedger;
        this.huischLedgerFile = huischLedgerFile;

        preferences = Preferences.userRoot().node(PreferencesStrings.APPLICATIONSCENEROOT_NODE);

        // set the onCloseRequest handler for the Application stage
        app.getPrimaryStage().setOnCloseRequest(new WindowCloseEventHandler());

        // a null-valued huischLedgerFile indicates a new HuischLedger which implies that it is not already saved.
        // setUnsavedChanges handles the adding and removing of the listeners
        setUnsavedChanges(huischLedgerFile == null);

        initAppearance();
        createAndSetChildren();
    }

    /**
     * Set all settings for this BorderPane that have to do with its appearance.
     */
    private void initAppearance() {
        setMinSize(App.MIN_WIDTH, App.MIN_HEIGHT);
        setPrefSize(App.INITIAL_WIDTH, App.INITIAL_HEIGHT);
    }

    /**
     * Creates and sets the children of this BorderPane. This method is called in all constructors.
     */
    private void createAndSetChildren() {
        createAndSetTopMenuBar(); // setTop()
        createAndSetCenterTabPane(); // setCenter()
    }

    /**
     * Creates and sets the MenuBar in the top of the window.
     */
    private void createAndSetTopMenuBar() {
        // File menu
        Menu fileMenu = new Menu("Bestand");
        MenuItem newFile = new MenuItem("Nieuwe maken");
        newFile.setOnAction(new NewFileEventHandler());
        MenuItem openFile = new MenuItem("Openen");
        openFile.setOnAction(new OpenFileEventHandler());
        MenuItem closeFile = new MenuItem("Sluiten");
        closeFile.setOnAction(new CloseEventHandler());
        MenuItem saveFile = new MenuItem("Opslaan");
        saveFile.setOnAction(new SaveEventHandler());
        MenuItem saveAsFile = new MenuItem("Opslaan als");
        saveAsFile.setOnAction(new SaveAsEventHandler());
        MenuItem preferences = new MenuItem("Instellingen");
        fileMenu.getItems().addAll(
                newFile,
                openFile,
                closeFile,
                new SeparatorMenuItem(),
                saveFile,
                saveAsFile,
                new SeparatorMenuItem(),
                preferences
        );
        // Edit menu
        Menu editMenu = new Menu("Bewerken");
        MenuItem addAccountingEntity = new MenuItem("Entiteit toevoegen");
        addAccountingEntity.setOnAction(new AddAccountingEntityEventHandler());
        MenuItem addInmateEntity = new MenuItem("Huischgenoot toevoegen");
        addInmateEntity.setOnAction(new AddInmateEntityEventHandler());
        MenuItem addReceipt = new MenuItem("Bonnetje toevoegen");
        addReceipt.setOnAction(new AddReceiptEventHandler());
        MenuItem addTransaction = new MenuItem("Transactie toevoegen");
        addTransaction.setOnAction(new AddTransactionEventHandler());
        editMenu.getItems().addAll(
                addAccountingEntity,
                addInmateEntity,
                addTransaction,
                addReceipt
        );
        // Print menu
        Menu toolsMenu = new Menu("Gereedschappen");
        Menu printSubMenu = new Menu("Factuur Printen");
        MenuItem printInvoicesForAllInmates = new MenuItem("Alle Huischgenoten");
        MenuItem printInvoicesForSelectedInmates = new MenuItem("Selecteer Huischgenoten");
        printInvoicesForSelectedInmates.setOnAction(new InvoiceSelectedResidentsEventHandler());
        printSubMenu.getItems().addAll(
                printInvoicesForAllInmates,
                printInvoicesForSelectedInmates
        );
        toolsMenu.getItems().addAll(
                printSubMenu
        );

        // Developer menu.
        Menu developerMenu = new Menu("Developer");
        MenuItem printSizeOfAccountingEntities = new MenuItem("size of entities");
        printSizeOfAccountingEntities.setOnAction(event -> {
            System.out.println("The size of entities is: " + huischLedger.getAccountingEntities().size());
        });
        MenuItem printSizeOfTransactions = new MenuItem("size of transactions");
        printSizeOfTransactions.setOnAction(event -> {
            System.out.println("The size of transactions is: " + huischLedger.getTransactions().size());
        });
        MenuItem printSizeOfReceipts = new MenuItem("size of receipts");
        printSizeOfReceipts.setOnAction(event -> {
            System.out.println("The size of receipts is: " + huischLedger.getReceipts().size());
        });
        MenuItem showFirstReceipt = new MenuItem("Show first receipt");
        showFirstReceipt.setOnAction(event -> {
            ViewReceiptDialog viewReceiptDialog = new ViewReceiptDialog(app.getPrimaryStage(), this, huischLedger.getReceipts().get(0));
            viewReceiptDialog.showAndWait();
        });
        MenuItem invoiceFirstResident = new MenuItem("Invoice first resident");
        invoiceFirstResident.setOnAction(event -> {
            // get the resident with id "1" and create an invoice with set dates.
            LocalDate startDate = LocalDate.parse("2020-01-01");
            LocalDate endDate = LocalDate.parse("2020-01-31");
            System.out.println("Invoice dates: " + startDate.toString() + ", " + endDate.toString());
            File out = new File("out/test_invoice_output.html");
            File template = new File("out/Template HuischInvoice.html");
            try {
                HuischLedger.generateResidentInvoice(out, template, huischLedger, 0, startDate, endDate,
                        "This is hardcoded text for the development option with start and end ${start_date} + " +
                                "${end_date}.");
            } catch (Exception e) {
                System.err.println("Something went wrong while testing the invoicing system");
                e.printStackTrace();
            }
        });
        developerMenu.getItems().addAll(
                printSizeOfAccountingEntities,
                printSizeOfTransactions,
                printSizeOfReceipts,
                showFirstReceipt,
                invoiceFirstResident
        );

        // Add the File Menus to the MenuBar
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(
                fileMenu,
                editMenu,
                toolsMenu,
                developerMenu
        );

        setTop(menuBar);
    }

    /**
     * Creates and sets the tab pane showing the AccountingEntities, Receipts and Transactions.
     */
    private void createAndSetCenterTabPane() {
        // The tabpane with the AccountingEntities, Receipts and Transactions
        TabPane tabPane = new TabPane();
        tabPane.setId("main-tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab entitiesTab = new Tab("Entiteiten");
        Tab receiptsTab = new Tab("Bonnetjes");
        Tab transactionsTab = new Tab("Transacties");

        // Entities Tab
        AccountingEntityTreeTableView entityTreeTableView = new AccountingEntityTreeTableView(
                this,
                huischLedger.getAccountingEntities()
        );
        entitiesTab.setContent(new BorderPane(entityTreeTableView)); // this should make everything full sized

        // Receipts Tab
        ReceiptTableView receiptTableView = new ReceiptTableView(this, huischLedger.getReceipts(), huischLedger.getAccountingEntities(), app.getPrimaryStage());
        receiptsTab.setContent(receiptTableView);

        // Transactions Tab
        TransactionTableView transactionTableView = new TransactionTableView(
                this,
                huischLedger.getTransactions(),
                huischLedger.getAccountingEntities(),
                huischLedger.getReceipts()
        );
        transactionsTab.setContent(transactionTableView);

        // Add the tabs and the pane to this BorderPane
        tabPane.getTabs().addAll(
                entitiesTab,
                receiptsTab,
                transactionsTab
        );
        setCenter(tabPane);
    }

    /**
     * Shows an EntityDialog in which the user can create a new entity that is then added to the {@code huischLedger}.
     */
    public void showAccountingEntityDialog() {
        AccountingEntityDialog accountingEntityDialog = new AccountingEntityDialog(
                app.getPrimaryStage(),
                huischLedger.getAndIncrementNextAccountingEntityId());
        Optional<AccountingEntity> result = accountingEntityDialog.showAndWait();
        // isResultAvailable return false if no AccountingEntity was created
        if (result.isPresent()) {
            AccountingEntity accountingEntity = result.get();
            try {
                huischLedger.addAccountingEntity(accountingEntity);
            } catch (Exception e) {
                System.err.println("Could not add to AccountingEntity returned from the AccountingEntityDialog");
                e.printStackTrace();
            }
        }
    }

    public void showInmateEntityDialog() {
        InmateEntityDialog inmateEntityDialog = new InmateEntityDialog(
                app.getPrimaryStage(),
                huischLedger.getAndIncrementNextAccountingEntityId());
        Optional<ResidentEntity> result = inmateEntityDialog.showAndWait();
        // isResultAvailable return false if no AccountingEntity was created
        if (result.isPresent()) {
            ResidentEntity residentEntity = result.get();
            try {
                huischLedger.addAccountingEntity(residentEntity);
            } catch (Exception e) {
                System.err.println("Could not add the ResidentEntity returned from the InmateEntityDialog");
                e.printStackTrace();
            }
        }
    }

    public void showTransactionDialog() {
        TransactionDialog transactionDialog = new TransactionDialog(
                app.getPrimaryStage(),
                huischLedger.getAccountingEntities(),
                huischLedger.getReceipts(),
                huischLedger.getAndIncrementNextTransactionId()
        );
        Optional<Transaction> result = transactionDialog.showAndWait();

        if (result.isPresent()) {
            Transaction transaction = result.get();
            try {
                huischLedger.addTransaction(transaction);
            } catch (Exception e) {
                System.err.println("Could not add the Transaction returned from the TransactionDialog id: " + transaction.getId());
                e.printStackTrace();
            }
        }
    }

    public void showCreateReceiptDialog() throws Exception {
        CreateReceiptDialog receiptDialog = new CreateReceiptDialog(
                app.getPrimaryStage(),
                huischLedger.getAccountingEntities(),
                huischLedger.getAndIncrementNextReceiptId()
        );
        Optional<Receipt> result = receiptDialog.showAndWait();

        if (result.isPresent()) {
            Receipt receipt = result.get();
            huischLedger.addReceipt(receipt);
        }
    }

    public App getApp() { return app; }

    public HuischLedger getHuischLedger() { return huischLedger; }

    // *****************************************************************************************************************
    // * Click Event Handlers
    // *****************************************************************************************************************
    private class AddAccountingEntityEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            try {
                showAccountingEntityDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class AddInmateEntityEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            try {
                showInmateEntityDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            try {
                showTransactionDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class AddReceiptEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            try {
                showCreateReceiptDialog();
            } catch (Exception e) {
                System.err.println("Could not add Receipt returned from CreateReceiptDialog");
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the field {@code unsavedChanges} and updates the observers for unsaved changes accordingly.
     *
     * @param newValue the value to set for {@code unsavedChanges}
     */
    private void setUnsavedChanges(boolean newValue) {
        if (newValue) {
            System.out.println("Observed changes to the HuischLedger. Setting unsavedChanges to true.");
            // remove the unsaved observing mapchangelisteners from the lists of mapchangelisteners
            huischLedger.removeEntityListener(entityMapChangeListener);
            huischLedger.removeTransactionListener(transactionMapChangeListener);
            huischLedger.removeReceiptListener(receiptMapChangeListener);

            unsavedChanges = newValue;
        } else {
            System.out.println("Changes are probably saved, because setUnsavedChanges was called with value false.");
            // changes have been saved. Re-register the listeners for observing unsaved changes
            huischLedger.addEntityListener(entityMapChangeListener);
            huischLedger.addTransactionListener(transactionMapChangeListener);
            huischLedger.addReceiptListener(receiptMapChangeListener);

            unsavedChanges = newValue;
        }
    }

    private class AccountingEntityMapChangeListener implements MapChangeListener<Integer, AccountingEntity> {

        /**
         * Called after a change has been made to an ObservableMap.
         * This method is called on every elementary change (put/remove) once.
         * This means, complex changes like keySet().removeAll(Collection) or clear()
         * may result in more than one call of onChanged method.
         *
         * @param change the change that was made
         */
        @Override
        public void onChanged(Change<? extends Integer, ? extends AccountingEntity> change) {
            setUnsavedChanges(true);
        }
    }

    private class TransactionMapChangeListener implements MapChangeListener<Integer, Transaction> {

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
            setUnsavedChanges(true);
        }
    }

    private class ReceiptMapChangeListener implements  MapChangeListener<Integer, Receipt> {

        /**
         * Called after a change has been made to an ObservableMap.
         * This method is called on every elementary change (put/remove) once.
         * This means, complex changes like keySet().removeAll(Collection) or clear()
         * may result in more than one call of onChanged method.
         *
         * @param change the change that was made
         */
        @Override
        public void onChanged(Change<? extends Integer, ? extends Receipt> change) {
            setUnsavedChanges(true);
        }

    }

    /**
     * Save the {@code huischLedger} to {@code huischLedgerFile}. If {@code huischLedgerFile} is {@code null} then
     * {@link ApplicationSceneRoot#saveAs()} is called. Sets {@code unsavedChanges} to {@code true} on success
     *
     * @see ApplicationSceneRoot#saveAs()
     */
    private boolean save() throws IOException {
        // save again also when unsavedChanges is false

        // if there is no savefile set, saveAs()
        if (huischLedgerFile == null) {
            return saveAs();
        }
        HuischLedger.toFile(huischLedgerFile, huischLedger);

        // save success
        setUnsavedChanges(false);
        return true;
    }

    /**
     * Asks the user to provide file name for the file to save {@code huischLedger} to. Sets {@code unsavedChanges} to
     * {@code true} on success.
     *
     * @see ApplicationSceneRoot#save()
     */
    private boolean saveAs() throws IOException {
        // Present user with FileChooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(preferences.get(PreferencesStrings.APPLICATIONSCENEROOT_FILE_CHOOSER_DIRECTORY, PreferencesStrings.APPLICATIONSCENEROOT_DEFAULT_FILE_CHOOSER_DIRECTORY)));
        fileChooser.setTitle("Huisch Kasboek - Opslaan als");
        fileChooser.getExtensionFilters().addAll(App.extensionFilters);
        File file = fileChooser.showSaveDialog(app.getPrimaryStage());

        if (file != null) {
            HuischLedger.toFile(file, huischLedger);

            // success
            huischLedgerFile = file;
            setUnsavedChanges(false);
            return true;
        }

        return false;
    }

    private boolean close() {
        if (unsavedChanges) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType("Opslaan", ButtonBar.ButtonData.YES),
                    new ButtonType("Sluiten", ButtonBar.ButtonData.NO),
                    new ButtonType("Afbreken", ButtonBar.ButtonData.CANCEL_CLOSE)
            );
            dialog.setTitle("Opongeslagen wijzigingen");
            dialog.setContentText("Er zijn wijzigingen die niet zijn opgeslagen. Weet je zeker dat je dit kasboek " +
                    "wil sluiten?");
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent()) {
                ButtonType res = result.get();
                if (res.getButtonData().equals(ButtonBar.ButtonData.YES)) {
                    try {
                        boolean saved = save();
                        if (saved) {
                            return true;
                        }
                        // The saving is cancelled, do nothing.
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                        ErrorDialog errorDialog = new ErrorDialog("Er is wat fout gegaan bij het opslaan.");
                        errorDialog.showAndWait();
                    }
                } else if (res.getButtonData().equals(ButtonBar.ButtonData.NO)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    private void newFile() {
        // close the current loaded HuischLedger
        if (close()) {
            // set the new HuischLedger
            ApplicationSceneRoot applicationSceneRoot = new ApplicationSceneRoot(
                    app,
                    new HuischLedger(),
                    null
            );
            app.changeToApplicationScene(applicationSceneRoot);
        }
    }

    /**
     * Present the user with a FileChooser and try to load a HuischLedger from the selected File.
     */
    private void openFile() {
        // can we safely close this ledger?
        if (close()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(preferences.get(
                    PreferencesStrings.APPLICATIONSCENEROOT_FILE_CHOOSER_DIRECTORY,
                    PreferencesStrings.APPLICATIONSCENEROOT_DEFAULT_FILE_CHOOSER_DIRECTORY
            )));
            fileChooser.setTitle("Huisch Kasboek - Open Kasboek");
            fileChooser.getExtensionFilters().addAll(App.extensionFilters);
            File file = fileChooser.showOpenDialog(app.getPrimaryStage());

            if (file != null) {
                try {
                    HuischLedger newLedger = HuischLedger.fromFile(file);

                    ApplicationSceneRoot applicationSceneRoot = new ApplicationSceneRoot(
                            app,
                            huischLedger,
                            file
                    );
                    app.changeToApplicationScene(applicationSceneRoot);
                } catch (Exception e) {
                    e.printStackTrace();
                    ErrorDialog errorDialog = new ErrorDialog("Kon geen Kasboek laden uit dat bestand.");
                    errorDialog.showAndWait();
                }
            }
        }
    }

    private class WindowCloseEventHandler implements EventHandler<WindowEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(WindowEvent event) {
            if (close()) {
                System.out.println("Calling Platform.exit()");
                Platform.exit();
            } else {
                event.consume();
            }
        }
    }

    private class SaveEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            try {
                save();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                ErrorDialog errorDialog = new ErrorDialog("Het kasboek kon niet opgeslagen worden");
                errorDialog.showAndWait();
            }
        }
    }

    private class SaveAsEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            try {
                saveAs();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                ErrorDialog errorDialog = new ErrorDialog("Het kasboek kon niet opgeslagen worden in het " +
                        "geselecteerde bestand.");
                errorDialog.showAndWait();
            }
        }
    }

    private class CloseEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            if (close()) {
                app.changeToSplashScene();
            } else {
                event.consume();
            }
        }
    }

    private class NewFileEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            try {
                newFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class OpenFileEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            try {
                openFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class InvoiceSelectedResidentsEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            InvoicingDialog invoicingDialog = new InvoicingDialog(preferences, huischLedger, app.getPrimaryStage());
            invoicingDialog.showAndWait();
        }
    }

}
