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

/**
 * PreferencesStrings contains static Strings to be used with the Preferences.
 */
public final class PreferencesStrings {
    /*
     * ApplicationSceneRoot
     */
    public static final String APPLICATIONSCENEROOT_NODE = "/ch/bolkhuis/kasboek/ApplicationSceneRoot";
    public static final String APPLICATIONSCENEROOT_FILE_CHOOSER_DIRECTORY = "FileChooserDirectory";
    public static final String APPLICATIONSCENEROOT_DEFAULT_FILE_CHOOSER_DIRECTORY = System.getProperty("user.home");

    /*
     * InvoicingDialog
     */
    public static final String INVOICINGDIALOG_INTRO_TEXT = "IntroText";
    public static final String INVOICINGDIALOG_DEFAULT_INTRO_TEXT = "Dit is de huischrekening van ${start_date} tot " +
            "${end_date}. Hieronder vind jouw nieuwe saldo. Als deze negatief is, gelieve het bedrag zo snel" +
            " mogelijk overmaken naar de huisch bankrekening.";
}
