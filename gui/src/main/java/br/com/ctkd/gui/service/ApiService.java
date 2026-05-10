package br.com.ctkd.gui.service;

import br.com.ctkd.gui.model.AddressModel;
import br.com.ctkd.gui.model.ClientModel;
import br.com.ctkd.gui.model.OccurrenceModel;
import br.com.ctkd.gui.model.PhotoModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiService {

    private static final ApiService INSTANCE = new ApiService();

    public static ApiService getInstance() {
        return INSTANCE;
    }

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private ApiService() {}

    // ── Auth ──────────────────────────────────────────────────────────────────

    public String login(String username, String password) throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", username, "password", password));
        HttpResponse<String> res = post("/api/v1/auth/login", body, null);
        if (res.statusCode() != 200) throw new RuntimeException("Invalid credentials (HTTP " + res.statusCode() + ")");
        return mapper.readTree(res.body()).get("token").asText();
    }

    // ── Clients ───────────────────────────────────────────────────────────────

    public List<ClientModel> listClients(String token) throws Exception {
        HttpResponse<String> res = get("/api/v1/clients", token);
        assertOk(res, 200);
        List<ClientModel> result = new ArrayList<>();
        for (JsonNode n : mapper.readTree(res.body())) {
            result.add(parseClient(n));
        }
        return result;
    }

    public ClientModel createClient(String name, LocalDate birthdate, String cpf, String token) throws Exception {
        String body = mapper.writeValueAsString(Map.of("name", name, "birthdate", birthdate.toString(), "cpf", cpf));
        HttpResponse<String> res = post("/api/v1/clients", body, token);
        assertOk(res, 201);
        return parseClient(mapper.readTree(res.body()));
    }

    public ClientModel updateClient(String id, String name, LocalDate birthdate, String cpf, String token) throws Exception {
        String body = mapper.writeValueAsString(Map.of("name", name, "birthdate", birthdate.toString(), "cpf", cpf));
        HttpResponse<String> res = put("/api/v1/clients/" + id, body, token);
        assertOk(res, 202);
        return parseClient(mapper.readTree(res.body()));
    }

    public void deleteClient(String id, String token) throws Exception {
        HttpResponse<String> res = delete("/api/v1/clients/" + id, token);
        assertOk(res, 204);
    }

    // ── Addresses ─────────────────────────────────────────────────────────────

    public List<AddressModel> listAddresses(String token) throws Exception {
        HttpResponse<String> res = get("/api/v1/address", token);
        assertOk(res, 200);
        List<AddressModel> result = new ArrayList<>();
        for (JsonNode n : mapper.readTree(res.body())) {
            result.add(parseAddress(n));
        }
        return result;
    }

    public AddressModel createAddress(String streetName, String neighborhood, String zipCode,
                                      String city, String state, String token) throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "streetName", streetName, "neighborhood", neighborhood,
                "zipCode", zipCode, "city", city, "state", state));
        HttpResponse<String> res = post("/api/v1/address", body, token);
        assertOk(res, 201);
        return parseAddress(mapper.readTree(res.body()));
    }

    public AddressModel updateAddress(String id, String streetName, String neighborhood, String zipCode,
                                      String city, String state, String token) throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "streetName", streetName, "neighborhood", neighborhood,
                "zipCode", zipCode, "city", city, "state", state));
        HttpResponse<String> res = put("/api/v1/address/" + id, body, token);
        assertOk(res, 202);
        return parseAddress(mapper.readTree(res.body()));
    }

    public void deleteAddress(String id, String token) throws Exception {
        HttpResponse<String> res = delete("/api/v1/address/" + id, token);
        assertOk(res, 204);
    }

    // ── Occurrences ───────────────────────────────────────────────────────────

    public List<OccurrenceModel> listOccurrences(String token) throws Exception {
        HttpResponse<String> res = get("/api/v1/occurrences", token);
        assertOk(res, 200);
        List<OccurrenceModel> result = new ArrayList<>();
        for (JsonNode n : mapper.readTree(res.body())) {
            result.add(parseOccurrence(n));
        }
        return result;
    }

    public OccurrenceModel createOccurrence(String clientId, String addressId, LocalDate date, String token) throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "clientId", clientId, "addressId", addressId, "occurrenceDate", date.toString()));
        HttpResponse<String> res = postMultipart("/api/v1/occurrences", body, token);
        assertOk(res, 201);
        return parseOccurrence(mapper.readTree(res.body()));
    }

    public List<PhotoModel> listPhotosForOccurrence(String occurrenceId, String token) throws Exception {
        HttpResponse<String> res = get("/api/v1/photos/occurrences/" + occurrenceId, token);
        assertOk(res, 200);
        List<PhotoModel> result = new ArrayList<>();
        for (JsonNode n : mapper.readTree(res.body())) {
            result.add(parsePhoto(n));
        }
        return result;
    }

    public PhotoModel uploadPhoto(String occurrenceId, File file, String token) throws Exception {
        byte[] bytes = Files.readAllBytes(file.toPath());
        String mimeType = detectMimeType(file.getName());
        String boundary = "----FormBoundary" + System.currentTimeMillis();
        String partHeader = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"photo\"; filename=\"" + file.getName() + "\"\r\n"
                + "Content-Type: " + mimeType + "\r\n\r\n";
        String partFooter = "\r\n--" + boundary + "--\r\n";
        byte[] hBytes = partHeader.getBytes();
        byte[] fBytes = partFooter.getBytes();
        byte[] body = new byte[hBytes.length + bytes.length + fBytes.length];
        System.arraycopy(hBytes, 0, body, 0, hBytes.length);
        System.arraycopy(bytes, 0, body, hBytes.length, bytes.length);
        System.arraycopy(fBytes, 0, body, hBytes.length + bytes.length, fBytes.length);

        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/v1/photos/occurrences/" + occurrenceId))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));
        if (token != null) req.header("Authorization", "Bearer " + token);
        HttpResponse<String> res = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
        assertOk(res, 201);
        return parsePhoto(mapper.readTree(res.body()));
    }

    public OccurrenceModel closeOccurrence(String id, String token) throws Exception {
        HttpResponse<String> res = patch("/api/v1/occurrences/" + id, token);
        assertOk(res, 200);
        return parseOccurrence(mapper.readTree(res.body()));
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET();
        if (token != null) req.header("Authorization", "Bearer " + token);
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) req.header("Authorization", "Bearer " + token);
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postMultipart(String path, String jsonBody, String token) throws Exception {
        String boundary = "----FormBoundary" + System.currentTimeMillis();
        String multipart = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"data\"\r\n"
                + "Content-Type: application/json\r\n\r\n"
                + jsonBody + "\r\n"
                + "--" + boundary + "--\r\n";
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(multipart));
        if (token != null) req.header("Authorization", "Bearer " + token);
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> put(String path, String body, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) req.header("Authorization", "Bearer " + token);
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> patch(String path, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .method("PATCH", HttpRequest.BodyPublishers.noBody());
        if (token != null) req.header("Authorization", "Bearer " + token);
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE();
        if (token != null) req.header("Authorization", "Bearer " + token);
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    private ClientModel parseClient(JsonNode n) {
        return new ClientModel(
                text(n, "id"), text(n, "name"), text(n, "cpf"),
                parseDate(text(n, "birthdate")),
                parseDateTime(text(n, "creationDate")),
                parseDateTime(text(n, "updateDate")),
                n.path("deleted").asBoolean(false));
    }

    private AddressModel parseAddress(JsonNode n) {
        return new AddressModel(
                text(n, "id"), text(n, "streetName"), text(n, "neighborhood"),
                text(n, "zipCode"), text(n, "city"), text(n, "state"),
                parseDateTime(text(n, "creationDate")),
                parseDateTime(text(n, "updateDate")),
                n.path("deleted").asBoolean(false));
    }

    private OccurrenceModel parseOccurrence(JsonNode n) {
        LocalDate occurrenceDate = parseDate(text(n, "occurrenceDate"));
        OffsetDateTime creationDate = parseDateTime(text(n, "creationDate"));
        OffsetDateTime updateDate = parseDateTime(text(n, "updateDate"));

        JsonNode client = n.get("client");
        String clientId = client != null ? text(client, "id") : null;
        String clientName = client != null ? text(client, "name") : null;
        String clientCpf = client != null ? text(client, "cpf") : null;
        LocalDate clientBirthdate = client != null ? parseDate(text(client, "birthdate")) : null;

        JsonNode address = n.get("address");
        String addressId = address != null ? text(address, "id") : null;
        String addressStreet = address != null ? text(address, "streetName") : null;
        String addressNeighborhood = address != null ? text(address, "neighborhood") : null;
        String addressZip = address != null ? text(address, "zipCode") : null;
        String addressCity = address != null ? text(address, "city") : null;
        String addressState = address != null ? text(address, "state") : null;

        List<PhotoModel> photos = new ArrayList<>();
        JsonNode photosNode = n.get("photos");
        if (photosNode != null && photosNode.isArray()) {
            for (JsonNode p : photosNode) {
                photos.add(new PhotoModel(
                        text(p, "id"), text(p, "occurrenceId"),
                        text(p, "pathBucket"), text(p, "url"), text(p, "hash"),
                        parseDateTime(text(p, "creationDate")),
                        parseDateTime(text(p, "updateDate")),
                        p.path("deleted").asBoolean(false)));
            }
        }

        return new OccurrenceModel(
                text(n, "id"), occurrenceDate, text(n, "status"),
                creationDate, updateDate, n.path("deleted").asBoolean(false),
                clientId, clientName, clientCpf, clientBirthdate,
                addressId, addressStreet, addressNeighborhood, addressZip, addressCity, addressState,
                photos);
    }

    private PhotoModel parsePhoto(JsonNode n) {
        return new PhotoModel(
                text(n, "id"), text(n, "occurrenceId"),
                text(n, "pathBucket"), text(n, "url"), text(n, "hash"),
                parseDateTime(text(n, "creationDate")),
                parseDateTime(text(n, "updateDate")),
                n.path("deleted").asBoolean(false));
    }

    private String detectMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private OffsetDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try { return OffsetDateTime.parse(s); } catch (Exception e) { return null; }
    }

    private String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private void assertOk(HttpResponse<String> res, int expected) throws Exception {
        if (res.statusCode() != expected) {
            String detail = extractDetail(res.body());
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + detail);
        }
    }

    private String extractDetail(String body) {
        try {
            JsonNode n = mapper.readTree(body);
            if (n.has("message")) return n.get("message").asText();
        } catch (Exception ignored) {}
        return body.length() > 200 ? body.substring(0, 200) : body;
    }
}
