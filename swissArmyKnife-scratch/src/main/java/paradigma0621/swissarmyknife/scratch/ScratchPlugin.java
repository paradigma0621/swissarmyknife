package paradigma0621.swissarmyknife.scratch;

import paradigma0621.swissarmyknife.api.ToolPlugin;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/** Implementação de plugin: cria view através de FXML somente quando solicitado. */
public class ScratchPlugin implements ToolPlugin {
    @Override public String id()   { return "scratch"; }
    @Override public String name() { return "Scratch Worker"; }

    @Override
    public Node createView() throws Exception {
        var url = getClass().getResource("/paradigma0621/swissarmyknife/scratch/ScratchView.fxml");
        return FXMLLoader.load(url); // carrega FXML sob demanda
    }

    @Override public String sayHelloFromToolPlugin() {
        return "plugin scratch";
    }

}
