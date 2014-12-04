package gpuproj;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.games.*;
import gpuproj.player.MCTSPlayer;
import gpuproj.player.UCBPlayer;
import gpuproj.simulator.CPU1Simulator;

public class Main
{
    public static void main(String[] args) {
        MCTSvsUCB(new Squavalath());
    }

    private static <B extends Board<B>> void MCTSvsUCB(BoardGame<B> game) {
        GameMachine.playGames(game,
                new MCTSPlayer<B>(game, CPU1Simulator.instance),
                new UCBPlayer<B>(game, CPU1Simulator.instance),
                200, 500);
    }
}
