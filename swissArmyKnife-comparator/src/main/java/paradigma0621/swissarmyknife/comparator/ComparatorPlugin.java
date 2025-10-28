package paradigma0621.swissarmyknife.comparator;

import paradigma0621.swissarmyknife.api.ToolPlugin;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/** Implementação de plugin: cria view através de FXML somente quando solicitado. */
public class ComparatorPlugin implements ToolPlugin {
    @Override public String id()   { return "comparator"; }
    @Override public String name() { return "Comparar arrays"; }

    @Override
    public Node createView() throws Exception {
        var url = getClass().getResource("/paradigma0621/swissarmyknife/comparator/ComparatorView.fxml");
        return FXMLLoader.load(url); // carrega FXML sob demanda
    }

    @Override public String sayHelloFromToolPlugin() {
        return "plugin comparator";
    }

}
