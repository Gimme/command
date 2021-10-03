package dev.gimme.command.function;

import dev.gimme.command.UtilsKt;
import dev.gimme.command.annotations.Default;
import dev.gimme.command.annotations.Parameter;
import dev.gimme.command.parameter.CommandParameter;
import dev.gimme.command.sender.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JFunctionCommandTest {

    static final CommandSender SENDER = UtilsKt.getDUMMY_COMMAND_SENDER();

    @Test
    void test() {
        String arg1 = "abc";
        int arg2 = 123;
        List<Object> args = List.of(arg1, arg2);

        FCmd c = new FCmd("k");

        Map<CommandParameter, Object> input = IntStream.range(0, args.size())
                .boxed()
                .collect(Collectors.toMap(i -> c.getParameters().getAt(i), args::get));

        assertFalse(c.called[0]);
        c.execute(SENDER, input);
        assertTrue(c.called[0]);

        assertNotNull(c.getParameters().get("arg1"));
        assertNotNull(c.getParameters().get("arg2"));
        assertNotNull(c.getParameters().get("c"));
    }
}

class FCmd extends FunctionCommand<Void> {

    final boolean[] called = {false};

    FCmd(@NotNull String name) {
        super(name);
    }

    @CommandFunction
    private void call(
          CommandSender s,
          String a,
          int b,
          @Parameter(value = "c", def = @Default("3")) int c
    ) {
        called[0] = true;

        assertEquals(JFunctionCommandTest.SENDER, s);
        assertEquals("abc", a);
        assertEquals(123, b);
        assertEquals(3, c);
    }
}
