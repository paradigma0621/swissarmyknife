import paradigma0621.swissarmyknife.scratch.ScratchPlugin;

module paradigma0621.swissarmyknife.scratch {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.base;
    requires javafx.graphics;
    requires java.datatransfer;

    requires paradigma0621.swissarmyknife.api;

    provides paradigma0621.swissarmyknife.api.ToolPlugin
            with ScratchPlugin;  // registra o provedor

    opens paradigma0621.swissarmyknife.scratch to javafx.fxml;
    exports paradigma0621.swissarmyknife.scratch;
}

