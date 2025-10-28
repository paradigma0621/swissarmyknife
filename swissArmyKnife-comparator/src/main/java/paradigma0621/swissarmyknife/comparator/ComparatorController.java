package paradigma0621.swissarmyknife.comparator;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ComparatorController {

    // Entradas
    @FXML private TextArea txtA;
    @FXML private TextArea txtB;

    // Saídas diferenciais
    @FXML private TextArea txtOnlyA;
    @FXML private TextArea txtOnlyB;
    @FXML private TextArea txtInter;

    // Saídas duplicados
    @FXML private TextArea txtDupA;
    @FXML private TextArea txtDupB;

    // Labels
    @FXML private Label lblOnlyAInfo;
    @FXML private Label lblOnlyBInfo;
    @FXML private Label lblInterInfo;
    @FXML private Label lblDupAInfo;
    @FXML private Label lblDupBInfo;
    @FXML private Label lblResumo;

    @FXML private Button btnComparar;
    @FXML private Button btnLimpar;

    // Atalho: Ctrl+Enter para comparar
    private static final KeyCodeCombination CTRL_ENTER =
            new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    // Regex: separa por QUALQUER coisa que NÃO seja sinal (+/-) ou dígito
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[^+\\-\\d]+");

    @FXML
    private void initialize() {
        btnComparar.setOnAction(e -> comparar());
        btnLimpar.setOnAction(e -> limpar());

        // Accelerator na cena: Ctrl+Enter → comparar
        txtA.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(CTRL_ENTER, this::comparar);
            }
        });

        // Estado inicial
        resetLabels();
    }

    /** Executa a comparação e preenche as áreas de saída. */
    private void comparar() {
        // Parse mantendo repetição (lista) e também sets únicos
        List<Long> listA = parseList(txtA.getText());
        List<Long> listB = parseList(txtB.getText());

        Set<Long> setA = new TreeSet<>(listA);
        Set<Long> setB = new TreeSet<>(listB);

        // Diferenças e interseção
        TreeSet<Long> onlyA = new TreeSet<>(setA);
        onlyA.removeAll(setB);

        TreeSet<Long> onlyB = new TreeSet<>(setB);
        onlyB.removeAll(setA);

        TreeSet<Long> inter = new TreeSet<>(setA);
        inter.retainAll(setB);

        // Duplicados (por valor) — contar ocorrências e filtrar > 1
        LinkedHashMap<Long, Integer> dupA = counts(listA).entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        LinkedHashMap<Long, Integer> dupB = counts(listB).entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // Preencher UI
        txtOnlyA.setText(joinLines(onlyA));
        txtOnlyB.setText(joinLines(onlyB));
        txtInter.setText(joinLines(inter));

        txtDupA.setText(joinCounts(dupA));
        txtDupB.setText(joinCounts(dupB));

        lblOnlyAInfo.setText(onlyA.size() + " itens");
        lblOnlyBInfo.setText(onlyB.size() + " itens");
        lblInterInfo.setText(inter.size() + " itens");

        int dupAValues = dupA.size();
        int dupAExtras = dupA.values().stream().mapToInt(c -> c - 1).sum();
        lblDupAInfo.setText(dupAValues + " valores duplicados (" + dupAExtras + " ocorrências extras)");

        int dupBValues = dupB.size();
        int dupBExtras = dupB.values().stream().mapToInt(c -> c - 1).sum();
        lblDupBInfo.setText(dupBValues + " valores duplicados (" + dupBExtras + " ocorrências extras)");

        int uniquesA = setA.size();
        int uniquesB = setB.size();
        int uniquesTotal = new TreeSet<Long>() {{ addAll(setA); addAll(setB); }}.size();

        lblResumo.setText(String.format(
                "Resumo: A=%d | A únicos=%d | B=%d | B únicos=%d | Únicos totais=%d",
                listA.size(), uniquesA, listB.size(), uniquesB, uniquesTotal
        ));
    }

    /** Limpa entradas e saídas. */
    private void limpar() {
        txtA.clear();
        txtB.clear();
        txtOnlyA.clear();
        txtOnlyB.clear();
        txtInter.clear();
        txtDupA.clear();
        txtDupB.clear();
        resetLabels();
        txtA.requestFocus();
    }

    private void resetLabels() {
        lblOnlyAInfo.setText("0 itens");
        lblOnlyBInfo.setText("0 itens");
        lblInterInfo.setText("0 itens");
        lblDupAInfo.setText("0 valores duplicados (0 ocorrências extras)");
        lblDupBInfo.setText("0 valores duplicados (0 ocorrências extras)");
        lblResumo.setText("Resumo: A=0 | A únicos=0 | B=0 | B únicos=0 | Únicos totais=0");
    }

    /** Parser robusto: retorna lista (mantém duplicatas) de inteiros longos. */
    private List<Long> parseList(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        List<Long> out = new ArrayList<>();
        for (String token : SPLIT_PATTERN.split(raw.trim())) {
            if (token == null || token.isBlank()) continue;
            Long v = safeParseLong(token);
            if (v != null) out.add(v);
        }
        return out;
    }

    /** Conta ocorrências por valor. */
    private LinkedHashMap<Long, Integer> counts(List<Long> values) {
        LinkedHashMap<Long, Integer> map = new LinkedHashMap<>();
        for (Long v : values) {
            map.merge(v, 1, Integer::sum);
        }
        return map;
    }

    /** Converte string em Long com segurança (retorna null se inválida). */
    private Long safeParseLong(String s) {
        try {
            String t = s.trim();
            if (t.isEmpty()) return null;
            if (t.equals("+") || t.equals("-")) return null;
            return Long.parseLong(t);
        } catch (Exception e) {
            return null;
        }
    }

    /** Junta números em linhas. */
    private String joinLines(Iterable<Long> values) {
        StringBuilder sb = new StringBuilder();
        for (Long v : values) {
            sb.append(v).append('\n');
        }
        return sb.toString().trim();
    }

    /** Formata mapa valor→contagem como linhas "valor (×N)". */
    private String joinCounts(Map<Long, Integer> counts) {
        if (counts.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        counts.forEach((k, c) -> sb.append(k).append(" (×").append(c).append(")\n"));
        return sb.toString().trim();
    }
}
