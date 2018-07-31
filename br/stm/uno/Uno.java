package br.stm.uno;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
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

    //FIXME: Remove duplicate entries
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
                                    possibleMoves.add("Choose " + color + "and play " + wcType + " and choose "
                                            + color2);
                                }
                            }
                        }

                        possibleMoves.add("Choose " + color + " and buy a card");

                        Card nextOnDrawPile = drawPile.get(drawPile.size() - 1);
                        if (nextOnDrawPile.getColor() != null) {
                            if (Objects.equals(nextOnDrawPile.getColor(), color)) {
                                possibleMoves.add("Choose " + color + "and buy a card then play it ("
                                        + nextOnDrawPile + ")");
                            }
                        } else {
                            if (nextOnDrawPile.getType().equals(CardType.WILDCARD)) {
                                for (CardColor color2 : CardColor.values()) {
                                    possibleMoves.add("Choose " + color + "and buy wildcard then play it and choose "
                                            + color2);
                                }
                            } else if (canPlayBuyFour) {
                                for (CardColor color2 : CardColor.values()) {
                                    possibleMoves.add("Choose " + color + "and buy buy-four then play it and choose "
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
                    possibleMoves.add("Buy a card then play it (" + nextOnDrawPile + ")");
                }
            } else {
                if (nextOnDrawPile.getType().equals(CardType.WILDCARD)) {
                    for (CardColor color : CardColor.values()) {
                        possibleMoves.add("Buy wildcard then play it and choose " + color);
                    }
                } else if (canPlayBuyFour) {
                    for (CardColor color : CardColor.values()) {
                        possibleMoves.add("Buy buy-four then play it and choose " + color);
                    }
                }
            }

        }
        return possibleMoves.toArray(new String[0]);
    }


    private void chooseAction() {
        System.out.println("-------------------------------------");
        String[] possibleActions = getPossibleMoves(players.getCurrentPlayer());

        System.out.println("Last card played: " + discardPile.getLastCard());
        System.out.println("Discard pile's current color: " + discardPile.getLastColor());
        System.out.println("Current player index: " + players.getCurrentIndex());
        System.out.println("Current player's cards: " + players.getCurrentPlayer());
        System.out.println("Possible actions: " + Arrays.toString(possibleActions));

        // For now it chooses a random action
        doAction(possibleActions[ThreadLocalRandom.current().nextInt(possibleActions.length)]);

    }

    private void doAction(String possibleAction) {
        if (possibleAction.startsWith("Choose")) {
            String[] commands = possibleAction.split("and");
            discardPile.setLastColor(CardColor.getByName(commands[0].split(" ")[1]));
            for (int i = 1; i < commands.length; i++) {
                String command = commands[i].trim();
                if (command.startsWith("play card at")) {
                    int cardIndex = Integer.parseInt(command.split("index")[1].trim());
                    discardPile.discard(players.getCurrentPlayer().remove(cardIndex));
                } else if (command.startsWith("choose")) {
                    discardPile.setLastColor(CardColor.getByName(command.split("oose")[1]));
                } else if (command.startsWith("buy")) {
                    Card bought = drawPile.withdraw(1, discardPile).get(0);
                    if (command.contains("then play")) {
                        discardPile.discard(bought);
                    } else {
                        players.getCurrentPlayer().add(bought);
                    }
                }
            }
        } else if (possibleAction.startsWith("Buy")) {
            switch (possibleAction) {
                case "Buy a card":
                    players.getCurrentPlayer().addAll(drawPile.withdraw(1, discardPile));
                    break;
                case "Buy two cards":
                    players.getCurrentPlayer().addAll(drawPile.withdraw(2, discardPile));
                    discardPile.setPendingAction(false);
                    break;
                case "Buy four cards":
                    players.getCurrentPlayer().addAll(drawPile.withdraw(4, discardPile));
                    discardPile.setPendingAction(false);
                    break;
                default:
                    discardPile.discard(drawPile.withdraw(1, discardPile).get(0));
                    if (possibleAction.contains("choose")) {
                        String chosenColor = possibleAction.split("choose")[1].trim();
                        discardPile.setLastColor(CardColor.getByName(chosenColor));
                    }
                    break;
            }
        } else if (possibleAction.startsWith("Play")) {
            int cardIndex = Integer.parseInt(possibleAction.replaceAll("\\D+", ""));
            Card chosen = players.getCurrentPlayer().remove(cardIndex);
            discardPile.discard(chosen);
            if (possibleAction.contains("choose")) {
                String chosenColor = possibleAction.split("choose")[1].trim();
                discardPile.setLastColor(CardColor.getByName(chosenColor));
                discardPile.setPendingAction(possibleAction.contains("four"));
            }
        } else if (possibleAction.startsWith("Invert")) {
            players.invertOrder();
            discardPile.setPendingAction(false);
        } else if (possibleAction.startsWith("Skip")) {
            System.out.println("Skipping current player");
            players.nextPlayer();
            discardPile.setPendingAction(false);
        }
    }

    boolean nextMove() {
        int winnerIndex = getWinnerIndex();
        if (winnerIndex == -1) {
            chooseAction();
            players.nextPlayer();
            return true;
        } else {
            System.out.println("Game over, player " + winnerIndex + " won");
            return false;
        }
    }

}
