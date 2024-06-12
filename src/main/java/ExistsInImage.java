import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExistsInImage {

	private static final String API_URL = "https://api.openai.com/v1/chat/completions";
	private static final String MODEL = "gpt-4o";
	private final String thingToFind;

	// Constructor that takes the thing to find in the image
	public ExistsInImage(String thingToFind) {
		this.thingToFind = thingToFind;
	}

	// Method to check if the thing exists in the image
	public boolean check(String imagePath) {
		try {
			// Read API key from file
			String apiKey = new String(Files.readAllBytes(Paths.get(System.getProperty("user.home"), "openapi.key")));

			// Load and encode the image
			File imageFile = new File(imagePath);
			String base64Image = encodeImageToBase64(imageFile);

			// Create JSON payload
			String jsonPayload = createJsonPayload(base64Image);

			// Send the image to OpenAI API
			String response = sendImageToOpenAiApi(apiKey, jsonPayload);

			// Parse the response and check if the thing exists in the image
			return parseExistsFromResponse(response);

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Method to encode the image to Base64
	private static String encodeImageToBase64(File imageFile) throws IOException {
		try (FileInputStream imageInFile = new FileInputStream(imageFile)) {
			byte[] imageData = IOUtils.toByteArray(imageInFile);
			return Base64.getEncoder().encodeToString(imageData);
		}
	}

	// Method to create JSON payload for the API request
	private String createJsonPayload(String base64Image) {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode payload = objectMapper.createObjectNode();
		payload.put("model", MODEL);

		ArrayNode messages = objectMapper.createArrayNode();
		ObjectNode message = objectMapper.createObjectNode();
		message.put("role", "user");

		ArrayNode content = objectMapper.createArrayNode();

		ObjectNode textContent = objectMapper.createObjectNode();
		textContent.put("type", "text");
		textContent.put("text", "Is there a " + thingToFind
				+ " in the picture? Answer with a simple JSON formatted response with one attribute that is called exists, with only two answer alternatives true and false. Make sure to only write the JSON format, nothing else so that it can directly be parsed as a response in a program, example: '{\"exists\": true}'.");
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

	// Method to send the image to OpenAI API
	private static String sendImageToOpenAiApi(String apiKey, String jsonPayload) throws IOException {
		OkHttpClient client = new OkHttpClient();

		RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));

		Request request = new Request.Builder().url(API_URL).header("Authorization", "Bearer " + apiKey).post(body)
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful())
				throw new IOException("Unexpected code " + response);
			return response.body().string();
		}
	}

	// Method to parse the response from the API to check if the thing exists in the
	// image
	private static boolean parseExistsFromResponse(String response) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(response);

		// Navigate to the correct subentry in the response JSON structure
		JsonNode choicesNode = jsonNode.path("choices");
		if (choicesNode.isArray() && choicesNode.size() > 0) {
			JsonNode messageNode = choicesNode.get(0).path("message");
			if (messageNode.has("content")) {
				JsonNode contentNode = objectMapper.readTree(messageNode.get("content").asText());
				return contentNode.path("exists").asBoolean(false);
			}
		}
		return false;
	}

	// Main method to run the example
	public static void main(String[] args) {
		ExistsInImage existsInImage = new ExistsInImage("bike");
		boolean result = existsInImage.check(System.getProperty("user.home") + "/surprise.jpg");
		System.out.println("Does the image contain a bike? " + (result ? "yes." : "no."));
	}
}
