package paradigma0621.swissarmyknife.clipboard;

import paradigma0621.swissarmyknife.api.ToolPlugin;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/** Implementação de plugin: cria view através de FXML somente quando solicitado. */
public class ClipboardPlugin implements ToolPlugin {
    @Override public String id()   { return "clipboard"; }
    @Override public String name() { return "Copiar Senha"; }

    @Override
    public Node createView() throws Exception {
        var url = getClass().getResource("/paradigma0621/swissarmyknife/clipboard/ClipboardView.fxml");
        return FXMLLoader.load(url); // carrega FXML sob demanda
    }

    @Override public String sayHelloFromToolPlugin() {
        return "plugin está sendo iniciado ok";
    }

}
