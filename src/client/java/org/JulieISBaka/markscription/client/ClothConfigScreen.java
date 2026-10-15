package org.JulieISBaka.markscription.client;

import java.util.EnumMap;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.JulieISBaka.markscription.RenderOptions;
import org.JulieISBaka.markscription.RenderOptions.Feature;
import org.JulieISBaka.markscription.CodeHighlighter.Token;
import org.JulieISBaka.markscription.SyntaxTheme;
import org.JulieISBaka.markscription.ThemeLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ClothConfigScreen {
        private static final Logger LOGGER = LoggerFactory.getLogger("markscription");
        private static final SystemToast.SystemToastId THEME_TOAST = new SystemToast.SystemToastId();

        private ClothConfigScreen() {
        }

        static Screen create(Screen parent) {
                ConfigBuilder builder = ConfigBuilder.create()
                                .setParentScreen(parent)
                                .setTitle(Component.translatable("markscription.config.title"));
                var category = builder.getOrCreateCategory(Component.translatable("markscription.config.category"));
                EnumMap<Feature, Boolean> selected = new EnumMap<>(Feature.class);
                RenderOptions current = ClientSettings.options();
                for (Feature feature : Feature.values()) {
                        selected.put(feature, current.enabled(feature));
                        String key = "markscription.config." + feature.key();
                        category.addEntry(builder.entryBuilder()
                                        .startBooleanToggle(Component.translatable(key), current.enabled(feature))
                                        .setDefaultValue(true)
                                        .setTooltip(Component.translatable(key + ".tooltip"))
                                        .setSaveConsumer(value -> selected.put(feature, value))
                                        .build());
                }
                var themeCategory = builder.getOrCreateCategory(Component.translatable("markscription.theme.title"));
                EnumMap<Token, Integer> colors = new EnumMap<>(Token.class);
                for (Token token : Token.values()) {
                        colors.put(token, current.theme().color(token));
                        themeCategory.addEntry(builder.entryBuilder()
                                        .startIntField(
                                                        Component.translatable("markscription.theme."
                                                                        + SyntaxTheme.key(token)),
                                                        current.theme().color(token))
                                        .setDefaultValue(SyntaxTheme.DEFAULT.color(token))
                                        .setTooltip(Component.translatable("markscription.theme.colors.tooltip"))
                                        .setSaveConsumer(value -> colors.put(token, value))
                                        .build());
                }
                var filename = builder.entryBuilder()
                                .startStrField(Component.translatable("markscription.theme.filename"), "my-theme.json")
                                .setDefaultValue("my-theme.json")
                                .setTooltip(Component.translatable("markscription.theme.filename.tooltip"))
                                .setErrorSupplier(value -> ThemeLibrary.validName(value) ? Optional.empty()
                                                : Optional.of(Component
                                                                .translatable("markscription.theme.filename.invalid")))
                                .build();
                themeCategory.addEntry(filename);
                var action = builder.entryBuilder()
                                .startEnumSelector(Component.translatable("markscription.theme.action"),
                                                ThemeAction.class, ThemeAction.NONE)
                                .setDefaultValue(ThemeAction.NONE)
                                .setEnumNameProvider(value -> Component.translatable(
                                                "markscription.theme.action." + value.name().toLowerCase(Locale.ROOT)))
                                .setTooltip(Component.translatable("markscription.theme.action.tooltip"))
                                .build();
                themeCategory.addEntry(action);
                builder.setSavingRunnable(() -> {
                        RenderOptions updated = RenderOptions.DEFAULT;
                        for (Feature feature : Feature.values()) {
                                updated = updated.with(feature, selected.get(feature));
                        }
                        try {
                                SyntaxTheme theme = action.getValue() == ThemeAction.IMPORT
                                                ? ClientSettings.THEMES.importTheme(filename.getValue())
                                                : new SyntaxTheme(colors);
                                if (action.getValue() == ThemeAction.EXPORT) {
                                        ClientSettings.THEMES.exportTheme(filename.getValue(), theme);
                                }
                                ClientSettings.save(updated.withTheme(theme));
                                if (action.getValue() != ThemeAction.NONE) {
                                        SystemToast.addOrUpdate(Minecraft.getInstance().gui.toastManager(), THEME_TOAST,
                                                        Component.translatable("markscription.theme.title"),
                                                        Component.translatable("markscription.theme.success."
                                                                        + action.getValue().name()
                                                                                        .toLowerCase(Locale.ROOT),
                                                                        filename.getValue()));
                                }
                        } catch (IOException exception) {
                                LOGGER.error("Could not save settings or complete theme file action {}; active settings were not changed",
                                                action.getValue(), exception);
                                SystemToast.addOrUpdate(Minecraft.getInstance().gui.toastManager(), THEME_TOAST,
                                                Component.translatable("markscription.theme.failure"),
                                                Component.translatable("markscription.theme.failure.details"));
                        }
                });
                return builder.build();
        }

        private enum ThemeAction {
                NONE, IMPORT, EXPORT
        }
}
