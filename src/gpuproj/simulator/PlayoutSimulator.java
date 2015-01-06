package gpuproj.simulator;

import gpuproj.StatDialog;
import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.player.Player;
import gpuproj.player.TreeNode;

import java.util.List;

public abstract class PlayoutSimulator
{
    /**
     * Number of simulations performed this turn, subclasses should update this.
     */
    public static int simCount = 0;

    /**
     * Logs simulation speed info
     * @param turn The number of turns played in the current game
     * @param player The player who just played
     * @param time The time (in milliseconds) allocated for simulation this turn
     */
    public static void logTurn(int turn, Player player, long time) {
        if(simCount == 0) return;
        StatDialog.get("Sim Count").println("Turn "+turn+" ("+player+"): "+simCount+" in "+time+"ms "+(simCount*1000D/time)+"/sec");
        simCount = 0;
    }

    /**
     * Clears the log window (used at the start of a game)
     */
    public static void resetLog() {
        StatDialog.get("Sim Count").clear();
    }

    /**
     * Play at least one game for each node and update the results, should give positive values for wins for the player who played at this node, (board.getTurn()^1)
     */
    public abstract <B extends Board<B>> void play(List<TreeNode<B>> nodes, BoardGame<B> game);

    public static <B extends Board<B>> int playout(B board, BoardGame<B> game) {
        int player = board.getTurn()^1;
        int winner = game.checkWinner(board);
        while(winner < 0) {
            game.playRandomMove(board);
            winner = game.checkWinner(board);
        }
        return BoardGame.score_i(winner, player);
    }
}
