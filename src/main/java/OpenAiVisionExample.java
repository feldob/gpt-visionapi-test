import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAiVisionExample {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions"; // The endpoint for GPT-4 model
    private static final String MODEL = "gpt-4o"; // Use the appropriate model ID

    public static void main(String[] args) {
        try {
            // Read API key from file
            String apiKey = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home"), "openapi.key")));

            // Load and encode the image
            File imageFile = new File(System.getProperty("user.home"), "surprise.jpg");
            String base64Image = encodeImageToBase64(imageFile);

            // Create JSON payload
            String jsonPayload = createJsonPayload(base64Image);

            // Send the image to OpenAI API
            String response = sendImageToOpenAiApi(apiKey, jsonPayload);
            System.out.println("Response: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String encodeImageToBase64(File imageFile) throws IOException {
        try (FileInputStream imageInFile = new FileInputStream(imageFile)) {
            byte[] imageData = IOUtils.toByteArray(imageInFile);
            return Base64.getEncoder().encodeToString(imageData);
        }
    }

    private static String createJsonPayload(String base64Image) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", MODEL);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");

        ArrayNode content = objectMapper.createArrayNode();

        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");
        textContent.put("text", "Is there a bike in the picture? Answer with a simple json formatted response with one attribute that is called exists, with only two answer alternatives true and false. Make sure to only write the json format, nothing else so that it can directly be parsed as a response in a program, example: '{\"exists\": true}'.");
        content.add(textContent);

        ObjectNode imageContent = objectMapper.createObjectNode();
        imageContent.put("type", "image_url");
        ObjectNode imageUrl = objectMapper.createObjectNode();
        imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
        imageContent.set("image_url", imageUrl);
        content.add(imageContent);

        message.set("content", content);
        messages.add(message);
        payload.set("messages", messages);
        payload.put("max_tokens", 300);

        return payload.toString();
    }

    private static String sendImageToOpenAiApi(String apiKey, String jsonPayload) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }
}
