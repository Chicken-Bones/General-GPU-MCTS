package gpuproj.game;

import java.util.List;

/**
 * Parent class for all board games
 * @param <B> The board class
 */
public abstract class BoardGame<B extends Board>
{
    /**
     * @return The board for the start of the game, may choose a random player
     */
    public abstract B getStartingBoard();

    /**
     * Return a list of boards representing all possible moves. Will only be called with boards for which checkWinner returns -1
     * @param board The board state, may not be safe to mutate.
     */
    public abstract List<? extends Move<B>> expand(B board);

    /**
     * @return -1 if the game should continue, 0 for the 1st player, 1 for the 2nd player, 2 for a draw
     * @param board The board state, may not be safe to mutate.
     */
    public abstract int checkWinner(B board);

    /**
     * @return The UCT exploration parameter in UCT
     */
    public double getExploration() {
        return Math.sqrt(2);
    }

    /**
     * Apply a random move to board
     * @param board The board state, should be mutated
     */
    public abstract void playRandomMove(B board);

    /**
     * @return winner == 2 ? 0.5F : winner == player ? 1 : 0
     */
    public static float score(int winner, int player) {
        return floatScore(score_i(winner, player), 1);
    }

    /**
     * @return winner == 2 ? 0 : winner == player ? 1 : -1
     */
    public static int score_i(int winner, int player) {
        return winner == 2 ? 0 : winner == player ? 1 : -1;
    }

    public static float floatScore(int score, int games) {
        return (score+games)/2F;
    }
}
