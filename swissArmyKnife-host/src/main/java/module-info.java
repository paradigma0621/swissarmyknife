module paradigma0621.swissarmyknife.host {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    // seus módulos/próprias libs
    requires paradigma0621.swissarmyknife.api;
    requires javafx.base;
    uses paradigma0621.swissarmyknife.api.ToolPlugin;

    // o JavaFX usa reflexão para injetar @FXML
    opens paradigma0621.swissarmyknife.host to javafx.fxml;
    exports paradigma0621.swissarmyknife.host;
}