package br.stm.uno;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

class Uno {

    private Players players;
    private DrawPile drawPile;
    private DiscardPile discardPile;
    private boolean silentMode;

    private int totalTurns;

    Uno(int nPlayers,boolean silentMode) {
        this.silentMode = silentMode;
        drawPile = new DrawPile((int) Math.ceil(nPlayers / 8.0),silentMode);
        players = new Players(nPlayers, drawPile,silentMode);
        discardPile = new DiscardPile(drawPile,silentMode);
    }

     Uno cloneGame() {
        Uno clone = new Uno(players.size(), true);
        clone.drawPile.clear();
        clone.drawPile.addAll(drawPile);

        clone.discardPile.clear();
        clone.discardPile.addAll(discardPile);

        clone.players.clear();
        //clone.players.addAll(players);
         for (Player p : players){
             Player a = new Player();
             a.addAll(p);
             clone.players.add(a);
         }

        clone.totalTurns = totalTurns;
        clone.players.currentPlayer = players.getCurrentIndex();

        return clone;
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
            if (buyFourIndex == -1)
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
    String[] getPossibleMoves(Player player) {
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

    private void Print(String text){
        if(!silentMode) System.out.println(text);
    }

    void chooseAction() {
        Print("-------------------------------------");
        String[] possibleActions = getPossibleMoves(players.getCurrentPlayer());

        Print("Last card played: " + discardPile.getLastCard());
        Print("Discard pile's current color: " + discardPile.getLastColor());
        Print("Current player index: " + players.getCurrentIndex());
        Print("Action pending: " + discardPile.hasPendingAction());
        Print("Number of cards on draw pile: " + drawPile.size());
        Print("Number of cards on discard pile: " + discardPile.size());
        Print("Current player's cards: " + players.getCurrentPlayer());
        Print("Possible actions: " + Arrays.toString(possibleActions));

        // For now it chooses a random action
        String escolha;
        if(players.getCurrentIndex() == 0) {
            escolha = possibleActions[ThreadLocalRandom.current().nextInt(possibleActions.length)];
        }else {
            long tempoInicial = System.currentTimeMillis();
            //var decisao = minimax(this.cloneGame(), 0);
            //System.out.println("minimax tempo : " + (System.currentTimeMillis() - tempoInicial)+" ms");

            var decisao = alfaBeta(this.cloneGame(), 0, -Integer.MAX_VALUE,Integer.MAX_VALUE);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date today = Calendar.getInstance().getTime();

            System.out.println(df.format(today)+": alfaBeta tempo : " + (System.currentTimeMillis() - tempoInicial)+" ms, decisao : "+decisao.x);

            if(decisao.y == -1)
                escolha = possibleActions[ThreadLocalRandom.current().nextInt(possibleActions.length)];
            else
                escolha = decisao.x;
        }
        doAction(escolha);
        totalTurns++;

    }

    private Tuple<String, Integer> alfaBeta(Uno uno, int depth, int alfa, int beta){
        // verificar estado do jogo e retornar euristica da folha
        int winner = uno.getWinnerIndex();
        if(winner != -1){
            switch(winner){
                case 0:
                    return new Tuple<>("", 1000-depth);
                case 1:
                    return new Tuple<>("", depth-1000);
            }
        }
        // testar cada jogada recursivamente
        depth++;
        //limitador de profundidade
        if (depth > 10) return new Tuple<>("", depth-1000);

        if(uno.players.getCurrentIndex() == 0){
            //human
            Tuple<String, Integer> melhorJogada = new Tuple<>("",Integer.MAX_VALUE);
            for(String jogada : uno.getPossibleMoves(uno.players.getCurrentPlayer())){
                Uno jogoPossivel = uno.cloneGame();
                jogoPossivel.doAction(jogada);
                totalTurns++;

                Tuple<String, Integer> valor = alfaBeta(jogoPossivel, depth, alfa, beta);
                melhorJogada = valor.y < melhorJogada.y ? new Tuple<>(jogada, valor.y) : melhorJogada ;

                beta = Math.min(beta, melhorJogada.y);
                if(beta <= alfa)
                    break;
            }
            return melhorJogada;
        }else{
            //!human
            Tuple<String, Integer> melhorJogada = new Tuple<>("",-Integer.MAX_VALUE);
            for(String jogada : uno.getPossibleMoves(uno.players.getCurrentPlayer())){
                Uno jogoPossivel = uno.cloneGame();
                jogoPossivel.doAction(jogada);
                totalTurns++;

                Tuple<String, Integer> valor = alfaBeta(jogoPossivel, depth, alfa, beta);
                melhorJogada = valor.y > melhorJogada.y ? new Tuple<>(jogada, valor.y) : melhorJogada ;

                alfa = Math.max(alfa, melhorJogada.y);
                if(beta <= alfa)
                    break;
            }
            return melhorJogada;
        }
    }

    private Tuple<String, Integer> minimax(Uno uno, int depth){
        // verificar estado do jogo e retornar euristica da folha
        int winner = uno.getWinnerIndex();
        if(winner != -1){
            switch(winner){
                case 0:
                    return new Tuple<>("", 1000-depth);
                case 1:
                    return new Tuple<>("", depth-1000);
            }
        }
        // testar cada jogada recursivamente
        depth++;
        //limitador de profundidade
        if (depth > 10) return new Tuple<>("", depth-1000);

        List<Tuple<String, Integer>> resultados = new ArrayList<>();
        for (String jogada : uno.getPossibleMoves(uno.players.getCurrentPlayer()) ){
            Uno jogoPossivel = uno.cloneGame();
            jogoPossivel.doAction(jogada);
            totalTurns++;
            int winner2 = jogoPossivel.getWinnerIndex();
            if(winner2 != -1){
                switch(winner2){
                    case 0:
                        //resultados.add(new Tuple<>(jogada, 1000-depth));
                        return new Tuple<>(jogada, 1000-depth);
                    case 1:
                        //resultados.add(new Tuple<>(jogada, depth-1000));
                        return new Tuple<>(jogada, depth-1000);
                }
            }
            else {
                jogoPossivel.players.nextPlayer();

                // adicionar jogada juntamente com sua euristica
                resultados.add(new Tuple<>(jogada, minimax(jogoPossivel, depth).y));
            }
        }
        // retornar a melhor jogada dependendo do jogador
        if(uno.players.getCurrentIndex() == 0){
            return resultados.stream().max(Comparator.comparing(i -> i.y)).orElse(new Tuple<>("",-1));
        }else{
            return resultados.stream().min(Comparator.comparing(i -> i.y)).orElse(new Tuple<>("",-1));
        }
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
            discardPile.setPendingAction(possibleAction.contains("four"));
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
                    Card chosen = drawPile.withdraw(1, discardPile).get(0);
                    discardPile.discard(chosen);
                    if (possibleAction.contains("choose")) {
                        String chosenColor = possibleAction.split("choose")[1].trim();
                        discardPile.setLastColor(CardColor.getByName(chosenColor));
                    } else {
                        // Invert, skip and buy two trigger the "pending action" boolean for the next player
                        discardPile.setPendingAction(chosen.getValue() == 20);
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
            } else {
                // Invert, skip and buy two trigger the "pending action" boolean for the next player
                discardPile.setPendingAction(chosen.getValue() == 20);
            }
        } else if (possibleAction.startsWith("Invert")) {
            players.invertOrder();
            discardPile.setPendingAction(false);
        } else if (possibleAction.startsWith("Skip")) {
            Print("Skipping current player");
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
            System.out.println("Game over, player " + winnerIndex + " won in " + totalTurns + " turns");
            return false;
        }
    }

    public int getTotalTurns() {
        return totalTurns;
    }
}
