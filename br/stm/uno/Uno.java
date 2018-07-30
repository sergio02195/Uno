package br.stm.uno;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

class Uno {

    private Players players;
    private DrawPile drawPile;
    private DiscardPile discardPile;

    private int totalTurns;

    Uno(int nPlayers) {
        drawPile = new DrawPile();
        players = new Players(nPlayers, drawPile);
        discardPile = new DiscardPile(drawPile);
    }

    private int getWinnerIndex() {
        return IntStream.range(0, players.size()).filter(i -> players.get(i)
                .isEmpty()).findFirst().orElse(-1);
    }

    private Integer[] getMatchingCards(Player player, Card testCard, CardColor testColor) {
        ArrayList<Integer> matches = new ArrayList<>();
        boolean colorMatch = false;
        int buyFourIndex = -1;
        for (int i = 0; i < player.size(); i++) {
            Card c = player.get(i);
            buyFourIndex = c.getType().equals(CardType.WILDCARD_BUY_FOUR) ? i : -1;
            boolean sameColor = Objects.equals(c.getColor(), testColor);
            if (sameColor) {
                colorMatch = matches.add(i);
            } else if (testCard != null) {
                boolean isWildCard = c.getType().equals(CardType.WILDCARD),
                        sameType = Objects.equals(c.getType(), testCard.getType()),
                        isNumber = c.getType().equals(CardType.NUMBER),
                        sameValue = c.getValue() == testCard.getValue();
                if (isWildCard || ((sameType && !isNumber) || (isNumber && sameValue)))
                    matches.add(i);
            }
        }
        // We can only use a buy-four if we don't have a card
        // with the same color as the last card played
        if (!colorMatch && buyFourIndex > -1)
            matches.add(buyFourIndex);

        return matches.toArray(new Integer[0]);
    }

    private String[] getPossibleMoves(Player player) {
        ArrayList<String> possibleMoves = new ArrayList<>();
        if (discardPile.hasPendingAction()) {
            switch (discardPile.getLastCard().getType()) {
                case WILDCARD:
                    /*
                    This case is only possible if the game starts with a wildcard.
                    Possible outcomes:
                    - Player chooses a color and plays a non-wildcard  card
                    - Player chooses a color, plays a wildcard and chooses a color

                    - Player chooses a color, buys a card and doesn't play it
                    - Player chooses a color, buys a non-wildcard card and plays it
                    - Player chooses a color, buys a wildcard, plays it and chooses a color
                     */
                    boolean canPlayBuyFour = true;
                    for (CardColor color : CardColor.values()) {
                        Integer[] possibleChoices = getMatchingCards(player, null, color);
                        for (int index : possibleChoices) {
                            Card possibleCard = player.get(index);
                            if (possibleCard.getColor() != null) {
                                possibleMoves.add("Choose " + color + " and play card at index " + index);
                                if (canPlayBuyFour) {
                                    canPlayBuyFour = !Objects.equals(possibleCard.getColor(), color);
                                }
                            } else {
                                String wcType = possibleCard.getType().equals(CardType.WILDCARD) ? "wildcard"
                                        : "buy-four";
                                for (CardColor color2 : CardColor.values()) {
                                    possibleMoves.add("Choose " + color + ", play " + wcType + " and choose " + color2);
                                }
                            }
                        }

                        possibleMoves.add("Choose " + color + " and buy a card");

                        Card nextOnDrawPile = drawPile.get(drawPile.size() - 1);
                        if (nextOnDrawPile.getColor() != null) {
                            if (Objects.equals(nextOnDrawPile.getColor(), color)) {
                                possibleMoves.add("Choose " + color + ", buy a card and play it ("
                                        + nextOnDrawPile + ")");
                            }
                        } else {
                            if (nextOnDrawPile.getType().equals(CardType.WILDCARD)) {
                                for (CardColor color2 : CardColor.values()) {
                                    possibleMoves.add("Choose " + color + ", buy wildcard, play it and choose "
                                            + color2);
                                }
                            } else if (canPlayBuyFour) {
                                for (CardColor color2 : CardColor.values()) {
                                    possibleMoves.add("Choose " + color + ", buy buy-four, play it and choose "
                                            + color2);
                                }
                            }
                        }
                    }
                    break;
                case WILDCARD_BUY_FOUR:
                    possibleMoves.add("Buy four cards");
                    break;
                case BUY_TWO:
                    possibleMoves.add("Buy two cards");
                    break;
                case INVERT:
                    possibleMoves.add("Invert game order");
                    break;
                case SKIP:
                    possibleMoves.add("Skip me");
            }
        } else {
            /* Now these are the options if there's no pending action:
            - Play a matching non-wildcard card
            - Play a wildcard and choose a color
            - Buy a non-wildcard card and play it
            - Buy a wildcard, play it and choose a color
            - Buy a card
             */
            Integer[] possibleChoices = getMatchingCards(player, discardPile.getLastCard(), discardPile.getLastColor());

            Card nextOnDrawPile = drawPile.get(drawPile.size() - 1);
            boolean canPlayBuyFour = true;

            for (int index : possibleChoices) {
                Card possibleCard = player.get(index);
                if (player.get(index).getColor() != null) {
                    possibleMoves.add("Play card at index " + index);
                    if (canPlayBuyFour) {
                        canPlayBuyFour = !Objects.equals(possibleCard.getColor(), discardPile.getLastColor());
                    }
                } else {
                    String wcType = possibleCard.getType().equals(CardType.WILDCARD) ? "wildcard"
                            : "buy-four";
                    for (CardColor color : CardColor.values()) {
                        possibleMoves.add("Play " + wcType + " at index " + index + " and choose " + color);
                    }
                }
            }

            possibleMoves.add("Buy a card");

            if (nextOnDrawPile.getColor() != null) {
                if (Objects.equals(nextOnDrawPile.getColor(), discardPile.getLastColor())) {
                    possibleMoves.add("Buy a card and play it (" + nextOnDrawPile + ")");
                }
            } else {
                if (nextOnDrawPile.getType().equals(CardType.WILDCARD)) {
                    for (CardColor color : CardColor.values()) {
                        possibleMoves.add("Buy wildcard, play it and choose " + color);
                    }
                } else if (canPlayBuyFour) {
                    for (CardColor color : CardColor.values()) {
                        possibleMoves.add("Buy buy-four, play it and choose " + color);
                    }
                }
            }

        }
        return possibleMoves.toArray(new String[0]);
    }

    private void chooseAction() {
        System.out.println("Last card played: " + discardPile.getLastCard());
        System.out.println("Current player index: " + players.getCurrentIndex());
        System.out.println("Current player's cards: " + players.getCurrentPlayer());
        System.out.println("Possible actions: " + Arrays.toString(getPossibleMoves(players.getCurrentPlayer())));
        System.out.println("-------------------------------------");
    }

    boolean nextMove() {
        if (getWinnerIndex() == -1) {
            chooseAction();
        }
        return false;
    }

}
