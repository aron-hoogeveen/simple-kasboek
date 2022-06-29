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
import ch.bolkhuis.kasboek.core.HuischLedger;
import ch.bolkhuis.kasboek.core.Receipt;
import ch.bolkhuis.kasboek.dialog.ViewReceiptDialog;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * ReceiptTableView is an implementation of the {@link TableView} for {@link Receipt}s. The class uses a map as its
 * backing structure and updates the backing list to reflect the map.values().
 */
public class ReceiptTableView extends TableView<Receipt> implements MapChangeListener<Integer, Receipt> {
    private final ObservableMap<Integer, Receipt> m_items;
    private final ObservableMap<Integer, AccountingEntity> m_entities;
    private final ApplicationSceneRoot appSceneRoot;
    private final Window owner;

    /**
     * Creates a default TableView control with no content.
     *
     * <p>Refer to the {@link TableView} class documentation for details on the
     * default state of other properties.
     */
    public ReceiptTableView(
            @NotNull ApplicationSceneRoot appSceneRoot,
            @NotNull ObservableMap<Integer, AccountingEntity> m_entities,
            @NotNull Window owner
    ) {
        if (appSceneRoot == null) { throw new NullPointerException(); }
        if (m_entities == null) { throw new NullPointerException(); }
        m_items = FXCollections.observableHashMap();
        this.m_entities = m_entities;
        this.appSceneRoot = appSceneRoot;
        this.owner = owner;

        setEditable(false); // disable editing in this table. Transactions are edited in a specific dialog presented to the user
        m_items.addListener(this);

        initColumns();
        initChildren();
    }

    /**
     * Creates a default TableView control with content.
     *
     * <p>Refer to the {@link TableView} class documentation for details on the
     * default state of other properties.
     *
     * @param m_items initial ObservableMap to be used as backing map
     */
    public ReceiptTableView(
            @NotNull ApplicationSceneRoot appSceneRoot,
            @NotNull ObservableMap<Integer, Receipt> m_items,
            @NotNull ObservableMap<Integer, AccountingEntity> m_entities,
            @NotNull Window owner) {
        if (appSceneRoot == null) { throw new NullPointerException(); }
        if (m_items == null) { throw new NullPointerException(); }
        if (m_entities == null) { throw new NullPointerException(); }

        this.appSceneRoot = appSceneRoot;
        this.m_items = m_items;
        this.m_entities = m_entities;
        this.owner = owner;

        setEditable(false); // disable editing in this table. Transactions are edited in a specific dialog presented to the user
        this.m_items.addListener(this);

        initColumns();
        initChildren();
    }

    /**
     * Create and set the columns for this TableView.
     */
    private void initColumns() {
        TableColumn<Receipt, String> dateColumn = new TableColumn<>("Datum");
        TableColumn<Receipt, String> payerColumn = new TableColumn<>("Betaald Door");
        TableColumn<Receipt, String> nameColumn = new TableColumn<>("Beschrijving");

        dateColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getDate().toString())); // FIXME change to property in Receipt class
        payerColumn.setCellValueFactory(param -> m_entities.get(param.getValue().getPayer()).nameProperty());
        nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getName()));

        getColumns().setAll(
                dateColumn,
                payerColumn,
                nameColumn
        );

        setRowFactory(param -> new ReceiptTableRowFactory());
    }

    /**
     * Initialises the backing {@code items} ObservableList from the values of the backing ObservableMap {@code m_items}.
     * This method is called by all constructors.
     */
    private void initChildren() {
        getItems().setAll(m_items.values()); // clears the ObservableList and then adds all Receipts if any
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
    public void onChanged(Change<? extends Integer, ? extends Receipt> change) {
        // update the backing ObservableList items
        if (change.wasAdded()) {
            getItems().add(change.getValueAdded());
        }
        if (change.wasRemoved()){
            getItems().remove(change.getValueRemoved());
        }
    }

    private class ReceiptTableRowFactory extends TableRow<Receipt> {
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
        protected void updateItem(Receipt item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                 MenuItem editItem = new MenuItem("Bewerken");
                 MenuItem deleteItem = new MenuItem("Verwijderen");

                 editItem.setOnAction(event -> {
                     ViewReceiptDialog viewReceiptDialog = new ViewReceiptDialog(owner, appSceneRoot, item);
                     viewReceiptDialog.showAndWait();
                 });

                 deleteItem.setOnAction(event -> {
                     ButtonType yesType = new ButtonType("Verwijderen", ButtonBar.ButtonData.YES);
                     ButtonType noType = new ButtonType("Annuleren", ButtonBar.ButtonData.NO);
                     Dialog<ButtonType> dialog = new Dialog<>();
                     dialog.getDialogPane().getButtonTypes().addAll(
                             noType,
                             yesType
                     );
                     dialog.setTitle("Bonnetje verwijderen");
                     dialog.setHeaderText("Bonnetje \"" + item.getName() + "\" verwijderen?");
                     dialog.setGraphic(null);
                     // set initial selected button
                     ((Button)dialog.getDialogPane().lookupButton(yesType)).setDefaultButton(false);
                     ((Button)dialog.getDialogPane().lookupButton(noType)).setDefaultButton(true);
                     Optional<ButtonType> result = dialog.showAndWait();

                     if (result.isPresent()) {
                         ButtonType resultButtonType = result.get();
                         if (resultButtonType.getButtonData().equals(ButtonBar.ButtonData.YES)) {
                             // delete transaction
                             if (appSceneRoot.getHuischLedger().removeReceipt(item) == null) {
                                 System.err.println("The receipt (id:" + item.getId() + ") could not be found and could" +
                                         " therefore not be removed");
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

                 // Enable double-click to open the receipt
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        ViewReceiptDialog viewReceiptDialog = new ViewReceiptDialog(owner, appSceneRoot, item);
                        viewReceiptDialog.showAndWait();
                    }
                });
            } else {
                // remove the old contextmenu
                setContextMenu(null);
            }
        }
    }
}
