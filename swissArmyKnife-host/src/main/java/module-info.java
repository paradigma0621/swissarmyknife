module paradigma0621.swissarmyknife.host {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.base;
    requires javafx.graphics;

    requires paradigma0621.swissarmyknife.api;

    uses paradigma0621.swissarmyknife.api.ToolPlugin;

    requires paradigma0621.swissarmyknife.clipboard; // CHAVE KEY DE OURO - ChatGPT não há previu, mas precisava

    exports paradigma0621.swissarmyknife.host;
    opens paradigma0621.swissarmyknife.host to javafx.fxml;
}