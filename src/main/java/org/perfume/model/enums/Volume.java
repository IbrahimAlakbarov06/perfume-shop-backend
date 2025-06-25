package org.perfume.model.enums;

public enum Volume {
    ML_30("30ml"),
    ML_50("50ml"),
    ML_75("75ml"),
    ML_100("100ml"),
    ML_125("125ml"),
    ML_150("150ml"),
    ML_200("200ml");

    private final String displayName;

    Volume(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getValue() {
        return Integer.parseInt(displayName.replace("ml", ""));
    }
}
