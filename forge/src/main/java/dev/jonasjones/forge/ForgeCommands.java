package dev.jonasjones.forge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.jonasjones.common.GPTCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber(modid = "buildgpt", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("buildgpt")
                .then(literal("bound")
                        .then(Commands.argument("start_pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("end_pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("building", StringArgumentType.greedyString())
                                                .executes(ForgeCommands::executeBoundCommand)))))
                .then(literal("unbound")
                        .then(Commands.argument("start_pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("building", StringArgumentType.greedyString())
                                        .executes(ForgeCommands::executeUnboundCommand)))));
    }

    private static int executeBoundCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String building = StringArgumentType.getString(context, "building");
        BlockPos start_pos = BlockPosArgument.getBlockPos(context, "start_pos");
        BlockPos end_pos = BlockPosArgument.getBlockPos(context, "end_pos");

        GPTCommand.executeBuildGptBound(
                new CommandHandlerImpl(source),
                start_pos.getX(), start_pos.getY(), start_pos.getZ(),
                end_pos.getX(), end_pos.getY(), end_pos.getZ(),
                building
        );
        return 1;
    }

    private static int executeUnboundCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String building = StringArgumentType.getString(context, "building");
        BlockPos start_pos = BlockPosArgument.getBlockPos(context, "start_pos");

        GPTCommand.executeBuildGptUnbound(
                new CommandHandlerImpl(source),
                start_pos.getX(), start_pos.getY(), start_pos.getZ(),
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
