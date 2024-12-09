module org.example.sqleksamenhelper {
    requires javafx.controls;
    requires javafx.fxml;
    requires tess4j;
    requires java.desktop;
    requires javafx.swing;
    requires org.slf4j;


    opens org.example.sqleksamenhelper to javafx.fxml;
    exports org.example.sqleksamenhelper;
}