module ch.bolkhuis.guis {
    requires javafx.controls;
    requires org.jetbrains.annotations;
    requires org.controlsfx.controls;
    requires java.desktop;
    requires com.google.gson;
    requires org.apache.commons.lang3;
    requires org.apache.commons.io;
    requires java.prefs;
    exports ch.bolkhuis.kasboek;
    exports ch.bolkhuis.kasboek.core;
    exports ch.bolkhuis.kasboek.components;
}