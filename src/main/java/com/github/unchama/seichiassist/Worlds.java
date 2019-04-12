package com.github.unchama.seichiassist;

public enum Worlds {


    ;

    private final boolean isSeichi;
    Worlds(boolean isSeichi) {
        this.isSeichi = isSeichi;
    }

    public boolean isSeichi() {
        return isSeichi;
    }
}
