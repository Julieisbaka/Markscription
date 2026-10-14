package org.JulieISBaka.markscription.client;

import java.util.EnumMap;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.JulieISBaka.markscription.RenderOptions;
import org.JulieISBaka.markscription.RenderOptions.Feature;

final class ClothConfigScreen {
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
        builder.setSavingRunnable(() -> {
            RenderOptions updated = RenderOptions.DEFAULT;
            for (Feature feature : Feature.values()) {
                updated = updated.with(feature, selected.get(feature));
            }
            ClientSettings.save(updated);
        });
        return builder.build();
    }
}
