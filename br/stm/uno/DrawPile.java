package br.stm.uno;

import java.util.ArrayList;
import java.util.Collections;

class DrawPile extends ArrayList<Card> {

    DrawPile() {
        super(Card.getDeck());
        Collections.shuffle(this);
    }

    ArrayList<Card> withdraw(int nCards, DiscardPile discardPile) {
        if (size() <= nCards) {
            Card top = remove(size() - 1);

            for (int i = 0; i < discardPile.size() - 1; i++)
                add(discardPile.remove(0));
            System.out.println("Draw pile refilled with discard pile");

            Collections.shuffle(this);

            add(top);
        }
        ArrayList<Card> cards = new ArrayList<>();
        for (int i = 0; i < nCards; i++)
            // Withdrawing cards from the "top" of the pile
            cards.add(remove(size() - 1));

        return cards;
    }
}
