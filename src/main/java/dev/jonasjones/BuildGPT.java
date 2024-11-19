package dev.jonasjones;

import com.google.gson.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.core.jmx.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

public class BuildGPT implements ModInitializer {
	public static final String MOD_ID = "modid";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String API_KEY_FILE = FabricLoader.getInstance().getConfigDir() + "/.openaikey";

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(CommandManager.literal("buildgpt")
					.then(CommandManager.literal("bound")
						.then(CommandManager.argument("start_pos", BlockPosArgumentType.blockPos())
								.then(CommandManager.argument("end_pos", BlockPosArgumentType.blockPos())
										.then(CommandManager.argument("building", StringArgumentType.greedyString())
																			.executes(BuildGPT::executeBuildGptdBound)))))
					.then(CommandManager.literal("unbound")
							.then(CommandManager.argument("start_pos", BlockPosArgumentType.blockPos())
									.then(CommandManager.argument("building", StringArgumentType.greedyString())
										.executes(BuildGPT::executeBuildGptUnboud)))));
		});
	}

	private static int executeBuildGptdBound(CommandContext<ServerCommandSource> context) {
		String building = StringArgumentType.getString(context, "building");
		BlockPos start_pos = BlockPosArgumentType.getBlockPos(context, "start_pos");
		String prompt;
		int x1 = start_pos.getX();
		int y1 = start_pos.getY();
		int z1 = start_pos.getZ();
		BlockPos end_pos = BlockPosArgumentType.getBlockPos(context, "end_pos");
		int x2 = end_pos.getX();
		int y2 = end_pos.getY();
		int z2 = end_pos.getZ();

		prompt = String.format(
				"Imagine, You're an architect. Design the structure of a minecraft %s within the coordinate range x: %d-%d, y: %d-%d, z: %d-%d in minecraft." +
						"Return the blocks in a json list of objects {x:1,y:1,z:1,block:minecraft:block}. Return only the json without any formatting or explanation as plaintext.",
				building, x1, x2, y1, y2, z1, z2
		);

		return executeBuildGpt(context, prompt);
	}

	private static int executeBuildGptUnboud(CommandContext<ServerCommandSource> context) {
		String building = StringArgumentType.getString(context, "building");
		BlockPos start_pos = BlockPosArgumentType.getBlockPos(context, "start_pos");
		int x1 = start_pos.getX();
		int y1 = start_pos.getY();
		int z1 = start_pos.getZ();

		String prompt = String.format(
				"Imagine, You're an architect. Design the structure of a %s at the coordinate x: %d, y: %d, z: %d in minecraft." +
						"Return the blocks in a json list of objects {x:1,y:1,z:1,block:minecraft:block}. Return only the json without any formatting or explanation as plaintext.",
				building, x1, y1, z1
		);

		return executeBuildGpt(context, prompt);
	}

	private static int executeBuildGpt(CommandContext<ServerCommandSource> context, String prompt) {
		ServerCommandSource source = context.getSource();
		source.sendMessage(Text.of("Requesting Structure from GPT..."));
		String jsonResponse = requestGpt(prompt);
		if (jsonResponse == null) {
			source.sendError(Text.of("Failed to get a response from GPT. Rerun the command to try again..."));
			return 0;
		}

		List<Map<String, Object>> blocks;

		try {
			 blocks = parseJson(jsonResponse);
		} catch (JsonSyntaxException ignored) {
			source.sendError(Text.of("Failed to parse the response from GPT. Rerun the command to try again..."));
			return 0;
		}

		for (Map<String, Object> block : blocks) {
			int x = ((Number) block.get("x")).intValue();
			int y = ((Number) block.get("y")).intValue();
			int z = ((Number) block.get("z")).intValue();
			String blockType = (String) block.get("block");

			source.getServer().getCommandManager().executeWithPrefix(
					source, String.format("/setblock %d %d %d %s", x, y, z, blockType)
			);
		}

		source.sendMessage(Text.of("Done building!"));

		return 1;
	}

	private static String requestGpt(String prompt) {
		// HttpClient with 60 second response timeout
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60 * 1000).build();
		CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		try {
			HttpPost request = new HttpPost("https://api.openai.com/v1/chat/completions");
			request.addHeader("Content-Type", "application/json");
			String apiKey = getApiKey();
			if (apiKey == null) {
				LOGGER.error("API key not found. Please provide a valid API key.");
				return null;
			}
			request.addHeader("Authorization", "Bearer " + apiKey);

			JsonObject payload = new JsonObject();
			payload.addProperty("model", "gpt-4o");

			JsonArray messages = new JsonArray();
			JsonObject userMessage = new JsonObject();
			userMessage.addProperty("role", "user");
			userMessage.addProperty("content", prompt);
			messages.add(userMessage);

			payload.add("messages", messages);
			request.setEntity(new StringEntity(payload.toString()));

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent())
				);
				StringBuilder responseContent = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					responseContent.append(line);
				}

				JsonObject jsonResponse = JsonParser.parseString(responseContent.toString()).getAsJsonObject();
				return jsonResponse.get("choices").getAsJsonArray()
						.get(0).getAsJsonObject()
						.get("message").getAsJsonObject()
						.get("content").getAsString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static List<Map<String, Object>> parseJson(String jsonResponse) {
		// Use Gson to parse JSON string to List<Map>
		Gson gson = new Gson();
		return gson.fromJson(jsonResponse, List.class);
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
				e.printStackTrace();
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