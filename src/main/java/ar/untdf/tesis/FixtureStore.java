package ar.untdf.tesis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

@ApplicationScoped
public class FixtureStore {

    @ConfigProperty(name = "mock.fixtures.path", defaultValue = "servicio-fixtures.json")
    String fixturesPath;

    @Inject
    ObjectMapper mapper;

    private ObjectNode root;

    @PostConstruct
    void init() {
        try {
            Path path = Path.of(fixturesPath).normalize();
            byte[] content = Files.readAllBytes(path);
            JsonNode node = mapper.readTree(content);
            if (!(node instanceof ObjectNode objectNode)) {
                throw new IllegalStateException("El fixture debe ser un objeto JSON");
            }
            this.root = objectNode;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo de fixtures: " + fixturesPath, e);
        }
    }

    public ObjectNode metadata() {
        JsonNode metadata = root.path("metadata");
        if (metadata instanceof ObjectNode objectNode) {
            return objectNode;
        }
        return mapper.createObjectNode();
    }

    public ArrayNode findNormativa(ObjectNode payload) {
        return findByKey("consultar_normativa", payload, true);
    }

    public ArrayNode findTramites(ObjectNode payload) {
        return findByKey("candidatos_de_tramite", payload, false);
    }

    public ArrayNode findNormativaPorTramite(Integer codTramite, String vigenteA) {
        JsonNode cases = root.path("normativa_tramite");
        if (!cases.isArray()) {
            return mapper.createArrayNode();
        }

        String normalizedVigenteA = normalizeNullable(vigenteA);
        ObjectNode exactCase = null;
        ObjectNode codeCase = null;

        Iterator<JsonNode> it = cases.elements();
        while (it.hasNext()) {
            JsonNode item = it.next();
            if (!(item instanceof ObjectNode caseNode)) {
                continue;
            }

            JsonNode requestNode = caseNode.path("request");
            if (requestNode.path("cod_tramite").asInt(Integer.MIN_VALUE) != codTramite) {
                continue;
            }

            if (codeCase == null) {
                codeCase = caseNode;
            }

            if (normalizedVigenteA == null) {
                continue;
            }

            String caseVigenteA = normalizeNullable(requestNode.path("filtros").path("vigente_a").asText(null));
            if (normalizedVigenteA.equals(caseVigenteA)) {
                exactCase = caseNode;
                break;
            }
        }

        ObjectNode selected = exactCase != null ? exactCase : codeCase;
        if (selected == null) {
            return mapper.createArrayNode();
        }

        JsonNode responseNode = selected.path("response");
        if (responseNode instanceof ArrayNode responseArray) {
            return responseArray;
        }
        return mapper.createArrayNode();
    }

    private ArrayNode findByKey(String key, ObjectNode payload, boolean compareFilters) {
        String text = normalize(payload.path("texto").asText(""));
        JsonNode filters = payload.get("filtros");

        ObjectNode exactCase = null;
        ObjectNode textCase = null;

        JsonNode cases = root.path(key);
        if (!cases.isArray()) {
            return mapper.createArrayNode();
        }

        Iterator<JsonNode> it = cases.elements();
        while (it.hasNext()) {
            JsonNode item = it.next();
            if (!(item instanceof ObjectNode caseNode)) {
                continue;
            }

            JsonNode requestNode = caseNode.path("request");
            String caseText = normalize(requestNode.path("texto").asText(""));
            if (!caseText.equals(text)) {
                continue;
            }

            if (!compareFilters) {
                exactCase = caseNode;
                break;
            }

            JsonNode caseFilters = requestNode.get("filtros");
            if (sameFilters(filters, caseFilters)) {
                exactCase = caseNode;
                break;
            }

            if (textCase == null) {
                textCase = caseNode;
            }
        }

        ObjectNode selected = exactCase != null ? exactCase : textCase;
        if (selected == null) {
            return mapper.createArrayNode();
        }

        JsonNode responseNode = selected.path("response");
        if (!(responseNode instanceof ArrayNode responseArray)) {
            return mapper.createArrayNode();
        }

        int k = payload.path("k").asInt(-1);
        if (k <= 0 || k >= responseArray.size()) {
            return responseArray;
        }

        ArrayNode cut = mapper.createArrayNode();
        for (int i = 0; i < k; i++) {
            cut.add(responseArray.get(i));
        }
        return cut;
    }

    private static boolean sameFilters(JsonNode left, JsonNode right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isBlank() ? null : normalized;
    }
}
