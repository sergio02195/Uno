package br.stm.uno;

import java.util.ArrayList;
import java.util.Collections;

class DrawPile extends ArrayList<Card> {

    DrawPile(int nDecks) {
        super(Card.getDeck(nDecks));
        Collections.shuffle(this);
        System.out.println("We need " + nDecks + " decks");
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
        System.out.println("Withdrawing " + nCards + " cards from the draw pile");
        ArrayList<Card> cards = new ArrayList<>();
        for (int i = 0; i < nCards; i++)
            // Withdrawing cards from the "top" of the pile
            cards.add(remove(size() - 1));

        return cards;
    }
}
