package io.shiftmanager.you.model;

public enum Timezone {
    MORNING("午前"),
    AFTERNOON("午後");

    private final String displayName;

    Timezone(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Timezone fromString(String value) {
        for (Timezone timezone : Timezone.values()) {
            if (timezone.name().equalsIgnoreCase(value)) {
                return timezone;
            }
        }
        throw new IllegalArgumentException("Invalid timezone: " + value);
    }
} 