package dev.jonasjones.fabric.versions.v1211;

import dev.jonasjones.common.GPTCommandLogic;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static net.minecraft.commands.Commands.literal;

public class Fabric121Commands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                    literal("hello")
                            .executes(ctx -> {
                                GPTCommandLogic.run();
                                return 1;
                            })
            );
        });
    }
}
