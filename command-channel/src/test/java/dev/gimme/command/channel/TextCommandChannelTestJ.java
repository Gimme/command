package dev.gimme.command.channel;

import dev.gimme.command.BaseCommand;
import dev.gimme.command.annotations.Name;
import dev.gimme.command.annotations.CommandFunction;
import dev.gimme.command.sender.CommandSender;
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

class TextCommandChannelTestJ {

    private final TextCommandChannel channel = new TextCommandChannel() {
        @Override
        public void onDisable() {
        }

        @Override
        public void onEnable() {
        }
    };

    static final CommandSender SENDER = UtilsKt.getDUMMY_COMMAND_SENDER();

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
        TestCommandJ command = new TestCommandJ(commandName);

        String input = commandName + " " + args.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));

        assertFalse(command.called[0]);

        channel.getCommandManager().registerCommand(command);
        channel.parseInput(SENDER, input);

        assertTrue(command.called[0]);

        assertNotNull(command.getParameters().get("a"));
        assertNotNull(command.getParameters().get("b"));
        assertNotNull(command.getParameters().get("list"));
        assertNotNull(command.getParameters().get("set"));
    }
}

class TestCommandJ extends BaseCommand<Void> {

    final boolean[] called = {false};

    TestCommandJ(@NotNull String name) {
        super(name);
    }

    @CommandFunction
    private void call(
            CommandSender s,
            @Name("a") String a,
            @Name("b") int b,
            @Name("list") List<Double> list,
            @Name("set") Set<String> set
    ) {
        called[0] = true;

        var args = TextCommandChannelTestJ.args;

        assertEquals(TextCommandChannelTestJ.SENDER, s);
        assertEquals(args.get(0), a);
        assertEquals(args.get(1), b);
        assertIterableEquals(List.of(args.get(2), args.get(3)), list);
        assertIterableEquals(Set.of(args.get(4)), set);
    }
}
