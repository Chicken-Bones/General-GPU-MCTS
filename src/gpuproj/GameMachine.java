package gpuproj;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;
import gpuproj.player.Player;
import gpuproj.simulator.PlayoutSimulator;

public class GameMachine
{
    /**
     * Plays a game between p1 and p2, returning the winner, 0 for p1, 1 for p2 and 2 for a draw
     * @param limit Time limit for each turn (ms)
     */
    public static <B extends Board<B>> int play(BoardGame<B> game, Player<B> p1, Player<B> p2, long limit) {
        B board = game.getStartingBoard();
        p1.startGame(board.copy());
        p2.startGame(board.copy());
        int winner;
        int cnt = 0;
        StatDialog statBoard = StatDialog.get("Board");
        statBoard.clear();
        statBoard.println("Turn: 0\n" + board);
        PlayoutSimulator.resetLog();

        do {
            Player<B> turn = board.getTurn() == 0 ? p1 : p2;
            Player<B> opp = board.getTurn() == 0 ? p2 : p1;
            Move<B> move = turn.selectMove(limit);
            opp.applyMove(move);
            board = move.apply(board);
            winner = game.checkWinner(board);

            cnt++;
            statBoard.println("Turn: "+cnt+"\n"+board);
            PlayoutSimulator.logTurn(cnt, turn, limit);
        } while(winner < 0);
        StatDialog.get("Game Results").println(cnt+" turns. "+(winner == 2 ? "Draw" : winner == 0 ? p1+" (White)" : p2+" (Black)"));
        return winner;
    }

    public static <B extends Board> void playGames(BoardGame<B> game, Player<B> p1, Player<B> p2, int games, long limit) {
        double score = 0;
        for(int i = 0; i < games; i++) {
            int swap = i % 2;
            score += BoardGame.score(GameMachine.play(game,
                    swap == 0 ? p1 : p2, swap == 0 ? p2 : p1,
                    limit), swap);
            StatDialog.get("Score").setText(p1+" won "+score+"/"+(i+1)+" vs "+p2+" @ "+limit+"ms");
        }
    }
}
