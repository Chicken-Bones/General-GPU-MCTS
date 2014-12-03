package gpuproj;

import gpuproj.games.Ataxx;
import gpuproj.games.AtaxxBoard;
import gpuproj.games.Othello;
import gpuproj.game.SimpleBoard;
import gpuproj.player.MCTSPlayer;
import gpuproj.player.UCBPlayer;
import gpuproj.simulator.CPU1Simulator;

public class Main
{
    public static void main(String[] args) {
        Othello game = new Othello();
        GameMachine.playGames(game,
                new MCTSPlayer<SimpleBoard>(game, CPU1Simulator.instance),
                new UCBPlayer<SimpleBoard>(game, CPU1Simulator.instance),
                200, 500);
    }
}
