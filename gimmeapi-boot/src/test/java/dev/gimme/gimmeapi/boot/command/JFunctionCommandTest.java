package dev.gimme.gimmeapi.boot.command;

import dev.gimme.gimmeapi.boot.command.executor.CommandExecutor;
import dev.gimme.gimmeapi.command.channel.TextCommandChannel;
import dev.gimme.gimmeapi.command.sender.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JFunctionCommandTest {

    private final TextCommandChannel channel = new TextCommandChannel() {
        @Override
        public void onDisable() {
        }

        @Override
        public void onEnable() {
        }
    };

    @Test
    void test() {
        String commandName = "k";
        CommandSender sender = UtilsKt.getDUMMY_COMMAND_SENDER();
        String arg1 = "abc";
        int arg2 = 123;
        FCmd c = new FCmd(commandName);

        assertFalse(c.called[0]);

        channel.getCommandManager().registerCommand(c);
        channel.parseInput(sender, commandName + " " + arg1 + " " + arg2);

        assertTrue(c.called[0]);
    }
}

class FCmd extends FunctionCommand<Void> {

    final boolean[] called = {false};

    FCmd(@NotNull String name) {
        super(name);
    }

    @CommandExecutor
    private void call(CommandSender sender, String a, int b) {
        called[0] = true;
        assertEquals(sender, sender);
        assertEquals("abc", a);
        assertEquals(123, b);
    }
}
