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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
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

        // popula mapas e fábricas lazy
        ServiceLoader<ToolPlugin> loader = ServiceLoader.load(ToolPlugin.class);
        for (ToolPlugin p : loader) {
            System.out.println("Está no p.sayHelloFromToolPlugin(): " + p.sayHelloFromToolPlugin());
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
                        this::openFirstItem // abre o primeiro item da lista
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

    // abre o primeiro item da lista (ex.: clipboard) e o seleciona
    private void openFirstItem() {
        if (list.getItems().isEmpty()) return;
        list.getSelectionModel().select(0);
        openSelected();
    }

    // (Opcional) abrir diretamente por ID conhecido (caso não queira depender da ordem)
    // Ex.: openById("paradigma0621.swissarmyknife.clipboard");
    private void openById(String id) {
        int idx = -1;
        for (int i = 0; i < list.getItems().size(); i++) {
            if (list.getItems().get(i).id().equals(id)) { idx = i; break; }
        }
        if (idx >= 0) {
            list.getSelectionModel().select(idx);
            openSelected();
        }
    }

    // pequeno DTO para a lista
    private record ToolEntry(String id, String name) {}

    // Se tiver um onKeyPressed no FXML, pode deixar vazio para não conflitar com o accelerator:
    @FXML
    public void onKeyPressed() {
        root.addEventHandler(KeyEvent.KEY_RELEASED, key -> {
            if (!isDebouncing) {
                isDebouncing = true;
                boolean showCtrlMessage = true;

                if (key.isControlDown() || key.getCode().equals(KeyCode.CONTROL)) {
                    if ((key.getCode() == key.getCode().LEFT)) {
                        System.out.println("You pressed left arrow...");
                    }
                    if (key.getCode() == key.getCode().RIGHT) {
                        System.out.println("You pressed right arrow...");
                    }

                    if (key.getCode() == KeyCode.S) {
                        System.out.println("pressionou CTRL + S");
                    }


                }
                debounceTimeline.playFromStart();
            }

        });
    }
}
