package dev.gimme.gimmeapi.command.property;

import dev.gimme.gimmeapi.command.UtilsKt;
import dev.gimme.gimmeapi.command.parameter.CommandParameter;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
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
