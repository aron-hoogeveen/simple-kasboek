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

import ch.bolkhuis.kasboek.components.RecentLedgerFile;
import ch.bolkhuis.kasboek.gson.CustomizedGson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;


/**
 * JavaFX App
 *
 * TODO add an option to import entities from another HuischLedgerFile
 */
public class App extends Application {
    public static final double INITIAL_WIDTH = 1280;
    public static final double INITIAL_HEIGHT = 720;
    public static final double MIN_WIDTH = 848;
    public static final double MIN_HEIGHT = 480;

    public static final String CSS_STYLES = "ch.bolkhuis.kasboek.Styles.css";
    public static final String CSS_SPLASH = "ch.bolkhuis.kasboek.splash.css";

    private static final String PREF_RECENT_LEDGERS_FILE = "RecentLedgersFile";
    private static final String PREF_DEF_RECENT_LEDGERS_FILE = System.getProperty("user.home") + "/.kasboek/RecentLedgers.json";
    private static final String PREF_NODE_NAME = "/ch/bolkhuis/kasboek/App";

    /**
     * ExtensionFilters to be used for saving and opening HuischLedger files.
     */
    public static final Vector<FileChooser.ExtensionFilter> extensionFilters = new Vector<>(List.of(
            new FileChooser.ExtensionFilter("Huischkasboek Bestanden", "*.hlf"),
            new FileChooser.ExtensionFilter("Alle Bestanden", "*.*")
    ));

    private Stage primaryStage;
    private Image splashLogo;

    private Preferences preferences;
    private ObservableList<RecentLedgerFile> recentLedgerFiles = FXCollections.observableArrayList();
    Type listType = new TypeToken<List<RecentLedgerFile>>() {}.getType();

    /**
     * The application initialization method. This method is called immediately
     * after the Application class is loaded and constructed. An application may
     * override this method to perform initialization prior to the actual starting
     * of the application.
     *
     * <p>
     * The implementation of this method provided by the Application class does nothing.
     * </p>
     *
     * <p>
     * NOTE: This method is not called on the JavaFX Application Thread. An
     * application must not construct a Scene or a Stage in this
     * method.
     * An application may construct other JavaFX objects in this method.
     * </p>
     *
     * @throws Exception if something goes wrong
     */
    @Override
    public void init() throws Exception {
        preferences = Preferences.userRoot().node(PREF_NODE_NAME);

        // Load the splash screen image already and pass it to the SplashSceneRoot constructor
        // The width is determined from the
        splashLogo = new Image("BolkhuischLogo.png", 240, 240, true, true);

        // load the recent ledgers
        try {

            BufferedReader ledgerFileReader = new BufferedReader(new FileReader(preferences.get(PREF_RECENT_LEDGERS_FILE, PREF_DEF_RECENT_LEDGERS_FILE)));
            recentLedgerFiles = FXCollections.observableList(CustomizedGson.gson.fromJson(ledgerFileReader, listType));
        } catch(FileNotFoundException fileNotFoundException) {
            System.out.println("No RecentLedgerFiles file. Creating an empty one");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        // Do not close the Application when the last showing window is hidden
        // However, Platform.exit() has to be called EXPLICITLY for the application to exit
        Platform.setImplicitExit(false);

        primaryStage = stage;

        changeToSplashScene();
    }

    public void changeToSplashScene() {
        primaryStage.hide();
        primaryStage.setTitle("Huisch Kasboek");
        primaryStage.setScene(new Scene(new SplashSceneRoot(this, splashLogo)));
        primaryStage.getScene().getStylesheets().addAll(
                CSS_STYLES,
                CSS_SPLASH
        );
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
        });
        primaryStage.show();
    }

    public void changeToApplicationScene(ApplicationSceneRoot root) {
        changeToApplicationScenePrivate(root, "Bolkhuisch Kasboek");
    }

    public void changeToApplicationScene(ApplicationSceneRoot root, String ledgerName) {
        changeToApplicationScenePrivate(root, "Bolkhuisch Kasboek - " + ledgerName);
    }

    private void changeToApplicationScenePrivate(ApplicationSceneRoot root, String title) {
        primaryStage.hide();
        primaryStage.setTitle(title);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(App.MIN_WIDTH);
        primaryStage.setMinHeight(App.MIN_HEIGHT);
        primaryStage.setScene(new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT));
        primaryStage.getScene().getStylesheets().add(App.CSS_STYLES);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void addRecentLedgerFile(RecentLedgerFile recentLedgerFile) {
        recentLedgerFiles.add(recentLedgerFile);
        // save the recent files
        saveRecentLedgersAsync();
    }

    public void removeRecentLedgerFile(RecentLedgerFile recentLedgerFile) {
        recentLedgerFiles.remove(recentLedgerFile);
        saveRecentLedgersAsync();
    }

    private void saveRecentLedgersAsync() {
        new Thread(() -> {
            try {
                String jsonString = CustomizedGson.gson.toJson(recentLedgerFiles, listType);
                BufferedWriter ledgerFileWriter = new BufferedWriter(new FileWriter(preferences.get(PREF_RECENT_LEDGERS_FILE, PREF_DEF_RECENT_LEDGERS_FILE)));
                ledgerFileWriter.write(jsonString);
                ledgerFileWriter.close();
            } catch (Exception e) {
                System.err.println("Error while saving recentLedgerFiles on different Thread");
                e.printStackTrace();
            }
        }).start();
    }

    public ObservableList<RecentLedgerFile> getRecentLedgerFiles() { return recentLedgerFiles; }

}