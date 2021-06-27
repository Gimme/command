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
            0.0,
            0.5,
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
        assertNotNull(command.getParameters().get("b"));
        assertNotNull(command.getParameters().get("list"));
        assertNotNull(command.getParameters().get("set"));
    }
}

class PCmd extends PropertyCommand<Void> {

    final boolean[] called = {false};

    private final Param<String> a = param(String.class)
            .build();

    private final Param<Integer> b = param(Integer.class)
            .build();

    private final Param<List<Double>> list = param(Double.class)
            .list();

    private final Param<Set<String>> set = param(String.class)
            .set();

    PCmd(@NotNull String name) {
        super(name);
    }

    @Override
    public Void call() {
        called[0] = true;

        var args = JPropertyCommandTest.args;

        assertEquals(args.get(0), a.get());
        assertEquals(args.get(1), b.get());
        assertIterableEquals(List.of(args.get(2), args.get(3)), list.get());
        assertIterableEquals(Set.of(args.get(4)), set.get());

        return null;
    }
}
