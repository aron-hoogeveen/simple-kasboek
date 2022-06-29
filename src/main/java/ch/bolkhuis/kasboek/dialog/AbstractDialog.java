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

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * AbstractDialog is the abstract class for implementing a simple construction or edit dialog for class {@code T}.
 *
 * @param <T> the type that this AbstractDialog returns
 */
public abstract class AbstractDialog<T> {
    protected Stage stage;

    private final static BorderStroke errorBorderStroke = new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1));
    private final static BorderStroke correctBorderStroke = new BorderStroke(Color.LIGHTGREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1));

    protected final static Border errorBorder = new Border(errorBorderStroke);
    protected final static Border correctBorder = new Border(correctBorderStroke);

    protected final static String numberRegex = "^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$";

    /**
     * The old T to edit if any, otherwise create a new T.
     */
    protected final T old;

    /**
     * Holds the newly created object of type T if any.
     */
    protected Optional<T> result = Optional.empty();

    /**
     * Creates a new AbstractDialog and initialises its owner.
     *
     * @param owner owner to be used for the stage
     */
    public AbstractDialog(@NotNull Window owner) {
        old = null;

        // Initialise the stage
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
    }

    /**
     * Creates a new AbstractDialog and initialises its owner and the old T to load.
     *
     * @param owner owner to be used for the stage
     * @param old T to be edited
     */
    public AbstractDialog(@NotNull Window owner, @NotNull T old) {
        this.old = old;

        // Initialise the stage
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
    }

    /**
     * Initialises and sets a root to a new Scene and sets the scene to be used by the stage.
     */
    protected abstract void initAppearance();

    /**
     * Sets behaviours of components that need them. E.g. sets action handlers.
     */
    protected abstract void initBehaviour();

    /**
     * Shows the stage and returns immediately.
     */
    public void show() {
        stage.show();
    }

    /**
     * Shows the stage and blocks until it is closed.
     */
    public Optional<T> showAndWait() {
        stage.showAndWait();
        return result;
    }
}
