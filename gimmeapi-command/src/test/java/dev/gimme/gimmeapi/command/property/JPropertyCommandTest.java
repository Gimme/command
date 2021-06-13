package dev.gimme.gimmeapi.command.property;

import dev.gimme.gimmeapi.command.UtilsKt;
import dev.gimme.gimmeapi.command.channel.TextCommandChannel;
import dev.gimme.gimmeapi.command.sender.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JPropertyCommandTest {

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
        PCmd command = new PCmd(commandName);

        String arg1 = "abc";
        int arg2 = 123;
        String arg3 = "x";

        String input = commandName + " " + arg1 + " " + arg2 + " " + arg3;

        assertFalse(command.called[0]);

        channel.getCommandManager().registerCommand(command);
        channel.parseInput(sender, input);

        assertTrue(command.called[0]);

        assertNotNull(command.getParameters().get("a"));
        assertNotNull(command.getParameters().get("bb"));
        assertNotNull(command.getParameters().get("c"));
    }
}

class PCmd extends PropertyCommand<Void> {

    final boolean[] called = {false};

    private final Param<String> a = param(String.class)
            .name("a")
            .build();

    private final Param<Integer> b = param(Integer.class)
            .name("bb")
            .build();

    private final Param<List<String>> c = param(String.class)
            .name("c")
            .buildList();

    PCmd(@NotNull String name) {
        super(name);
    }

    @Override
    public Void call() {
        called[0] = true;
        assertEquals("abc", a.getValue());
        assertEquals(123, b.getValue());
        assertIterableEquals(List.of("x"), c.getValue());
        return null;
    }
}
