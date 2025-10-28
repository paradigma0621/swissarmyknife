import paradigma0621.swissarmyknife.clipboard.ClipboardPlugin;

module paradigma0621.swissarmyknife.clipboard {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.base;
    requires javafx.graphics;
    requires java.datatransfer;

    requires paradigma0621.swissarmyknife.api;

    provides paradigma0621.swissarmyknife.api.ToolPlugin
            with ClipboardPlugin;  // registra o provedor

    opens paradigma0621.swissarmyknife.clipboard to javafx.fxml;
    exports paradigma0621.swissarmyknife.clipboard;
}

