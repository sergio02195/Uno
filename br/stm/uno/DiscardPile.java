package br.stm.uno;

import java.util.ArrayList;

class DiscardPile extends ArrayList<Card> {

    private boolean pendingAction;
    private CardColor lastColor;

    DiscardPile(DrawPile drawPile) {
        Card firstCard;
        // First card can't be a buy-four. If it is a buy-four
        // we put it at the bottom of the draw pile and pick
        // another one.
        while((firstCard = drawPile.remove(drawPile.size() - 1))
                .getType().equals(CardType.WILDCARD_BUY_FOUR))
            drawPile.add(0, firstCard);
//        firstCard = Card.WC;
        discard(firstCard);
        pendingAction = !firstCard.getType().equals(CardType.NUMBER);
    }

    boolean hasPendingAction() {
        return pendingAction;
    }

    void setPendingAction(boolean pendingAction) {
        this.pendingAction = pendingAction;
    }

    Card getLastCard() {
        return get(size() - 1);
    }

    CardColor getLastColor() {
        return lastColor;
    }

    void setLastColor(CardColor lastColor) {
        this.lastColor = lastColor;
    }

    void discard(Card card) {
        add(card);
        lastColor = card.getColor();
    }
}
