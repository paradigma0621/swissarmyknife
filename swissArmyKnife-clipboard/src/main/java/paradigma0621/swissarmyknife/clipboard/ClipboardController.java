package paradigma0621.swissarmyknife.clipboard;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClipboardController {
    @FXML private TextArea info;
    @FXML private Button btnCopy;

    @FXML
    private void initialize() {
        System.out.println("Dentro do initialize do plugin clipboard");
        info.setText("""
            Plugin: Copiar Senha
            Local padrão: ~/swissarmyknife/password.txt (uma linha)
            """);
        btnCopy.setOnAction(e -> copy());
    }

    private void copy() {
        System.out.println("Dentro do copy do plugin clipboard");
        String pwd = readPasswordOrFallback();
        ClipboardContent cc = new ClipboardContent();
        cc.putString(pwd);
        boolean ok = Clipboard.getSystemClipboard().setContent(cc);

        Alert a = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setTitle(ok ? "Copiado" : "Erro");
        a.setContentText(ok ? "Senha copiada para a área de transferência."
                : "Falha ao copiar.");
        a.show();
    }

    private String readPasswordOrFallback() {
        System.out.println("Dentro do readPasswordOrFallback do plugin clipboard");
        try {
            Path p = Path.of(System.getProperty("user.home"), "swissarmyknife", "password.txt");
            if (Files.exists(p)) {
                return Files.readString(p, StandardCharsets.UTF_8).lines().findFirst().orElse("").trim();
            }
        } catch (Exception ignored) {}
        return "SENHA_PADRAO_123!";
    }
}
