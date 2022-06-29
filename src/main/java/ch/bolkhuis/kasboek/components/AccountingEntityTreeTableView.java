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
import ch.bolkhuis.kasboek.core.AccountType;
import ch.bolkhuis.kasboek.core.AccountingEntity;
import ch.bolkhuis.kasboek.core.ResidentEntity;
import ch.bolkhuis.kasboek.core.PlaceholderEntity;
import ch.bolkhuis.kasboek.dialog.AccountingEntityDialog;
import ch.bolkhuis.kasboek.dialog.InmateEntityDialog;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;

/**
 * AccountingEntityTreeTableView is an implementation of a {@code TreeTableView<AccountingEntity>}. The class listens for
 * changes in its own backing ObservableMap and updates its leaves accordingly.
 */
public class AccountingEntityTreeTableView extends TreeTableView<AccountingEntity> implements MapChangeListener<Integer, AccountingEntity> {
    private final ApplicationSceneRoot appSceneRoot;
    // Backing ObservableMap
    private final ObservableMap<Integer, AccountingEntity> m_items;
    // Root TreeItems
    private final TreeItem<AccountingEntity> inmatesRoot = new TreeTableRootItem<>(new PlaceholderEntity(-1, "Huischgenoten"));
    private final TreeItem<AccountingEntity> assetsRoot = new TreeTableRootItem<>(new PlaceholderEntity(-1, "Assets"));
    private final TreeItem<AccountingEntity> expensesRoot = new TreeTableRootItem<>(new PlaceholderEntity(-1, "Expenses"));
    private final TreeItem<AccountingEntity> liabilitiesRoot = new TreeTableRootItem<>(new PlaceholderEntity(-1, "Liabilities"));
    private final TreeItem<AccountingEntity> dividendsRoot = new TreeTableRootItem<>(new PlaceholderEntity(-1, "Dividends"));
    private final TreeItem<AccountingEntity> revenuesRoot = new TreeTableRootItem<>(new PlaceholderEntity(-1, "Revenues"));
    private final TreeItem<AccountingEntity> equitiesRoot = new TreeTableRootItem<>(new PlaceholderEntity(-1, "Equities"));

    /**
     * Creates a TreeTableView populated with the AccountingEntities from the ObservableMap {@code items}.
     *
     * @param m_items the AccountingEntities to put in this TreeTableView
     */
    public AccountingEntityTreeTableView(@NotNull ApplicationSceneRoot appSceneRoot,
                                         @NotNull ObservableMap<Integer, AccountingEntity> m_items) {
        Objects.requireNonNull(appSceneRoot);
        Objects.requireNonNull(m_items);

        this.m_items = m_items;
        this.appSceneRoot = appSceneRoot;

        setShowRoot(false);
        m_items.addListener(this); // listen for changes in the backing ObservableMap

        setCustomColumns();
        initChildren();
    }

    /**
     * Sets the Column Factories for this TreeTableView.
     */
    private void setCustomColumns() {
        TreeTableColumn<AccountingEntity, String> nameColumn = new TreeTableColumn<>("Naam");
        nameColumn.setCellFactory(param -> new AccountingEntityTreeTableCell());
        TreeTableColumn<AccountingEntity, String> balanceColumn = new TreeTableColumn<>("Balans");

        nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<AccountingEntity, String> param) -> {
                    AccountingEntity item = param.getValue().getValue();
                    if (item != null) {
                        return item.nameProperty();
                    }
                    return null;
                }
        );
        // Only set the balance for TreeItems which are not roots
        balanceColumn.setCellValueFactory(param -> {
            if (!param.getValue().isLeaf()) {
                return null;
            }
            StringProperty stringProperty = new SimpleStringProperty();
            AccountingEntity item = param.getValue().getValue();
            if (item != null) {
                ReadOnlyDoubleProperty doubleProperty = item.balanceProperty();
                stringProperty.bind(Bindings.createStringBinding(
                        () -> NumberFormat.getCurrencyInstance(Locale.GERMANY).format(doubleProperty.get()), doubleProperty
                ));
                return stringProperty;
            }
            return new SimpleStringProperty();
        });

        // Set preferred widths for the columns
        nameColumn.setPrefWidth(300);
        balanceColumn.setPrefWidth(100);

        getColumns().setAll(
                nameColumn,
                balanceColumn
        );
    }

    /**
     * Populate all the root items with leaves.
     */
    private void initChildren() {
        TreeItem<AccountingEntity> root = new TreeTableRootItem<>(new PlaceholderEntity(-1, "Entiteiten"));

        Vector<TreeItem<AccountingEntity>> inmateLeaves = new Vector<>();
        Vector<TreeItem<AccountingEntity>> assetLeaves = new Vector<>();
        Vector<TreeItem<AccountingEntity>> expenseLeaves = new Vector<>();
        Vector<TreeItem<AccountingEntity>> liabilityLeaves = new Vector<>();
        Vector<TreeItem<AccountingEntity>> dividendLeaves = new Vector<>();
        Vector<TreeItem<AccountingEntity>> revenueLeaves = new Vector<>();
        Vector<TreeItem<AccountingEntity>> equityLeaves = new Vector<>();

        // populate the vectors with leaves
        for (AccountingEntity item : m_items.values()) {
            Objects.requireNonNull(item);

            if (item instanceof ResidentEntity) {
                inmateLeaves.add(new TreeItem<>(item));
            } else {
                TreeItem<AccountingEntity> leaf = new TreeItem<>(item);
                if (item.getAccountType().equals(AccountType.ASSET)) {
                    assetLeaves.add(leaf);
                } else if (item.getAccountType().equals(AccountType.EXPENSE)) {
                    expenseLeaves.add(leaf);
                } else if (item.getAccountType().equals(AccountType.LIABILITY)) {
                    liabilityLeaves.add(leaf);
                } else if (item.getAccountType().equals(AccountType.DIVIDEND)) {
                    dividendLeaves.add(leaf);
                } else if (item.getAccountType().equals(AccountType.REVENUE)) {
                    revenueLeaves.add(leaf);
                } else if (item.getAccountType().equals(AccountType.EQUITY)) {
                    equityLeaves.add(leaf);
                } else {
                    System.err.println("AccountType \"" + item.getAccountType() + "\" not supported by class " +
                            "AccountingEntityTreeTableView for item with id: " + item.getId() + ".");
                }
            }
        }

        // Clear the children of the root and then add the new leaves. This makes this method suitable for resetting
        // this TreeTableView with a different backing ObservableMap.
        inmatesRoot.getChildren().setAll(inmateLeaves);
        assetsRoot.getChildren().setAll(assetLeaves);
        expensesRoot.getChildren().setAll(expenseLeaves);
        liabilitiesRoot.getChildren().setAll(liabilityLeaves);
        dividendsRoot.getChildren().setAll(dividendLeaves);
        revenuesRoot.getChildren().setAll(revenueLeaves);
        equitiesRoot.getChildren().setAll(equityLeaves);

        root.getChildren().addAll(
                inmatesRoot,
                assetsRoot,
                expensesRoot,
                liabilitiesRoot,
                dividendsRoot,
                revenuesRoot,
                equitiesRoot
        );

        // expand all roots
        inmatesRoot.setExpanded(true);
        assetsRoot.setExpanded(true);
        expensesRoot.setExpanded(true);
        liabilitiesRoot.setExpanded(true);
        dividendsRoot.setExpanded(true);
        revenuesRoot.setExpanded(true);
        equitiesRoot.setExpanded(true);

        setRoot(root);
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
    public void onChanged(Change<? extends Integer, ? extends AccountingEntity> change) {
        if (change.wasAdded()) {
            AccountingEntity entity = Objects.requireNonNull(change.getValueAdded(), "There should be no null valued AccountingEntities");
            TreeItem<AccountingEntity> leaf = new TreeItem<>(entity);

            // If there is an entity with the same id, remove the old entry first
            if (entity instanceof ResidentEntity) {
                inmatesRoot.getChildren().removeIf(treeItem -> treeItem.getValue().getId() == entity.getId());
                inmatesRoot.getChildren().add(leaf);
            } else if (entity.getAccountType().equals(AccountType.ASSET)) {
                assetsRoot.getChildren().removeIf(treeItem -> treeItem.getValue().getId() == entity.getId());
                assetsRoot.getChildren().add(leaf);
            } else if (entity.getAccountType().equals(AccountType.EXPENSE)) {
                expensesRoot.getChildren().removeIf(treeItem -> treeItem.getValue().getId() == entity.getId());
                expensesRoot.getChildren().add(leaf);
            } else if (entity.getAccountType().equals(AccountType.LIABILITY)) {
                liabilitiesRoot.getChildren().removeIf(treeItem -> treeItem.getValue().getId() == entity.getId());
                liabilitiesRoot.getChildren().add(leaf);
            } else if (entity.getAccountType().equals(AccountType.DIVIDEND)) {
                dividendsRoot.getChildren().removeIf(treeItem -> treeItem.getValue().getId() == entity.getId());
                dividendsRoot.getChildren().add(leaf);
            } else if (entity.getAccountType().equals(AccountType.REVENUE)) {
                revenuesRoot.getChildren().removeIf(treeItem -> treeItem.getValue().getId() == entity.getId());
                revenuesRoot.getChildren().add(leaf);
            } else if (entity.getAccountType().equals(AccountType.EQUITY)) {
                equitiesRoot.getChildren().removeIf(treeItem -> treeItem.getValue().getId() == entity.getId());
                equitiesRoot.getChildren().add(leaf);
            } else {
                System.err.println("Added change event: AccountType \"" + entity.getAccountType() + "\" not supported by class " +
                        "AccountingEntityTreeTableView for item with id: " + entity.getId() + ".");
            }
        }
        if (change.wasRemoved()) {
            AccountingEntity entity = Objects.requireNonNull(change.getValueRemoved(), "There should be no null valued AccountingEntities");
            TreeItem<AccountingEntity> leaf = new TreeItem<>(entity);
            if (entity instanceof ResidentEntity) {
                inmatesRoot.getChildren().remove(leaf);
            } else if (entity.getAccountType().equals(AccountType.ASSET)) {
                assetsRoot.getChildren().remove(leaf);
            } else if (entity.getAccountType().equals(AccountType.EXPENSE)) {
                expensesRoot.getChildren().remove(leaf);
            } else if (entity.getAccountType().equals(AccountType.LIABILITY)) {
                liabilitiesRoot.getChildren().remove(leaf);
            } else if (entity.getAccountType().equals(AccountType.DIVIDEND)) {
                dividendsRoot.getChildren().remove(leaf);
            } else if (entity.getAccountType().equals(AccountType.REVENUE)) {
                revenuesRoot.getChildren().remove(leaf);
            } else if (entity.getAccountType().equals(AccountType.EQUITY)) {
                equitiesRoot.getChildren().remove(leaf);
            } else {
                System.err.println("Removed change event: AccountType \"" + entity.getAccountType() + "\" not supported by class " +
                        "AccountingEntityTreeTableView for item with id: " + entity.getId() + ".");
            }
        }
    }

    /**
     * A TreeTableCell for AccountingEntities.
     */
    class AccountingEntityTreeTableCell extends TextFieldTreeTableCell<AccountingEntity, String> {
        /**
         * {@inheritDoc}
         *
         * @param item
         * @param empty
         */
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            ContextMenu contextMenu = new ContextMenu();

            // Add a ContextMenu if this is a TreeTableRootItem
            TreeItem<AccountingEntity> treeItem = getTreeTableRow().getTreeItem();
            if (treeItem instanceof TreeTableRootItem) {
                MenuItem addMenuItem = new MenuItem();

                if (treeItem == inmatesRoot) {
                    addMenuItem.setText("Huischgenoot toevoegen");
                    addMenuItem.setOnAction(new AddInmateEventHandler());
                } else if (treeItem == assetsRoot) {
                    addMenuItem.setText("Asset toevoegen");
                    addMenuItem.setOnAction(new AddEntityEventHandler(AccountType.ASSET));
                } else if (treeItem == expensesRoot) {
                    addMenuItem.setText("Expense toevoegen");
                    addMenuItem.setOnAction(new AddEntityEventHandler(AccountType.EXPENSE));
                } else if (treeItem == liabilitiesRoot) {
                    addMenuItem.setText("Liability toevoegen");
                    addMenuItem.setOnAction(new AddEntityEventHandler(AccountType.LIABILITY));
                } else if (treeItem == dividendsRoot) {
                    addMenuItem.setText("Dividend toevoegen");
                    addMenuItem.setOnAction(new AddEntityEventHandler(AccountType.DIVIDEND));
                } else if (treeItem == revenuesRoot) {
                    addMenuItem.setText("Revenue toevoegen");
                    addMenuItem.setOnAction(new AddEntityEventHandler(AccountType.REVENUE));
                } else if (treeItem == equitiesRoot) {
                    addMenuItem.setText("Equity toevoegen");
                    addMenuItem.setOnAction(new AddEntityEventHandler(AccountType.EQUITY));
                }

                contextMenu.getItems().clear();
                contextMenu.getItems().add(addMenuItem);
            }

            setContextMenu(contextMenu);
//            AccountingEntityTreeTableView.this.setContextMenu(contextMenu);
        }
    }

    class AddEntityEventHandler implements EventHandler<ActionEvent> {
        private final AccountType accountType;

        public AddEntityEventHandler(AccountType accountType) {
            this.accountType = accountType;
        }

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            AccountingEntityDialog accountingEntityDialog = new AccountingEntityDialog(
                    appSceneRoot.getApp().getPrimaryStage(),
                    appSceneRoot.getHuischLedger().getAndIncrementNextAccountingEntityId(),
                    accountType
            );
            Optional<AccountingEntity> result = accountingEntityDialog.showAndWait();
            // isResultAvailable return false if no AccountingEntity was created
            if (result.isPresent()) {
                AccountingEntity accountingEntity = result.get();
                try {
                    appSceneRoot.getHuischLedger().addAccountingEntity(accountingEntity);
                } catch (Exception e) {
                    System.err.println("Could not add to AccountingEntity returned from the AccountingEntityDialog");
                    e.printStackTrace();
                }
            }
        }
    }

    class AddInmateEventHandler implements EventHandler<ActionEvent> {

        /**
         * Invoked when a specific event of the type for which this handler is
         * registered happens.
         *
         * @param event the event which occurred
         */
        @Override
        public void handle(ActionEvent event) {
            InmateEntityDialog inmateEntityDialog = new InmateEntityDialog(
                    appSceneRoot.getApp().getPrimaryStage(),
                    appSceneRoot.getHuischLedger().getAndIncrementNextAccountingEntityId()
            );
            Optional<ResidentEntity> result = inmateEntityDialog.showAndWait();
            // isResultAvailable return false if no ResidentEntity was created
            if (result.isPresent()) {
                AccountingEntity accountingEntity = result.get();
                try {
                    appSceneRoot.getHuischLedger().addAccountingEntity(accountingEntity);
                } catch (Exception e) {
                    System.err.println("Could not add to AccountingEntity returned from the AccountingEntityDialog");
                    e.printStackTrace();
                }
            }
        }
    }

}
