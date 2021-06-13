package dev.gimme.gimmeapi.command.property;

import dev.gimme.gimmeapi.command.UtilsKt;
import dev.gimme.gimmeapi.command.parameter.CommandParameter;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDelegatePropertyTest {

    static List<String> listInput = List.of("a", "b");

    @Test
    void converts_delegate_properties_to_parameters_that_take_arguments() {

        var command = new PCmd2();
        Map<CommandParameter, Object> input = Map.of(
                requireNonNull(command.getParameters().get("string")), "a",
                requireNonNull(command.getParameters().get("int")), 1,
                requireNonNull(command.getParameters().get("double")), 0.5,
                requireNonNull(command.getParameters().get("boolean")), true,
                requireNonNull(command.getParameters().get("list")), listInput,
                requireNonNull(command.getParameters().get("collection")), listInput,
                requireNonNull(command.getParameters().get("iterable")), listInput
        );

        assertFalse(command.called[0]);
        command.execute(UtilsKt.getDUMMY_COMMAND_SENDER(), input);
        assertTrue(command.called[0]);
    }
}

class PCmd2 extends PropertyCommand<Void> {

    final boolean[] called = {false};

    private final Param<String> a = param(String.class)
            .name("string")
            .build();

    private final Param<Integer> b = param(Integer.class)
            .name("int")
            .build();

    private final Param<Double> c = param(Double.class)
            .name("double")
            .build();

    private final Param<Boolean> d = param(Boolean.class)
            .name("boolean")
            .build();

    private final Param<List<String>> e = param(String.class)
            .name("list")
            .buildList();

    private final Param<? extends Collection<String>> f = param(String.class)
            .name("collection")
            .buildList();

    private final Param<? extends Iterable<String>> g = param(String.class)
            .name("iterable")
            .buildList();

    PCmd2() {
        super("test-command");
    }

    @Override
    public Void call() {
        called[0] = true;

        assertEquals("a", a.getValue());
        assertEquals(1, b.getValue());
        assertEquals(0.5, c.getValue());
        assertEquals(true, d.getValue());
        assertEquals(JDelegatePropertyTest.listInput, e.getValue());
        assertEquals(JDelegatePropertyTest.listInput, f.getValue());
        assertEquals(JDelegatePropertyTest.listInput, g.getValue());

        return null;
    }
}
