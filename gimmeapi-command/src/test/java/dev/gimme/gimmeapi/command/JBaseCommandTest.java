package dev.gimme.gimmeapi.command;

import dev.gimme.gimmeapi.command.annotations.Parameter;
import dev.gimme.gimmeapi.command.annotations.Sender;
import dev.gimme.gimmeapi.command.parameter.CommandParameter;
import dev.gimme.gimmeapi.command.sender.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JBaseCommandTest {

    static boolean called;
    static CommandSender dummySender = UtilsKt.getDUMMY_COMMAND_SENDER();

    @Test
    void senderAnnotation() {
        testCommand(new JTestCommand1());
    }

    @Test
    void handles_various_parameter_types() {
        List<String> listArg = List.of("a", "b", "a");
        List<Object> halfArgs = List.of(
                "a",
                1,
                0.5,
                true,
                listArg,
                new HashSet<>(listArg),
                listArg,
                listArg
        );
        List<Object> args = Stream.concat(halfArgs.stream(), halfArgs.stream()).collect(Collectors.toList());

        BaseCommand<Void> command = new JTestCommand2(args);

        testCommand(
                command,
                args,
                dummySender
        );
    }

    private void testCommand(
            BaseCommand<?> command,
            Map<CommandParameter, ?> args,
            CommandSender sender
    ) {
        called = false;
        command.execute(sender, args);
        assertTrue(called);
    }

    private void testCommand(BaseCommand<?> command) {
        testCommand(command, Map.of(), dummySender);
    }

    private void testCommand(
            BaseCommand<?> command,
            List<Object> args,
            CommandSender sender
    ) {
        AtomicInteger i = new AtomicInteger(0);
        testCommand(
                command,
                command.getParameters().stream()
                        .collect(Collectors.toMap(Function.identity(), x -> args.get(i.getAndIncrement()))),
                sender
        );
    }
}

class JTestCommand1 extends BaseCommand<Void> {

    @Sender
    private CommandSender sender;

    @Override
    protected Void call() {
        assertEquals(JBaseCommandTest.dummySender, sender);

        JBaseCommandTest.called = true;
        return null;
    }

    JTestCommand1() {
        super("test-command");
    }
}

class JTestCommand2 extends BaseCommand<Void> {

    private final Param<String> string1 = param(String.class);
    private final Param<Integer> int1 = param(Integer.class);
    private final Param<Double> double1 = param(Double.class);
    private final Param<Boolean> boolean1 = param(Boolean.class);
    private final Param<List<String>> list1 = param(String.class).list();
    private final Param<Set<String>> set1 = param(String.class).set();
    private final Param<? extends Collection<String>> collection1 = param(String.class).list();
    private final Param<? extends Iterable<String>> iterable1 = param(String.class).list();

    @Parameter
    private String string2;

    @Parameter
    private Integer int2;

    @Parameter
    private Double double2;

    @Parameter
    private Boolean boolean2;

    @Parameter
    private List<String> list2;

    @Parameter
    private Set<String> set2;

    @Parameter
    private Collection<String> collection2;

    @Parameter
    private Iterable<String> iterable2;

    @Override
    protected Void call() {
        Iterator<Object> iter = args.iterator();

        assertEquals(iter.next(), string1.get());
        assertEquals(iter.next(), int1.get());
        assertEquals(iter.next(), double1.get());
        assertEquals(iter.next(), boolean1.get());
        assertEquals(iter.next(), list1.get());
        assertEquals(iter.next(), set1.get());
        assertEquals(iter.next(), collection1.get());
        assertEquals(iter.next(), iterable1.get());

        assertEquals(iter.next(), string2);
        assertEquals(iter.next(), int2);
        assertEquals(iter.next(), double2);
        assertEquals(iter.next(), boolean2);
        assertEquals(iter.next(), list2);
        assertEquals(iter.next(), set2);
        assertEquals(iter.next(), collection2);
        assertEquals(iter.next(), iterable2);

        JBaseCommandTest.called = true;
        return null;
    }

    private final List<Object> args;

    JTestCommand2(List<Object> args) {
        super("test-command");

        this.args = args;
    }
}
