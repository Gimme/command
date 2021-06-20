package dev.gimme.gimmeapi.command.function;

import dev.gimme.gimmeapi.command.UtilsKt;
import dev.gimme.gimmeapi.command.annotations.Parameter;
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

    static final CommandSender SENDER = UtilsKt.getDUMMY_COMMAND_SENDER();

    @Test
    void test() {
        String commandName = "k";
        String arg1 = "abc";
        int arg2 = 123;
        FCmd c = new FCmd(commandName);

        assertFalse(c.called[0]);

        channel.getCommandManager().registerCommand(c);
        channel.parseInput(SENDER, commandName
                + " " + arg1
                + " " + arg2
        );

        assertTrue(c.called[0]);
    }
}

class FCmd extends FunctionCommand<Void> {

    final boolean[] called = {false};

    FCmd(@NotNull String name) {
        super(name);
    }

    @CommandFunction
    private void call(CommandSender s, String a, int b, @Parameter(defaultValue = "3") int c) {
        called[0] = true;

        assertEquals(JFunctionCommandTest.SENDER, s);
        assertEquals("abc", a);
        assertEquals(123, b);
        assertEquals(3, c);
    }
}
