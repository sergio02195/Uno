package br.stm.uno;

public enum CardColor {
    RED, GREEN, YELLOW, BLUE;

    static CardColor getByName(String name) {
        for (CardColor color : values()) {
            if (color.toString().equalsIgnoreCase(name.trim())) {
                return color;
            }
        }
        return null;
    }
}
