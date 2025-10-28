package paradigma0621.swissarmyknife.host;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import paradigma0621.swissarmyknife.api.Comunicando;
import paradigma0621.swissarmyknife.api.ToolPlugin;

import java.util.*;
import java.util.function.Supplier;

public class MainController {

    // --- UI mapeada do FXML
    @FXML private BorderPane root;
    @FXML private ListView<ToolEntry> list;
    @FXML private TabPane tabs;
    @FXML private Button openBtn;

    // --- estado/serviços
    private final Map<String, ToolPlugin> pluginsById = new LinkedHashMap<>();
    private final Map<String, Supplier<Node>> lazyFactories = new LinkedHashMap<>();

    private Timeline debounceTimeline;
    private boolean isDebouncing = false;

    // cache LRU das views dos plugins
    private final Map<String, Node> lruCache = new java.util.LinkedHashMap<>(16, 0.75f, true) {
        private static final int MAX = 5;
        @Override protected boolean removeEldestEntry(Map.Entry<String, Node> eldest) {
            return size() > MAX;
        }
    };

    @FXML
    private void initialize() {
        // margem do ListView
        BorderPane.setMargin(list, new Insets(8));
        list.setPrefWidth(220);

        debounceTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> isDebouncing = false));
        debounceTimeline.setCycleCount(1);

        // carrega plugins (mostra contagem e id/nome)
        int count = 0;
        for (ToolPlugin p : ServiceLoader.load(ToolPlugin.class)) {
            System.out.println("[PLUGIN] " + p.id() + " - " + p.name());
            count++;
        }
        System.out.println("[PLUGIN] total=" + count);

        ServiceLoader<ToolPlugin> loader = ServiceLoader.load(ToolPlugin.class);
        List<ToolPlugin> plugins = new ArrayList<>();
        for (ToolPlugin p : loader) {
            plugins.add(p);
        }

        // Ordem fixa:
        List<String> pluginsOrder = List.of(
                "clipboard",
                "scratch",
                "comparator"
        );

        plugins.sort(Comparator.comparingInt(p ->
                pluginsOrder.indexOf(p.id()) >= 0 ? pluginsOrder.indexOf(p.id()) : Integer.MAX_VALUE
        ));

        // Agora popula seus mapas normalmente
        for (ToolPlugin p : plugins) {
            pluginsById.put(p.id(), p);
            lazyFactories.put(p.id(), () -> {
                try { return p.createView(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        }

        // preenche a lista com entries (id, name)
        list.setItems(FXCollections.observableArrayList(
                pluginsById.values().stream().map(p -> new ToolEntry(p.id(), p.name())).toList()
        ));

        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(ToolEntry item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });

        System.out.println(Comunicando.sayHelloFromAPI());

        // clique duplo abre o plugin
        list.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) openBtn.fire();
        });

        // registra o accelerator CTRL+P quando a Scene estiver disponível
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN),
                        this::openClipboardItem
                );

                newScene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                        this::openScratchItem
                );
            }
        });
    }

    // handler do botão "Abrir plugin selecionado"
    @FXML
    private void onOpenSelected(ActionEvent e) {
        openSelected();
    }

    // encapsula a lógica de abertura do plugin selecionado
    private void openSelected() {
        ToolEntry sel = list.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        // se já existe a aba, apenas seleciona
        for (Tab t : tabs.getTabs()) {
            if (sel.id().equals(t.getId())) {
                tabs.getSelectionModel().select(t);
                return;
            }
        }

        // cria/recupera a view (LRU)
        Node view = lruCache.get(sel.id());
        if (view == null) {
            Supplier<Node> factory = lazyFactories.get(sel.id());
            if (factory == null) {
                new Alert(Alert.AlertType.WARNING, "Plugin não encontrado: " + sel.name()).show();
                return;
            }
            view = factory.get();
            lruCache.put(sel.id(), view);
        }

        Tab tab = new Tab(sel.name(), view);
        tab.setId(sel.id());
        tab.setClosable(true);
        tab.setOnClosed(ev -> tab.setContent(new Region())); // ajuda o GC

        tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
    }

    private void openClipboardItem() {
        // queremos abrir especificamente o plugin "clipboard"
        String targetId = "clipboard";

        ToolEntry target = list.getItems()
                .stream()
                .filter(e -> e.id().equalsIgnoreCase(targetId))
                .findFirst()
                .orElse(null);

        if (target == null) {
            System.err.println("Plugin '" + targetId + "' não encontrado na lista.");
            return;
        }

        list.getSelectionModel().select(target);
        openSelected();
    }


    private void openScratchItem() {
        // queremos abrir especificamente o plugin "scratch"
        String targetId = "scratch";

        ToolEntry target = list.getItems()
                .stream()
                .filter(e -> e.id().equalsIgnoreCase(targetId))
                .findFirst()
                .orElse(null);

        if (target == null) {
            System.err.println("Plugin '" + targetId + "' não encontrado na lista.");
            return;
        }

        list.getSelectionModel().select(target);
        openSelected();
    }

    // pequeno DTO para a lista
    private record ToolEntry(String id, String name) {}

    @FXML
    public void onKeyPressed() {
        root.addEventHandler(KeyEvent.KEY_RELEASED, key -> {
            if (!isDebouncing) {
                isDebouncing = true;

              /*  if (key.isControlDown() || key.getCode().equals(KeyCode.CONTROL)) {
                    if ((key.getCode() == key.getCode().LEFT)) {
                        System.out.println("You pressed left arrow...");
                    }

                    if (key.getCode() == KeyCode.S) {
                        System.out.println("pressionou CTRL + S");
                    }
                }*/
                debounceTimeline.playFromStart();
            }

        });
    }
}
