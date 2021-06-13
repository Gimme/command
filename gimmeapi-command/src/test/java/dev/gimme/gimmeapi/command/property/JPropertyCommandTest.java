package dev.gimme.gimmeapi.command.property;

import dev.gimme.gimmeapi.command.UtilsKt;
import dev.gimme.gimmeapi.command.channel.TextCommandChannel;
import dev.gimme.gimmeapi.command.sender.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    static List<Object> args = List.of(
            "abc",
            123,
            "a",
            "x"
    );

    @Test
    void test() {
        String commandName = "k";
        CommandSender sender = UtilsKt.getDUMMY_COMMAND_SENDER();
        PCmd command = new PCmd(commandName);

        String input = commandName + " " + args.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));

        assertFalse(command.called[0]);

        channel.getCommandManager().registerCommand(command);
        channel.parseInput(sender, input);

        assertTrue(command.called[0]);

        assertNotNull(command.getParameters().get("a"));
        assertNotNull(command.getParameters().get("bb"));
        assertNotNull(command.getParameters().get("list"));
        assertNotNull(command.getParameters().get("set"));
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

    private final Param<List<String>> list = param(String.class)
            .name("list")
            .buildList();

    private final Param<Set<String>> set = param(String.class)
            .name("set")
            .buildSet();

    PCmd(@NotNull String name) {
        super(name);
    }

    @Override
    public Void call() {
        called[0] = true;

        var args = JPropertyCommandTest.args;

        assertEquals(args.get(0), a.getArg());
        assertEquals(args.get(1), b.getArg());
        assertIterableEquals(List.of(args.get(2)), list.getArg());
        assertIterableEquals(Set.of(args.get(3)), set.getArg());

        return null;
    }
}
