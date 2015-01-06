package gpuproj;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.games.Ataxx;
import gpuproj.player.MCTSPlayer;
import gpuproj.player.UCBPlayer;
import gpuproj.simulator.CPU1Simulator;
import gpuproj.simulator.GPUSimulator;
import gpuproj.srctree.*;

import java.io.File;

public class Main
{
    public static void main(String[] args) {
        //MCTSvsUCB(new GoMoku());
        TypeIndex.sourceProviders.add(new DirectorySourceProvider(new File("D:\\QUT\\VRES\\GPU\\project\\src")));
        CPUvsGPU(new Ataxx(), 32);
    }

    private static <B extends Board<B>> void MCTSvsUCB(BoardGame<B> game) {
        GameMachine.playGames(game,
                new MCTSPlayer<>(game, CPU1Simulator.instance),
                new UCBPlayer<>(game, CPU1Simulator.instance),
                200, 500);
    }

    private static <B extends Board<B>> void CPUvsGPU(BoardGame<B> game, int simsPerNode) {
        GameMachine.playGames(game,
                new MCTSPlayer<>(game, CPU1Simulator.instance),
                new MCTSPlayer<>(game, new GPUSimulator(simsPerNode)),
                200, 500);
    }
}
