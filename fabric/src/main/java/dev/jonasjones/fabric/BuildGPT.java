package dev.jonasjones.fabric;

import dev.jonasjones.common.GPTCommand;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class BuildGPT implements ModInitializer {
	public static final String MOD_ID = "buildgpt";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String API_KEY_FILE = FabricLoader.getInstance().getConfigDir() + "/.openaikey";

	@Override
	public void onInitialize() {
		System.out.println("BuildGPT Fabric loaded!");

		Path configDir = FabricLoader.getInstance().getConfigDir();
		GPTCommand.setApiKeyFile(configDir.resolve(".openaikey").toString());

		BuildGPTCommand.register();
	}

}