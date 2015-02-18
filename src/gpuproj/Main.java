package gpuproj;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.player.MCTSMultiPlayer;
import gpuproj.player.MCTSPlayer;
import gpuproj.player.UCBPlayer;
import gpuproj.simulator.CPUBlockSimulator;
import gpuproj.simulator.GPUBlockSimulator;
import gpuproj.simulator.GPUWarpSimulator;
import gpuproj.srctree.*;
import gpuproj.games.*;

import java.io.File;
import java.net.URISyntaxException;

public class Main
{
    public static void main(String[] args) {
        loadSourceDir(args);
        CPU1vsHybrid(new Ataxx());
    }

    private static void loadSourceDir(String[] args) {
        try {
            if(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).isDirectory()) {
                if(args.length < 1)
                    throw new IllegalArgumentException("Must specify source directory as program argument in a development environment");
                TypeIndex.sourceProviders.add(new DirectorySourceProvider(new File(args[0])));
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static <B extends Board<B>> void MCTSvsUCB(BoardGame<B> game) {
        GameMachine.playGames(game,
                new MCTSPlayer<B>(game, new CPUBlockSimulator(1)),
                new UCBPlayer<B>(game, new CPUBlockSimulator(1)),
                200, 500);
    }

    private static <B extends Board<B>> void CPUvsGPUWarp(BoardGame<B> game, int simsPerKernel) {
        GameMachine.playGames(game,
                new MCTSPlayer<B>(game, new CPUBlockSimulator(1)),
                new MCTSPlayer<B>(game, new GPUWarpSimulator(game.getClass(), simsPerKernel)),
                200, 500);
    }

    private static <B extends Board<B>> void CPUvsGPUBlock(BoardGame<B> game, int simsPerNode) {
        GameMachine.playGames(game,
                new MCTSPlayer<B>(game, new CPUBlockSimulator(simsPerNode)),
                new MCTSPlayer<B>(game, new GPUBlockSimulator(game.getClass(), simsPerNode)),
                200, 500);
    }

    private static <B extends Board<B>> void CPU1vsGPUBlock(BoardGame<B> game, int simsPerNode) {
        GameMachine.playGames(game,
                new MCTSPlayer<B>(game, new CPUBlockSimulator(1)),
                new MCTSPlayer<B>(game, new GPUBlockSimulator(game.getClass(), simsPerNode)),
                200, 500);
    }

    private static <B extends Board<B>> void CPU1vsHybrid(BoardGame<B> game) {
        GameMachine.playGames(game,
                new MCTSPlayer<B>(game, new CPUBlockSimulator(1)),
                new MCTSMultiPlayer<B>(game, 4, true),
                200, 500);
    }

    //bitboard vs array
    //CPU1 vs GPU
    //CPU1 vs hybrid
    //CPUmulti vs hybrid
}
