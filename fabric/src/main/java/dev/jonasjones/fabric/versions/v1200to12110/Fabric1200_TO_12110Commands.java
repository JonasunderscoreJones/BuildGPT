package dev.jonasjones.fabric.versions.v1200to12110;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.jonasjones.common.GPTCommand;
import dev.jonasjones.common.VersionBridge;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public class Fabric1200_TO_12110Commands implements VersionBridge {

    // Static registration method
    public static void registerCommands() {
        new Fabric1200_TO_12110Commands().registerBuildGptCommand();
    }

    public void registerBuildGptCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("buildgpt")
                    .then(literal("bound")
                            .then(Commands.argument("start_pos", BlockPosArgument.blockPos())
                                    .then(Commands.argument("end_pos", BlockPosArgument.blockPos())
                                            .then(Commands.argument("building", StringArgumentType.greedyString())
                                                    .executes(this::executeBoundCommand)))))
                    .then(literal("unbound")
                            .then(Commands.argument("start_pos", BlockPosArgument.blockPos())
                                    .then(Commands.argument("building", StringArgumentType.greedyString())
                                            .executes(this::executeUnboundCommand))))
            );
        });
    }

    private int executeBoundCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        BlockPos startPos = BlockPosArgument.getBlockPos(context, "start_pos");
        BlockPos endPos = BlockPosArgument.getBlockPos(context, "end_pos");
        String building = StringArgumentType.getString(context, "building");

        GPTCommand.executeBuildGptBound(
                new CommandHandlerImpl(source),
                startPos.getX(), startPos.getY(), startPos.getZ(),
                endPos.getX(), endPos.getY(), endPos.getZ(),
                building
        );
        return 1;
    }

    private int executeUnboundCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        BlockPos startPos = BlockPosArgument.getBlockPos(context, "start_pos");
        String building = StringArgumentType.getString(context, "building");

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