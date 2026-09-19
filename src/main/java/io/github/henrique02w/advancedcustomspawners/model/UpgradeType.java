package io.github.henrique02w.advancedcustomspawners.model;

public enum UpgradeType {
    SPEED("speed", "Velocidade"),
    AMOUNT("amount", "Quantidade"),
    RANGE("range", "Alcance"),
    ENVIRONMENT("environment", "Condicoes"),
    DROPS("drops", "Drops"),
    XP("xp", "XP"),
    FUEL("fuel", "Combustivel");

    private final String key;
    private final String display;

    UpgradeType(String key, String display) {
        this.key = key;
        this.display = display;
    }

    public String key() {
        return key;
    }

    public String display() {
        return display;
    }
}
