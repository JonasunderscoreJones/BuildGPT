package dev.jonasjones.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.jonasjones.common.GPTCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;

public class BuildGPTCommand {

    /*public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    register(dispatcher);
                }
        );
    }*/

    static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("buildgpt")
                    .then(Commands.literal("bound")
                            .then(Commands.argument("start_pos", BlockPosArgument.blockPos())
                                    .then(Commands.argument("end_pos", BlockPosArgument.blockPos())
                                            .then(Commands.argument("building", StringArgumentType.greedyString())
                                                    .executes(ctx -> {
                                                        int x1 = BlockPosArgument.getBlockPos(ctx, "start_pos").getX();
                                                        int y1 = BlockPosArgument.getBlockPos(ctx, "start_pos").getY();
                                                        int z1 = BlockPosArgument.getBlockPos(ctx, "start_pos").getZ();
                                                        int x2 = BlockPosArgument.getBlockPos(ctx, "end_pos").getX();
                                                        int y2 = BlockPosArgument.getBlockPos(ctx, "end_pos").getY();
                                                        int z2 = BlockPosArgument.getBlockPos(ctx, "end_pos").getZ();
                                                        String building = StringArgumentType.getString(ctx, "building");

                                                        // Create a handler for this command source
                                                        GPTCommand.CommandHandler handler = new GPTCommand.CommandHandler() {
                                                            private final CommandSourceStack source = ctx.getSource();

                                                            @Override
                                                            public void sendMessage(String message) {
                                                                source.sendSuccess(() -> Component.literal(message), false);
                                                            }

                                                            @Override
                                                            public void sendError(String message) {
                                                                source.sendFailure(Component.literal(message));
                                                            }

                                                            @Override
                                                            public void executeSetblock(int x, int y, int z, String blockType) {
                                                                source.getServer().getCommands().performPrefixedCommand(
                                                                        source,
                                                                        String.format("/setblock %d %d %d %s", x, y, z, blockType)
                                                                );
                                                            }
                                                        };

                                                        // call your common logic here
                                                        GPTCommand.executeBuildGptBound(handler, x1, y1, z1, x2, y2, z2, building);

                                                        ctx.getSource().sendSuccess(
                                                                () -> Component.literal("BuildGPT works!"),
                                                                false
                                                        );

                                                        return 1;
                                                    })))))
                    .then(Commands.literal("unbound")
                            .then(Commands.argument("start_pos", BlockPosArgument.blockPos())
                                    .then(Commands.argument("building", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                int x1 = BlockPosArgument.getBlockPos(ctx, "start_pos").getX();
                                                int y1 = BlockPosArgument.getBlockPos(ctx, "start_pos").getY();
                                                int z1 = BlockPosArgument.getBlockPos(ctx, "start_pos").getZ();
                                                String building = StringArgumentType.getString(ctx, "building");

                                                // Create a handler for this command source
                                                GPTCommand.CommandHandler handler = new GPTCommand.CommandHandler() {
                                                    private final CommandSourceStack source = ctx.getSource();

                                                    @Override
                                                    public void sendMessage(String message) {
                                                        source.sendSuccess(() -> Component.literal(message), false);
                                                    }

                                                    @Override
                                                    public void sendError(String message) {
                                                        source.sendFailure(Component.literal(message));
                                                    }

                                                    @Override
                                                    public void executeSetblock(int x, int y, int z, String blockType) {
                                                        source.getServer().getCommands().performPrefixedCommand(
                                                                source,
                                                                String.format("/setblock %d %d %d %s", x, y, z, blockType)
                                                        );
                                                    }
                                                };

                                                // call your common logic here
                                                GPTCommand.executeBuildGptUnbound(handler, x1, y1, z1, building);

                                                ctx.getSource().sendSuccess(
                                                        () -> Component.literal("BuildGPT works!"),
                                                        false
                                                );

                                                return 1;
                                            })))));
        });
    }
}