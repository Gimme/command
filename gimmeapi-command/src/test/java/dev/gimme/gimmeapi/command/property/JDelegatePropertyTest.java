package dev.gimme.gimmeapi.command.property;

import dev.gimme.gimmeapi.command.UtilsKt;
import dev.gimme.gimmeapi.command.parameter.CommandParameter;
import dev.gimme.gimmeapi.command.sender.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDelegatePropertyTest {

    static List<String> listInput = List.of("a", "b", "a");
    static Set<String> setInput = Set.of("a", "b");

    @Test
    void converts_delegate_properties_to_parameters_that_take_arguments() {

        var command = new DelegateTestCommand();
        Map<CommandParameter, Object> input = Map.of(
                requireNonNull(command.getParameters().get("string")), "a",
                requireNonNull(command.getParameters().get("int")), 1,
                requireNonNull(command.getParameters().get("double")), 0.5,
                requireNonNull(command.getParameters().get("boolean")), true,
                requireNonNull(command.getParameters().get("list")), listInput,
                requireNonNull(command.getParameters().get("set")), setInput,
                requireNonNull(command.getParameters().get("collection")), listInput,
                requireNonNull(command.getParameters().get("iterable")), listInput
        );

        assertFalse(command.called[0]);
        command.execute(UtilsKt.getDUMMY_COMMAND_SENDER(), input);
        assertTrue(command.called[0]);
    }

    @Test
    void converts_delegate_properties_to_command_senders() {


        var commandSender = new Sender1();

        final boolean[] called = {false};

        var command = new PropertyCommand<Void>("test-command") {

            private final Sender<CommandSender> senderSuper = sender(CommandSender.class);
            private final Sender<Sender1> senderSub1 = sender(Sender1.class, false);
            private final Sender<Sender2> senderSub2 = sender(Sender2.class, false);

            private final Param<String> string = param(String.class)
                    .name("string")
                    .build();

            private final Param<Integer> i = param(Integer.class)
                    .name("int")
                    .build();

            @Override
            public Void call() {
                called[0] = true;

                assertEquals(commandSender, senderSuper.getValue());
                assertEquals(commandSender, senderSub1.getValue());
                assertEquals("sender1", senderSub1.getValue().getName());
                assertEquals(senderSub1.getValue().getName(), senderSuper.getValue().getName());
                assertNull(senderSub2.getValue());

                return null;
            }
        };

        assertFalse(called[0]);
        command.execute(commandSender, Map.of());
        assertTrue(called[0]);
    }
}

class DelegateTestCommand extends PropertyCommand<Void> {

    final boolean[] called = {false};

    private final Param<String> string = param(String.class)
            .name("string")
            .build();

    private final Param<Integer> i = param(Integer.class)
            .name("int")
            .build();

    private final Param<Double> d = param(Double.class)
            .name("double")
            .build();

    private final Param<Boolean> b = param(Boolean.class)
            .name("boolean")
            .build();

    private final Param<List<String>> list = param(String.class)
            .name("list")
            .buildList();

    private final Param<Set<String>> set = param(String.class)
            .name("set")
            .buildSet();

    private final Param<? extends Collection<String>> collection = param(String.class)
            .name("collection")
            .buildList();

    private final Param<? extends Iterable<String>> iterable = param(String.class)
            .name("iterable")
            .buildList();

    DelegateTestCommand() {
        super("test-command");
    }

    @Override
    public Void call() {
        called[0] = true;

        var listInput = JDelegatePropertyTest.listInput;
        var setInput = JDelegatePropertyTest.setInput;

        assertEquals("a", string.getArg());
        assertEquals(1, i.getArg());
        assertEquals(0.5, d.getArg());
        assertEquals(true, b.getArg());
        assertIterableEquals(listInput, list.getArg());
        assertIterableEquals(setInput, set.getArg());
        assertIterableEquals(listInput, collection.getArg());
        assertIterableEquals(listInput, iterable.getArg());

        return null;
    }
}

class Sender1 implements CommandSender {
    @NotNull
    @Override
    public String getName() {
        return "sender1";
    }

    @Override
    public void sendMessage(@NotNull String message) {
    }
}

class Sender2 implements CommandSender {
    @NotNull
    @Override
    public String getName() {
        return "sender2";
    }

    @Override
    public void sendMessage(@NotNull String message) {
    }
}
