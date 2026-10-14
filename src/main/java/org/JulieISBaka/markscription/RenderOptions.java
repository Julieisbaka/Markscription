package org.JulieISBaka.markscription;

import java.util.EnumSet;
import java.util.Set;

public record RenderOptions(Set<Feature> features) {
    public static final RenderOptions DEFAULT = new RenderOptions(EnumSet.allOf(Feature.class));

    public RenderOptions {
        features = Set.copyOf(features);
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
        return new RenderOptions(updated);
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
