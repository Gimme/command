package dev.gimme.gimmeapi.command.property;

import dev.gimme.gimmeapi.command.SenderTypes;
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
                requireNonNull(command.getParameters().get("i")), 1,
                requireNonNull(command.getParameters().get("d")), 0.5,
                requireNonNull(command.getParameters().get("b")), true,
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

        var command = new MultipleSenderTestCommand(commandSender);

        assertFalse(command.called[0]);
        command.execute(commandSender, Map.of());
        assertTrue(command.called[0]);
    }

    @Test
    void handles_custom_adapted_senders() {
        SenderTypes.INSTANCE.registerAdapter(Player.class, PlayerSender.class, (PlayerSender sender) -> sender.player);

        var commandSender = new PlayerSender();

        var command = new CustomSenderTestCommand(commandSender.player);

        assertFalse(command.called[0]);
        command.execute(commandSender, Map.of());
        assertTrue(command.called[0]);
    }
}

class MultipleSenderTestCommand extends PropertyCommand<Void> {

    final boolean[] called = {false};
    private final CommandSender expectedSender;

    private final Sender<CommandSender> senderSuper = sender(CommandSender.class);
    private final Sender<Sender1> senderSub1 = sender(Sender1.class, true);
    private final Sender<Sender2> senderSub2 = sender(Sender2.class, true);

    private final Param<String> string = param(String.class)
            .build();

    private final Param<Integer> i = param(Integer.class)
            .build();

    MultipleSenderTestCommand(CommandSender expectedSender) {
        super("test-command");

        this.expectedSender = expectedSender;
    }

    @Override
    public Void call() {
        called[0] = true;

        assertEquals(expectedSender, senderSuper.get());
        assertEquals(expectedSender, senderSub1.get());
        assertEquals("sender1", senderSub1.get().getName());
        assertEquals(senderSub1.get().getName(), senderSuper.get().getName());
        assertNull(senderSub2.get());

        return null;
    }
}

class CustomSenderTestCommand extends PropertyCommand<Void> {

    final boolean[] called = {false};
    private final Player expectedPlayer;

    private final Sender<Player> sender = sender(Player.class);

    CustomSenderTestCommand(Player expectedPlayer) {
        super("test-command");

        this.expectedPlayer = expectedPlayer;
    }

    @Override
    public Void call() {
        called[0] = true;

        assertEquals(expectedPlayer, sender.get());

        return null;
    }
}

class DelegateTestCommand extends PropertyCommand<Void> {

    final boolean[] called = {false};

    private final Param<String> string = param(String.class)
            .build();

    private final Param<Integer> i = param(Integer.class)
            .build();

    private final Param<Double> d = param(Double.class)
            .build();

    private final Param<Boolean> b = param(Boolean.class)
            .build();

    private final Param<List<String>> list = param(String.class)
            .buildList();

    private final Param<Set<String>> set = param(String.class)
            .buildSet();

    private final Param<? extends Collection<String>> collection = param(String.class)
            .buildList();

    private final Param<? extends Iterable<String>> iterable = param(String.class)
            .buildList();

    DelegateTestCommand() {
        super("test-command");
    }

    @Override
    public Void call() {
        called[0] = true;

        var listInput = JDelegatePropertyTest.listInput;
        var setInput = JDelegatePropertyTest.setInput;

        assertEquals("a", string.get());
        assertEquals(1, i.get());
        assertEquals(0.5, d.get());
        assertEquals(true, b.get());
        assertIterableEquals(listInput, list.get());
        assertIterableEquals(setInput, set.get());
        assertIterableEquals(listInput, collection.get());
        assertIterableEquals(listInput, iterable.get());

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

class PlayerSender implements CommandSender {

    Player player = new Player();

    @NotNull
    @Override
    public String getName() {
        return "sender2";
    }

    @Override
    public void sendMessage(@NotNull String message) {
    }
}

class Player {}
