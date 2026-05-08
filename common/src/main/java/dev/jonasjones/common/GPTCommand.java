package dev.jonasjones.common;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GPTCommand
{

    private static final Logger LOGGER =
            LoggerFactory.getLogger("buildgpt");

    private static String API_KEY_FILE = ".openaikey";

    public static final String GPT_PROMPT_BOUND = "Imagine, You're an architect. Design the structure of a minecraft " +
            "%s within the coordinate range x: %d-%d, y: %d-%d, z: %d-%d in minecraft. Return the blocks in a json " +
            "list of objects {x:1,y:1,z:1,block:minecraft:block}. Return only the json without any formatting or " +
            "explanation as plaintext.";

    public static final String GPT_PROMPT_UNBOUND = "Imagine, You're an architect. Design the structure of a %s at " +
            "the coordinate x: %d, y: %d, z: %d in minecraft. Return the blocks in a json list of objects " +
            "{x:1,y:1,z:1,block:minecraft:block}. Return only the json without any formatting or explanation as " +
            "plaintext.";

    public static void setApiKeyFile(String path) {
        API_KEY_FILE = path;
    }

    public interface CommandHandler {
        void sendMessage(String message);
        void sendError(String message);
        void executeSetblock(int x, int y, int z, String blockType);
    }

    public static void executeBuildGptBound(CommandHandler handler, int x1, int y1, int z1, int x2, int y2, int z2, String building) {
        String prompt = String.format(GPT_PROMPT_BOUND, building, x1, x2, y1, y2, z1, z2);
        executeBuildGpt(handler, prompt);
    }

    public static void executeBuildGptUnbound(CommandHandler handler, int x, int y, int z, String building) {
        String prompt = String.format(GPT_PROMPT_UNBOUND, building, x, y, z);
        executeBuildGpt(handler, prompt);
    }

    private static void executeBuildGpt(CommandHandler handler, String prompt) {
        handler.sendMessage("Requesting Structure from GPT...");

        CompletableFuture
                .supplyAsync(() -> requestGpt(prompt))
                .thenApply(json -> {
                    if (json == null) return null;
                    try {
                        return parseJson(json);
                    } catch (JsonSyntaxException e) {
                        return null;
                    }
                })
                .thenAccept(blocks -> {
                    if (blocks == null) {
                        handler.sendError("Failed to get or parse GPT response.");
                        return;
                    }

                    for (Map<String, Object> block : blocks) {
                        int x = ((Number) block.get("x")).intValue();
                        int y = ((Number) block.get("y")).intValue();
                        int z = ((Number) block.get("z")).intValue();
                        String blockType = (String) block.get("block");

                        handler.executeSetblock(x, y, z, blockType);
                    }

                    handler.sendMessage("Done building!");
                });
    }

    private static String requestGpt(String prompt) {
        try {
            String apiKey = getApiKey();
            if (apiKey == null) {
                LOGGER.error("API key not found. Please provide a valid API key.");
                return null;
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(60))
                    .build();

            JsonObject payload = new JsonObject();
            payload.addProperty("model", "gpt-4o");

            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messages.add(userMessage);

            payload.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                LOGGER.error("Rate limit exceeded or no tokens left.");
                return null;
            }

            if (response.statusCode() != 200) {
                LOGGER.error("Unexpected response code: " + response.statusCode());
                LOGGER.error(response.body());
                return null;
            }

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

            return jsonResponse.get("choices").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("content").getAsString();

        } catch (HttpTimeoutException e) {
            LOGGER.error("Request timed out.", e);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error during GPT request", e);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> parseJson(String jsonResponse) {
        // Use Gson to parse JSON string to List<Map>
        Gson gson = new Gson();
        return (List<Map<String, Object>>) gson.fromJson(jsonResponse, List.class);
    }

    public static String getApiKey() {
        File keyFile = new File(API_KEY_FILE);

        if (keyFile.exists()) {
            // Read the API key from the file
            try {
                String apiKey = new String(Files.readAllBytes(Paths.get(API_KEY_FILE))).trim();
                if (!apiKey.isEmpty()) {
                    return apiKey;
                } else {
                    LOGGER.error("API key file is empty. Please provide a valid API key.");
                }
            } catch (IOException e) {
                LOGGER.error("Error reading API key file", e);
            }
        } else {
            // File doesn't exist, create it and prompt user for the key
            try {
                LOGGER.warn("No API key found. Creating .openaikey file...");

                // Write the API key to the file
                Files.write(Paths.get(API_KEY_FILE), "".getBytes(), StandardOpenOption.CREATE);

                LOGGER.warn("Add your OpenAI API key to " + API_KEY_FILE);
                return null;
            } catch (IOException e) {
                LOGGER.error("Failed to create API key file.");
                LOGGER.error("Create it manually as '.openaikey' in the config directory.");
            }
        }

        return null;
    }

}
