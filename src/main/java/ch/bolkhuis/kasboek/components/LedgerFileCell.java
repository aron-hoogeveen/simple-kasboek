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
import ch.bolkhuis.kasboek.ApplicationSceneRoot;
import ch.bolkhuis.kasboek.core.HuischLedger;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;

public class LedgerFileCell extends ListCell<RecentLedgerFile> {

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
    protected void updateItem(RecentLedgerFile item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
            setText(null);
            setContextMenu(null);
        } else {
            // Set the graphic of the ListCell
            VBox root = new VBox();
            Label name = new Label(item.getName());
            Label location = new Label(item.getFile().getPath());
            name.setTextFill(Color.BLACK); // fixed colour, no need for the different isSelected() colour.
            location.setTextFill(Color.GRAY); // fixed colour, no need for different isSelected() colour.
            // TODO set a little graphic of a cross on the top right corner. When clicked the item should be removed from the list
            root.getChildren().addAll(
                    name,
                    location
            );
            root.setCursor(Cursor.HAND);
            // due to the selection of a listitem when first clicked this event is only fired when the listiem is double
            // clicked or clicked when selected. Which I think is nice.
            root.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    try {
                        LedgerFileListView parentListView = ((LedgerFileListView) getListView());
                        HuischLedger huischLedger = HuischLedger.fromFile(item.getFile());
                        ApplicationSceneRoot applicationSceneRoot = new ApplicationSceneRoot(
                                parentListView.getApp(),
                                huischLedger,
                                item.getFile()
                        );
                        parentListView.getApp().changeToApplicationScene(applicationSceneRoot, item.getName());
                    } catch (IOException ioException) {
                        System.err.println("Could not load the HuischLedger from the file provided by a RecentLedgerFile");
                    }
                }
            });

            MenuItem menuItem = new MenuItem("Verwijderen");
            menuItem.setOnAction(event -> {
                LedgerFileListView parentListView = ((LedgerFileListView)getListView());
                parentListView.getApp().removeRecentLedgerFile(item);
            });
            ContextMenu contextMenu = new ContextMenu(menuItem);
            setContextMenu(contextMenu);

            setGraphic(root);
        }
    }
}
