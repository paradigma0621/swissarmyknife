package paradigma0621.swissarmyknife.api;

import javafx.scene.Node;

public interface ToolPlugin {
    String id();
    String name();
    Node createView() throws Exception;
    String sayHelloFromToolPlugin();
}
