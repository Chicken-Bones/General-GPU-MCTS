package gpuproj.player;

import gpuproj.util.Portable;
import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.game.Move;

import java.util.List;

public class RandomPlayer<B extends Board<B>> extends Player<B>
{
    private B board;

    public RandomPlayer(BoardGame<B> game) {
        super(game);
    }

    @Override
    public void startGame(B board) {
        this.board = board;
    }

    @Override
    public Move<B> selectMove(long limit) {
        List<? extends Move<B>> moves = game.expand(board);
        return moves.get(Portable.randInt(moves.size()));
    }

    @Override
    public void applyMove(Move<B> move) {
        board = move.apply(board);
    }

    @Override
    public String toString() {
        return "Random";
    }
}
