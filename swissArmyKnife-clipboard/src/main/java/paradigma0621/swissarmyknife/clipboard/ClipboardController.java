package paradigma0621.swissarmyknife.clipboard;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClipboardController {

    @FXML private VBox root;         // VBox raiz do FXML (fx:id="root")
    @FXML private TextArea info;
    @FXML private Button btnCopy;

    private static final KeyCodeCombination CTRL_P =
            new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);

    private static final KeyCodeCombination CTRL_S =
            new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);

    private static final Path PASSWORD_FILE =
            Path.of(System.getProperty("user.home"), "swissarmyknife", "password.txt");

    @FXML
    private void initialize() {
        System.out.println("Dentro do initialize do plugin clipboard");

        info.setText("""
            Plugin: Copiar/SALVAR Senha
            Local padrão: ~/swissarmyknife/password.txt (uma linha)
            Atalhos:
              • CTRL + P → copiar senha do arquivo para a área de transferência
              • CTRL + S → abrir diálogo e SALVAR nova senha no arquivo
            """);

        // Botão existente segue copiando
        btnCopy.setOnAction(e -> copy());

        // Event Filter (prioridade ao plugin)
        root.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            if (CTRL_P.match(ev)) {
                copy();
                ev.consume();
            } else if (CTRL_S.match(ev)) {
                savePasswordInteractive();
                ev.consume();
            }
        });

        // Accelerators na Scene (reforço, caso o foco esteja em outro controle)
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(CTRL_P, this::copy);
                newScene.getAccelerators().put(CTRL_S, this::savePasswordInteractive);
            }
        });
    }

    /** Lê a senha do arquivo e copia para a área de transferência. */
    private void copy() {
        System.out.println("Dentro do copy do plugin clipboard");
        String pwd = readPasswordOrFallback();

        ClipboardContent cc = new ClipboardContent();
        cc.putString(pwd);
        boolean ok = Clipboard.getSystemClipboard().setContent(cc);

        if (!ok) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Erro");
            a.setContentText("Falha ao copiar para a área de transferência.");
            a.show();
        } else {
            info.setText(info.getText() + "COPIED!");
        }
    }

    /** Abre um diálogo para o usuário digitar a senha e salva no arquivo. */
    private void savePasswordInteractive() {
        System.out.println("Abrindo diálogo de salvar senha (CTRL+S)");

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Salvar senha");
        dialog.setHeaderText("Informe a nova senha a ser armazenada (primeira linha do arquivo).");

        ButtonType salvarButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvarButtonType, ButtonType.CANCEL);

        PasswordField pwdField = new PasswordField();
        pwdField.setPromptText("Senha");

        // Layout simples
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 10, 10, 10));

        grid.add(new Label("Senha:"), 0, 0);
        grid.add(pwdField, 1, 0);

        Node saveButton = dialog.getDialogPane().lookupButton(salvarButtonType);
        saveButton.setDisable(true);

        // Habilita "Salvar" quando há algo digitado
        pwdField.textProperty().addListener((o, oldV, newV) -> {
            saveButton.setDisable(newV == null || newV.isBlank());
        });

        dialog.getDialogPane().setContent(grid);

        // Foco inicial
        dialog.setOnShown(e -> pwdField.requestFocus());

        // Resultado: string da senha
        dialog.setResultConverter(btn -> {
            if (btn == salvarButtonType) {
                return pwdField.getText();
            }
            return null;
        });

        String pwd = dialog.showAndWait().orElse(null);
        if (pwd == null) {
            // cancelado
            return;
        }

        boolean ok = savePasswordToFile(pwd);
        if (!ok) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Erro");
            a.setContentText("Falha ao salvar a senha no arquivo.");
            a.show();
        } else {
            info.setText("Senha salva em: " + PASSWORD_FILE);
        }
    }

    /** Persiste a senha no arquivo (apenas a primeira linha é relevante). */
    private boolean savePasswordToFile(String pwd) {
        try {
            // garante diretório
            if (PASSWORD_FILE.getParent() != null) {
                Files.createDirectories(PASSWORD_FILE.getParent());
            }
            // grava apenas a senha (uma linha)
            Files.writeString(PASSWORD_FILE, (pwd == null ? "" : pwd) + System.lineSeparator(),
                    StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Lê a primeira linha do arquivo de senha; se não existir, retorna fallback. */
    private String readPasswordOrFallback() {
        System.out.println("Dentro do readPasswordOrFallback do plugin clipboard");
        try {
            if (Files.exists(PASSWORD_FILE)) {
                return Files.readString(PASSWORD_FILE, StandardCharsets.UTF_8)
                        .lines()
                        .findFirst()
                        .orElse("")
                        .trim();
            }
        } catch (Exception ignored) { }
        return "SENHA_PADRAO_123!";
    }
}
