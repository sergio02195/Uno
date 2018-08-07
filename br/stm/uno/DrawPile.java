package br.stm.uno;

import java.util.ArrayList;
import java.util.Collections;

class DrawPile extends ArrayList<Card> {

    private boolean silentMode;
    
    DrawPile(int nDecks,boolean silentMode) {
        super(Card.getDeck(nDecks));
        this.silentMode = silentMode;
        Collections.shuffle(this);
        Print("We need " + nDecks + " decks");
    }
    
    private void Print(String text){
        if(!silentMode) System.out.println(text);
    }

    ArrayList<Card> withdraw(int nCards, DiscardPile discardPile) {
        if (size() <= nCards) {
            Card top = remove(size() - 1);

            for (int i = 0; i < discardPile.size() - 1; i++)
                add(discardPile.remove(0));
            Print("Draw pile refilled with discard pile");

            Collections.shuffle(this);

            add(top);
        }
        Print("Withdrawing " + nCards + " cards from the draw pile");
        ArrayList<Card> cards = new ArrayList<>();
        for (int i = 0; i < nCards; i++)
            // Withdrawing cards from the "top" of the pile
            cards.add(remove(size() - 1));

        return cards;
    }
}
