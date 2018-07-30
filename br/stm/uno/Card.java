package br.stm.uno;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Card {
    B2R(CardType.BUY_TWO, CardColor.RED, 20),
    B2B(CardType.BUY_TWO, CardColor.BLUE, 20),
    B2Y(CardType.BUY_TWO, CardColor.YELLOW, 20),
    B2G(CardType.BUY_TWO, CardColor.GREEN, 20),
    IR(CardType.INVERT, CardColor.RED, 20),
    IB(CardType.INVERT, CardColor.BLUE, 20),
    IY(CardType.INVERT, CardColor.YELLOW, 20),
    IG(CardType.INVERT, CardColor.GREEN, 20),
    SR(CardType.SKIP, CardColor.RED, 20),
    SB(CardType.SKIP, CardColor.BLUE, 20),
    SY(CardType.SKIP, CardColor.YELLOW, 20),
    SG(CardType.SKIP, CardColor.GREEN, 20),
    WC(CardType.WILDCARD, null, 50),
    WC4(CardType.WILDCARD_BUY_FOUR, null, 50),
    R0(CardType.NUMBER, CardColor.RED, 0),
    R1(CardType.NUMBER, CardColor.RED, 1),
    R2(CardType.NUMBER, CardColor.RED, 2),
    R3(CardType.NUMBER, CardColor.RED, 3),
    R4(CardType.NUMBER, CardColor.RED, 4),
    R5(CardType.NUMBER, CardColor.RED, 5),
    R6(CardType.NUMBER, CardColor.RED, 6),
    R7(CardType.NUMBER, CardColor.RED, 7),
    R8(CardType.NUMBER, CardColor.RED, 8),
    R9(CardType.NUMBER, CardColor.RED, 9),
    B0(CardType.NUMBER, CardColor.BLUE, 0),
    B1(CardType.NUMBER, CardColor.BLUE, 1),
    B2(CardType.NUMBER, CardColor.BLUE, 2),
    B3(CardType.NUMBER, CardColor.BLUE, 3),
    B4(CardType.NUMBER, CardColor.BLUE, 4),
    B5(CardType.NUMBER, CardColor.BLUE, 5),
    B6(CardType.NUMBER, CardColor.BLUE, 6),
    B7(CardType.NUMBER, CardColor.BLUE, 7),
    B8(CardType.NUMBER, CardColor.BLUE, 8),
    B9(CardType.NUMBER, CardColor.BLUE, 9),
    Y0(CardType.NUMBER, CardColor.YELLOW, 0),
    Y1(CardType.NUMBER, CardColor.YELLOW, 1),
    Y2(CardType.NUMBER, CardColor.YELLOW, 2),
    Y3(CardType.NUMBER, CardColor.YELLOW, 3),
    Y4(CardType.NUMBER, CardColor.YELLOW, 4),
    Y5(CardType.NUMBER, CardColor.YELLOW, 5),
    Y6(CardType.NUMBER, CardColor.YELLOW, 6),
    Y7(CardType.NUMBER, CardColor.YELLOW, 7),
    Y8(CardType.NUMBER, CardColor.YELLOW, 8),
    Y9(CardType.NUMBER, CardColor.YELLOW, 9),
    G0(CardType.NUMBER, CardColor.GREEN, 0),
    G1(CardType.NUMBER, CardColor.GREEN, 1),
    G2(CardType.NUMBER, CardColor.GREEN, 2),
    G3(CardType.NUMBER, CardColor.GREEN, 3),
    G4(CardType.NUMBER, CardColor.GREEN, 4),
    G5(CardType.NUMBER, CardColor.GREEN, 5),
    G6(CardType.NUMBER, CardColor.GREEN, 6),
    G7(CardType.NUMBER, CardColor.GREEN, 7),
    G8(CardType.NUMBER, CardColor.GREEN, 8),
    G9(CardType.NUMBER, CardColor.GREEN, 9);

    CardType type;
    CardColor color;
    int value;

    Card(CardType type, CardColor color, int value) {
        this.type = type;
        this.color = color;
        this.value = value;
    }

    public CardType getType() {
        return type;
    }

    public CardColor getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }

    public static List<Card> getDeck() {
        final List<Card> deck = new ArrayList<>(Arrays.asList(R0, B0, Y0, G0, WC, WC, WC4, WC4));
        for (int i = 0; i < 2; i++) {
            for (Card c : values()) {
                if (c.getValue() > 0) {
                    deck.add(c);
                }
            }
        }
        return deck;
    }

    @Override
    public String toString() {
        return (color != null ? color.toString().toLowerCase() + " " : "")
                + (type.equals(CardType.NUMBER) ? value : type.toString().toLowerCase());
    }
}