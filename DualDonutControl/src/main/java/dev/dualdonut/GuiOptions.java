package dev.dualdonut;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_2561;

public final class GuiOptions {
    private GuiOptions() {}

    public static class_2561 text(String value) {
        return class_2561.method_43470(value);
    }

    public static Option<Boolean> bool(String name, String description, boolean fallback, Supplier<Boolean> get, Consumer<Boolean> set) {
        var builder = Option.<Boolean>createBuilder().name(text(name)).binding(fallback, get, set).controller(TickBoxControllerBuilder::create);
        if (description != null && !description.isEmpty()) {
            builder.description(OptionDescription.of(text(description)));
        }
        return builder.build();
    }

    public static Option<Integer> integer(String name, String description, int fallback, int min, int max, Supplier<Integer> get, Consumer<Integer> set) {
        var builder = Option.<Integer>createBuilder().name(text(name)).binding(fallback, get, set)
            .controller(option -> IntegerFieldControllerBuilder.create(option).min(min).max(max));
        if (description != null && !description.isEmpty()) {
            builder.description(OptionDescription.of(text(description)));
        }
        return builder.build();
    }

    public static Option<Integer> integer(String name, int fallback, int min, int max, Supplier<Integer> get, Consumer<Integer> set) {
        return integer(name, "", fallback, min, max, get, set);
    }

    public static Option<Double> decimal(String name, String description, double fallback, double min, double max, Supplier<Double> get, Consumer<Double> set) {
        var builder = Option.<Double>createBuilder().name(text(name)).binding(fallback, get, set)
            .controller(option -> DoubleFieldControllerBuilder.create(option).min(min).max(max));
        if (description != null && !description.isEmpty()) {
            builder.description(OptionDescription.of(text(description)));
        }
        return builder.build();
    }

    public static Option<Double> decimal(String name, double fallback, double min, double max, Supplier<Double> get, Consumer<Double> set) {
        return decimal(name, "", fallback, min, max, get, set);
    }

    public static Option<String> string(String name, String description, String fallback, Supplier<String> get, Consumer<String> set) {
        var builder = Option.<String>createBuilder().name(text(name)).binding(fallback, get, set).controller(StringControllerBuilder::create);
        if (description != null && !description.isEmpty()) {
            builder.description(OptionDescription.of(text(description)));
        }
        return builder.build();
    }

    public static ButtonOption button(String name, String buttonText, String description, Runnable action) {
        var builder = ButtonOption.createBuilder().name(text(name))
            .action((screen, option) -> action.run());
        if (buttonText != null && !buttonText.isEmpty()) {
            builder.text(text(buttonText));
        }
        if (description != null && !description.isEmpty()) {
            builder.description(OptionDescription.of(text(description)));
        }
        return builder.build();
    }

    public static ButtonOption button(String name, String description, Runnable action) {
        return button(name, null, description, action);
    }

    public static ListOption<String> strings(String name, String description, Supplier<List<String>> get, Consumer<List<String>> set) {
        var builder = ListOption.<String>createBuilder().name(text(name))
            .binding(new ArrayList<>(), () -> new ArrayList<>(get.get()), value -> set.accept(new ArrayList<>(value)))
            .controller(StringControllerBuilder::create).initial("").insertEntriesAtEnd(true);
        if (description != null && !description.isEmpty()) {
            builder.description(OptionDescription.of(text(description)));
        }
        return builder.build();
    }
}

