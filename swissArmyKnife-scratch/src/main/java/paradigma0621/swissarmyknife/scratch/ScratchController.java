package paradigma0621.swissarmyknife.scratch;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class ScratchController {

    // UI
    @FXML private TextField txtConfigPath;
    @FXML private TextField txtBaseDir;
    @FXML private TextField txtScratchFile;
    @FXML private TextArea  txtContent;
    @FXML private Button    btnSave;
    @FXML private Button    btnLoad;
    @FXML private Button    btnClear;
    @FXML private Label     lblStatus;

    // Atalhos
    private static final KeyCodeCombination CTRL_S =
            new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private static final KeyCodeCombination CTRL_L =
            new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);

    // Locais candidatos para pathConfiguration.txt
    private Path configFilePath; // onde encontrou pathConfiguration.txt
    private Path baseDir;        // diretório base resolvido (do conteúdo do pathConfiguration.txt)
    private Path scratchFile;    // baseDir/scratch.txt

    @FXML
    private void initialize() {
        // Handlers
        btnSave.setOnAction(e -> saveScratch());
        btnLoad.setOnAction(e -> loadScratch());
        btnClear.setOnAction(e -> { txtContent.clear(); setStatus("Conteúdo limpo."); });

        // Detecta config e atualiza UI
        resolveConfiguration();
        updateUiPaths();

        // Atalhos na Scene
        txtContent.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(CTRL_S, this::saveScratch);
                newScene.getAccelerators().put(CTRL_L, this::loadScratch);
            }
        });

        setStatus("Pronto.");
    }

    /* =========================
       CORE
       ========================= */

    /** Procura pathConfiguration.txt e define baseDir/scratchFile. */
    private void resolveConfiguration() {
        // 1) Tenta em ~/swissarmyknife/pathConfiguration.txt
        Path home = Path.of(System.getProperty("user.home"));
        Path candidateHome = home.resolve("swissarmyknife").resolve("pathConfiguration.txt");

        // 2) Tenta em ./pathConfiguration.txt (diretório de execução)
        Path candidateCwd = Path.of("pathConfiguration.txt").toAbsolutePath().normalize();

        if (Files.exists(candidateHome)) {
            configFilePath = candidateHome;
        } else if (Files.exists(candidateCwd)) {
            configFilePath = candidateCwd;
        } else {
            // Não encontrou: define um "virtual", mas ainda mostramos na UI
            configFilePath = candidateHome; // preferimos o padrão do home
        }

        // Lê a primeira linha (não vazia) do pathConfiguration.txt, se existir
        String dirLine = readFirstNonEmptyLine(configFilePath);
        if (dirLine == null || dirLine.isBlank()) {
            // Fallback: ~/swissarmyknife
            baseDir = home.resolve("swissarmyknife");
        } else {
            baseDir = resolvePathString(dirLine.trim());
        }

        scratchFile = baseDir.resolve("scratch.txt");
    }

    /** Atualiza os campos de UI com os caminhos resolvidos. */
    private void updateUiPaths() {
        txtConfigPath.setText(safePathString(configFilePath));
        txtBaseDir.setText(safePathString(baseDir));
        txtScratchFile.setText(safePathString(scratchFile));
    }

    /** Salva o conteúdo do TextArea em scratch.txt (criando diretórios se preciso). */
    private void saveScratch() {
        try {
            if (baseDir != null) {
                Files.createDirectories(baseDir);
            }
            Files.writeString(scratchFile, txtContent.getText(), StandardCharsets.UTF_8,
                              StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            setStatus("Salvo em: " + scratchFile);
            showInfo("Salvo", "Conteúdo salvo com sucesso em:\n" + scratchFile);
        } catch (Exception e) {
            setStatus("Falha ao salvar: " + e.getMessage());
            showError("Erro ao salvar", e);
        }
    }

    /** Carrega o conteúdo de scratch.txt para o TextArea, se existir. */
    private void loadScratch() {
        try {
            if (!Files.exists(scratchFile)) {
                setStatus("Arquivo não existe: " + scratchFile);
                showWarn("Arquivo não encontrado", "O arquivo não existe:\n" + scratchFile);
                return;
            }
            String data = Files.readString(scratchFile, StandardCharsets.UTF_8);
            txtContent.setText(data);
            setStatus("Carregado de: " + scratchFile);
        } catch (Exception e) {
            setStatus("Falha ao carregar: " + e.getMessage());
            showError("Erro ao carregar", e);
        }
    }

    /* =========================
       UTIL
       ========================= */

    /** Lê a primeira linha não vazia do arquivo (retorna null se não existir ou vazio). */
    private String readFirstNonEmptyLine(Path file) {
        try {
            if (file == null || !Files.exists(file)) return null;
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String ln : lines) {
                if (ln != null && !ln.trim().isEmpty()) {
                    return ln.trim();
                }
            }
        } catch (IOException ignored) { }
        return null;
    }

    /** Expande "~" e normaliza caminho; aceita absoluto ou relativo. */
    private Path resolvePathString(String raw) {
        String s = raw;
        if (s.startsWith("~")) {
            s = System.getProperty("user.home") + s.substring(1);
        }
        Path p = Path.of(s);
        if (!p.isAbsolute()) {
            p = p.toAbsolutePath();
        }
        return p.normalize();
    }

    private String safePathString(Path p) {
        return p == null ? "" : p.toString();
    }

    private void setStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }

    private void showInfo(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.show();
    }

    private void showWarn(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.show();
    }

    private void showError(String title, Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(e.getMessage() == null ? e.toString() : e.getMessage());
        a.show();
        e.printStackTrace();
    }
}

