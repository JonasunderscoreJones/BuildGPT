package dev.jonasjones.fabric.versions.v1211;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.jonasjones.common.GPTCommand;
import dev.jonasjones.common.VersionBridge;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public class VersionBridge1211 implements VersionBridge {

    @Override
    public void registerHelloCommand() {
        System.out.println("[1.21+] Registering hello command");
    }

    @Override
    public void registerBuildGptCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("buildgpt")
                    // Bound version: requires start_pos and end_pos
                    .then(literal("bound")
                            .then(net.minecraft.commands.Commands.argument("start_pos", BlockPosArgument.blockPos())
                                    .then(net.minecraft.commands.Commands.argument("end_pos", BlockPosArgument.blockPos())
                                            .then(net.minecraft.commands.Commands.argument("building", StringArgumentType.greedyString())
                                                    .executes(VersionBridge1211::executeBoundCommand)
                                            )
                                    )
                            )
                    )
                    // Unbound version: requires start_pos only
                    .then(literal("unbound")
                            .then(net.minecraft.commands.Commands.argument("start_pos", BlockPosArgument.blockPos())
                                    .then(net.minecraft.commands.Commands.argument("building", StringArgumentType.greedyString())
                                            .executes(VersionBridge1211::executeUnboundCommand)
                                    )
                            )
                    )
            );
        });
    }

    private static int executeBoundCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String building = StringArgumentType.getString(context, "building");
        BlockPos startPos = BlockPosArgument.getBlockPos(context, "start_pos");
        BlockPos endPos = BlockPosArgument.getBlockPos(context, "end_pos");

        GPTCommand.executeBuildGptBound(
                new CommandHandlerImpl(source),
                startPos.getX(), startPos.getY(), startPos.getZ(),
                endPos.getX(), endPos.getY(), endPos.getZ(),
                building
        );

        return 1;
    }

    private static int executeUnboundCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String building = StringArgumentType.getString(context, "building");
        BlockPos startPos = BlockPosArgument.getBlockPos(context, "start_pos");

        GPTCommand.executeBuildGptUnbound(
                new CommandHandlerImpl(source),
                startPos.getX(), startPos.getY(), startPos.getZ(),
                building
        );

        return 1;
    }

    private static class CommandHandlerImpl implements GPTCommand.CommandHandler {
        private final CommandSourceStack source;

        CommandHandlerImpl(CommandSourceStack source) {
            this.source = source;
        }

        @Override
        public void sendMessage(String message) {
            source.sendSystemMessage(Component.literal(message));
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
    }
}