package dev.gimme.gimmeapi.boot.command.property;

import dev.gimme.gimmeapi.boot.command.UtilsKt;
import dev.gimme.gimmeapi.command.channel.TextCommandChannel;
import dev.gimme.gimmeapi.command.sender.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        String arg1 = "abc";
        int arg2 = 123;
        PCmd c = new PCmd(commandName);

        assertFalse(c.called[0]);

        channel.getCommandManager().registerCommand(c);
        channel.parseInput(sender, commandName + " " + arg1 + " " + arg2);

        assertTrue(c.called[0]);
    }
}

class PCmd extends PropertyCommand<Void> {

    final boolean[] called = {false};

    private Param<String> a = this.<String>param()
            .setName("a")
            .setType(String.class)
            .build();

    PCmd(@NotNull String name) {
        super(name);
    }

    @Override
    public Void call() {
        called[0] = true;
        assertEquals("abc", a.getValue());
        return null;
    }
}
