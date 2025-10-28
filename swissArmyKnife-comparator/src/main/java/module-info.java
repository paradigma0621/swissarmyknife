import paradigma0621.swissarmyknife.comparator.ComparatorPlugin;

module paradigma0621.swissarmyknife.comparator {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.base;
    requires javafx.graphics;
    requires java.datatransfer;

    requires paradigma0621.swissarmyknife.api;

    provides paradigma0621.swissarmyknife.api.ToolPlugin
            with ComparatorPlugin;  // registra o provedor

    opens paradigma0621.swissarmyknife.comparator to javafx.fxml;
    exports paradigma0621.swissarmyknife.comparator;
}

