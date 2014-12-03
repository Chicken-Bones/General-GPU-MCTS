package gpuproj.player;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;

public abstract class Player<B extends Board>
{
    /**
     * The game to be played;
     */
    public BoardGame<B> game;

    public Player(BoardGame<B> game) {
        this.game = game;
    }

    /**
     * Set the starting board
     */
    public abstract void startGame(B board);

    /**
     * @param limit The time limit in milliseconds
     * @return The move selected by this player. If a game has a seperate move class, that class must be returned, not the board state
     */
    public abstract Move<B> selectMove(long limit);

    /**
     * Apply an oppponent's move. If the game has a seperate move class, move must be an instance of it, not the board class
     */
    public abstract void applyMove(Move<B> move);
}
