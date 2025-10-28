package paradigma0621.swissarmyknife.clipboard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClipboardController {

    @FXML private VBox root;         // <--- referência ao container raiz (fx:id="root")
    @FXML private TextArea info;
    @FXML private Button btnCopy;

    // combinação de teclas para reuso
    private static final KeyCodeCombination CTRL_P =
            new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);

    @FXML
    private void initialize() {
        System.out.println("Dentro do initialize do plugin clipboard");

        info.setText("""
            Plugin: Copiar Senha
            Local padrão: ~/swissarmyknife/password.txt (uma linha)
            Atalho: CTRL + P
            """);

        // clique no botão
        btnCopy.setOnAction(e -> copy());

        // 1) Event Filter: consome CTRL+P antes do accelerator global do host
        root.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            if (CTRL_P.match(ev)) {
                copy();
                ev.consume(); // impede propagação para o host
            }
        });

        // 2) Accelerator na Scene: substitui/atualiza o mapping quando a Scene estiver pronta
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // registra/atualiza para que CTRL+P execute copy() enquanto este plugin estiver visível
                newScene.getAccelerators().put(CTRL_P, this::copy);
            }
        });
    }

    private void copy() {
        System.out.println("Dentro do copy do plugin clipboard");
        String pwd = readPasswordOrFallback();

        ClipboardContent cc = new ClipboardContent();
        cc.putString(pwd);
        boolean ok = Clipboard.getSystemClipboard().setContent(cc);
        info.setText(info.getText() + "COPIED!!");
    }

    private String readPasswordOrFallback() {
        System.out.println("Dentro do readPasswordOrFallback do plugin clipboard");
        try {
            Path p = Path.of(System.getProperty("user.home"), "swissarmyknife", "password.txt");
            if (Files.exists(p)) {
                return Files.readString(p, StandardCharsets.UTF_8)
                        .lines()
                        .findFirst()
                        .orElse("")
                        .trim();
            }
        } catch (Exception ignored) { }
        return "SENHA_PADRAO_123!";
    }
}
