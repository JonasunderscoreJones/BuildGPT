package dev.jonasjones.fabric;

import dev.jonasjones.common.GPTCommand;
import dev.jonasjones.fabric.versions.v1200to12110.Fabric1200_TO_12110Commands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public class BuildGPT implements ModInitializer {
	public static final String MOD_ID = "buildgpt";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String API_KEY_FILE = FabricLoader.getInstance().getConfigDir() + "/.openaikey";

	// version ranges arrays
	private static final List<String> VERSIONS_119_TO_120 = List.of(
			"1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4"
	);

	private static final List<String> VERSIONS_1200_TO_12110 = List.of(
			"1.20", "1.20.1", "1.20.2",
			"1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9",  "1.21.10"
	);

	private static final List<String> VERSIONS_12111_PLUS = List.of(
			"1.21.11", "26.1", "26.1.1", "26.1.2"
	);

	@Override
	public void onInitialize() {

		Path configDir = FabricLoader.getInstance().getConfigDir();
		GPTCommand.setApiKeyFile(configDir.resolve(".openaikey").toString());

		String mcVersion;

		try {
			Object versionObj = SharedConstants.getCurrentVersion();
			// Try getName()
			try {
				mcVersion = (String) versionObj.getClass().getMethod("getName").invoke(versionObj);
			} catch (NoSuchMethodException e) {
				// fallback to name() for newer versions
				mcVersion = (String) versionObj.getClass().getMethod("name").invoke(versionObj);
			}
		} catch (Exception e) {
			e.printStackTrace();
			mcVersion = "unknown";
		}

		if (VERSIONS_1200_TO_12110.contains(mcVersion)) {
			Fabric1200_TO_12110Commands.registerCommands();
		} /*else if (mcVersion.startsWith("1.21")) {
			Fabric1201Commands.registerCommands();
		}*/ else {
			throw new RuntimeException("Unsupported Minecraft version: " + mcVersion);
		}
	}

}