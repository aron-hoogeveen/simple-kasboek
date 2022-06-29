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

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class ErrorDialog extends Dialog<ButtonType> {
    public ErrorDialog(String contentText) {
        setTitle("Foutmelding");
        setContentText(contentText);

        // Do not set the Modality to APPLICATION_MODAL. If an error happens in an infinite loop, this would cause the program to become unusable
//        initModality(Modality.APPLICATION_MODAL);

        // Add a single ButtonType to the dialogpane
        getDialogPane().getButtonTypes().add(
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE)
        );
    }

}
