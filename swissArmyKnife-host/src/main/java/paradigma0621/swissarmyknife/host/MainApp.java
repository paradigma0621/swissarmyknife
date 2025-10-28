package paradigma0621.swissarmyknife.host;

import paradigma0621.swissarmyknife.api.ToolPlugin;
import paradigma0621.swissarmyknife.api.Comunicando;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class MainApp extends Application {

    private final Map<String, ToolPlugin> pluginsById = new LinkedHashMap<>();
    private final Map<String, Supplier<javafx.scene.Node>> lazyFactories = new LinkedHashMap<>();
    private final Map<String, javafx.scene.Node> lruCache = new java.util.LinkedHashMap<>(16, 0.75f, true) {
        private static final int MAX = 5;
        @Override protected boolean removeEldestEntry(Map.Entry<String, javafx.scene.Node> eldest) {
            return size() > MAX;
        }
    };

    @Override
    public void start(Stage stage) {
        ServiceLoader<ToolPlugin> loader = ServiceLoader.load(ToolPlugin.class);

        int count = 0;
        for (ToolPlugin p : ServiceLoader.load(ToolPlugin.class)) {
            System.out.println("[PLUGIN] " + p.id() + " - " + p.name());
            count++;
        }

        System.out.println("[PLUGIN] total=" + count);

        for (ToolPlugin p : loader) {
            System.out.println("Está no p.sayHelloFromToolPlugin(): " + p.sayHelloFromToolPlugin());
            pluginsById.put(p.id(), p);
            lazyFactories.put(p.id(), () -> {
                try { return p.createView(); } catch (Exception e) { throw new RuntimeException(e); }
            });
        }

        ListView<ToolEntry> list = new ListView<>();
        list.setItems(FXCollections.observableArrayList(
                pluginsById.values().stream().map(p -> new ToolEntry(p.id(), p.name())).toList()
        ));
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(ToolEntry item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });

        BorderPane root = new BorderPane();
        root.setLeft(list);
        BorderPane.setMargin(list, new Insets(8));
        list.setPrefWidth(220);

        TabPane tabs = new TabPane();
        root.setCenter(tabs);

        System.out.println(Comunicando.sayHelloFromAPI());
        Button openBtn = new Button("Abrir plugin selecionado");
        openBtn.setOnAction(e -> {
            ToolEntry sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) return;

            for (Tab t : tabs.getTabs()) {
                if (sel.id().equals(t.getId())) {
                    tabs.getSelectionModel().select(t);
                    return;
                }
            }

            javafx.scene.Node view = lruCache.get(sel.id());
            if (view == null) {
                view = lazyFactories.get(sel.id()).get();
                lruCache.put(sel.id(), view);
            }

            Tab tab = new Tab(sel.name(), view);
            tab.setId(sel.id());
            tab.setClosable(true);
            tab.setOnClosed(ev -> tab.setContent(new Region()));

            tabs.getTabs().add(tab);
            tabs.getSelectionModel().select(tab);
        });

        ToolBar tb = new ToolBar(openBtn, new Separator(), new Label("Dica: clique duplo para abrir"));
        root.setTop(tb);

        list.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) openBtn.fire();
        });

        stage.setTitle("SwissArmyKnife — Host + Plugins (Lazy)");
        stage.setScene(new Scene(root, 1000, 650));
        stage.show();
    }

    private record ToolEntry(String id, String name) {}

    public static void main(String[] args) {
        launch(args);
    }
}