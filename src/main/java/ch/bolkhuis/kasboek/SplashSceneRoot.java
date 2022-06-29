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

import ch.bolkhuis.kasboek.components.LedgerFileListView;
import ch.bolkhuis.kasboek.components.RecentLedgerFile;
import ch.bolkhuis.kasboek.core.HuischLedger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.HyperlinkLabel;
import org.jetbrains.annotations.NotNull;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public class SplashSceneRoot extends BorderPane {
    private static final double WIDTH = 840;
    private static final double HEIGHT = 484;

    private Image logo;
    private App app;

    /**
     * Creates a new Splash Scene Root with the supplied Image as logo.
     */
    public SplashSceneRoot(@NotNull App app, @NotNull Image logo) {
        this.app = Objects.requireNonNull(app);
        this.logo = Objects.requireNonNull(logo);

        setId("SplashSceneRoot");
        initAppearance();
        createAndSetChildren();
    }

    /**
     * Sets all properties for the visual appearance of this SplashSceneRoot.
     *
     * This scene uses a fixed size
     */
    private void initAppearance() {
        setMinSize(WIDTH, HEIGHT);
        setPrefSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
    }

    /**
     * Creates and sets all children for this SplashSceneRoot.
     */
    private void createAndSetChildren() {
        // Recent opened Ledgers
        ObservableList<RecentLedgerFile> recentLedgerFiles = app.getRecentLedgerFiles();

        // create and set the main menu
        GridPane centerGrid = new GridPane();
        centerGrid.setId("centerGrid");
        centerGrid.setAlignment(Pos.CENTER);

        BorderPane imageBorderPane = new BorderPane();
        ImageView imageView = new ImageView(logo);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(180);
        imageBorderPane.setCenter(imageView);
        centerGrid.add(imageBorderPane, 0, 0);
        Label title = new Label("Huisch Kasboek");
        title.setId("splashTitle");
        centerGrid.add(title, 0, 1);

        Button newLedgerButton = new Button("Nieuw Kasboek", new ImageView("plus-sign-16.png"));
        newLedgerButton.setOnAction(event -> {
            app.changeToApplicationScene(new ApplicationSceneRoot(app, new HuischLedger(), null));
        });

        Button importLedgerButton = new Button("Importeer Kasboek", new ImageView("import-16.png"));
        importLedgerButton.setOnAction(new ImportLedgerEventHandler());

        Button getHelpButton = new Button("Krijg hulp", new ImageView("question-mark-16.png"));

        centerGrid.add(newLedgerButton, 0, 2);
        centerGrid.add(importLedgerButton, 0, 3);
        centerGrid.add(getHelpButton, 0, 4);

        setCenter(centerGrid);

        centerGrid.setMinSize(WIDTH, HEIGHT);
        centerGrid.setPrefSize(WIDTH, HEIGHT);
        centerGrid.setMaxSize(WIDTH, HEIGHT);

        // Set a list of RecentLedgerFiles on the Left side of this BorderPane
        LedgerFileListView ledgerFileListView = new LedgerFileListView(app, recentLedgerFiles);
        ledgerFileListView.setFocusTraversable(false); // We only want TAB to be used for the menu in the center

        // Set a fixed width of a third of the total width
        ledgerFileListView.setMinSize(WIDTH / 3.0, HEIGHT);
        ledgerFileListView.setPrefSize(WIDTH / 3.0, HEIGHT);
        ledgerFileListView.setMaxSize(WIDTH / 3.0, HEIGHT);

        setLeft(ledgerFileListView);

        // Update the centerGrid's width
        double newWidth = WIDTH / 3.0 * 2;
        centerGrid.setMinWidth(newWidth);
        centerGrid.setPrefWidth(newWidth);
        centerGrid.setMaxWidth(newWidth);

        HyperlinkLabel creatorNotice = new HyperlinkLabel("Gemaakt door [Aron Hoogeveen]");
        creatorNotice.setOnMouseClicked(event -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/aron-hoogeveen"));
            } catch (Exception ignored) {
                System.err.println("Java.AWT.Desktop is not available.");
            }
        });
        HBox creatorNoticeHBox = new HBox(creatorNotice);
        creatorNoticeHBox.setAlignment(Pos.CENTER);

        setBottom(creatorNoticeHBox);
    }

    private class ImportLedgerEventHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            // TODO add the extensionFilters
            fileChooser.setTitle("Importeer Kasboek bestand");
            File file = fileChooser.showOpenDialog(app.getPrimaryStage());

            if (file != null) {
                try {
                    HuischLedger huischLedger = HuischLedger.fromFile(file);
                    TextInputDialog textInputDialog = new TextInputDialog(file.getName());
                    textInputDialog.setHeaderText("Welke naam moet gebruikt worden voor dit kasboek?");
                    textInputDialog.setGraphic(null);
                    textInputDialog.setTitle("Kasboek importeren");
                    textInputDialog.setContentText(null);
                    Optional<String> result = textInputDialog.showAndWait();

                    if (result.isPresent()) {
                        if (!result.get().isBlank()) {
                            RecentLedgerFile recentLedgerFile = new RecentLedgerFile(file, result.get());
                            app.addRecentLedgerFile(recentLedgerFile);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Trying to import illegal HuischLedger file format.");
                }
            }
        }
    }

}
