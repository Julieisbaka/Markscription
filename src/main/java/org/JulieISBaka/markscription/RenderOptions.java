package org.JulieISBaka.markscription;

import java.util.EnumSet;
import java.util.Set;
import java.util.Objects;

public record RenderOptions(Set<Feature> features, SyntaxTheme theme) {
    public static final RenderOptions DEFAULT = new RenderOptions(EnumSet.allOf(Feature.class));

    public RenderOptions {
        features = Set.copyOf(features);
        Objects.requireNonNull(theme, "theme");
    }

    public RenderOptions(Set<Feature> features) {
        this(features, SyntaxTheme.DEFAULT);
    }

    public boolean enabled(Feature feature) {
        return features.contains(feature);
    }

    public RenderOptions with(Feature feature, boolean enabled) {
        EnumSet<Feature> updated = EnumSet.noneOf(Feature.class);
        updated.addAll(features);
        if (enabled) {
            updated.add(feature);
        } else {
            updated.remove(feature);
        }
        return new RenderOptions(updated, theme);
    }

    public RenderOptions withTheme(SyntaxTheme updated) {
        return new RenderOptions(features, updated);
    }

    public enum Feature {
        MARKDOWN("markdown"),
        BOLD("bold"),
        ITALIC("italic"),
        STRIKETHROUGH("strikethrough"),
        HEADINGS("headings"),
        CODE("code"),
        LINKS("links"),
        SYNTAX_HIGHLIGHTING("syntaxHighlighting");

        private final String key;

        Feature(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }
}
