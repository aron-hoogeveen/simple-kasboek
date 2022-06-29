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

import ch.bolkhuis.kasboek.App;
import ch.bolkhuis.kasboek.components.LedgerFileCell;
import ch.bolkhuis.kasboek.components.RecentLedgerFile;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class LedgerFileListView extends ListView<RecentLedgerFile> {
    private final App app;
    /**
     * Creates a default ListView which will display contents stacked vertically.
     * As no {@link ObservableList} is provided in this constructor, an empty
     * ObservableList is created, meaning that it is legal to directly call
     * {@link #getItems()} if so desired. However, as noted elsewhere, this
     * is not the recommended approach
     * (instead call {@link #setItems(ObservableList)}).
     *
     * <p>Refer to the {@link ListView} class documentation for details on the
     * default state of other properties.
     */
    public LedgerFileListView(App app) {
        if (app == null) { throw new NullPointerException(); }

        this.app = app;

        initCellFactory();
    }

    /**
     * Creates a default ListView which will stack the contents retrieved from the
     * provided {@link ObservableList} vertically.
     *
     * <p>Attempts to add a listener to the {@link ObservableList}, such that all
     * subsequent changes inside the list will be shown to the user.
     *
     * <p>Refer to the {@link ListView} class documentation for details on the
     * default state of other properties.
     *
     * @param items the list of items
     */
    public LedgerFileListView(App app, ObservableList<RecentLedgerFile> items) {
        super(items);

        if (app == null) { throw new NullPointerException(); }

        this.app = app;

        initCellFactory();
    }

    /**
     * Sets the cell factory for this ListView.
     */
    private void initCellFactory() {
        setCellFactory(createCellFactory());
    }

    private Callback<ListView<RecentLedgerFile>, ListCell<RecentLedgerFile>> createCellFactory() {
        return new Callback<ListView<RecentLedgerFile>, ListCell<RecentLedgerFile>>() {
            @Override
            public ListCell<RecentLedgerFile> call(ListView<RecentLedgerFile> param) {
                return new LedgerFileCell();
            }
        };
    }

    public App getApp() { return this.app; }
}
