package dev.gimme.command.property;

import dev.gimme.command.UtilsKt;
import dev.gimme.command.parameter.CommandParameter;
import dev.gimme.command.sender.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JPropertyCommandTest {

    static List<Object> args = List.of(
            "abc",
            123,
            List.of(0.0, 0.5),
            Set.of("x")
    );

    @Test
    void test() {
        CommandSender sender = UtilsKt.getDUMMY_COMMAND_SENDER();
        PCmd command = new PCmd("k");

        Map<CommandParameter, Object> input = IntStream.range(0, args.size())
                .boxed()
                .collect(Collectors.toMap(i -> command.getParameters().getAt(i), args::get));

        assertFalse(command.called[0]);
        command.execute(sender, input);
        assertTrue(command.called[0]);

        assertNotNull(command.getParameters().get("a"));
        assertNotNull(command.getParameters().get("b"));
        assertNotNull(command.getParameters().get("list"));
        assertNotNull(command.getParameters().get("set"));
    }
}

class PCmd extends PropertyCommand<Void> {

    final boolean[] called = {false};

    private final Param<String> a = param();

    private final Param<Integer> b = param();

    private final Param<List<Double>> list = param();

    private final Param<Set<String>> set = param();

    PCmd(@NotNull String name) {
        super(name);
    }

    @Override
    public Void call() {
        called[0] = true;

        var args = JPropertyCommandTest.args;

        assertEquals(args.get(0), a.get());
        assertEquals(args.get(1), b.get());
        assertIterableEquals((List<Double>) args.get(2), list.get());
        assertIterableEquals((Set<String>) args.get(3), set.get());

        return null;
    }
}
