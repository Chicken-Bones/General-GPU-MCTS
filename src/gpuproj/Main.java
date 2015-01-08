package gpuproj;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.player.MCTSPlayer;
import gpuproj.player.UCBPlayer;
import gpuproj.simulator.CPUSimulator;
import gpuproj.simulator.GPUSimulator;
import gpuproj.srctree.*;
import gpuproj.games.*;

import java.io.File;
import java.net.URISyntaxException;

public class Main
{
    public static void main(String[] args) {
        loadSourceDir(args);
        CPUvsGPU(new Squavalath(), 32);
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
                new MCTSPlayer<B>(game, new CPUSimulator(1)),
                new UCBPlayer<B>(game, new CPUSimulator(1)),
                200, 500);
    }

    private static <B extends Board<B>> void CPUvsGPU(BoardGame<B> game, int simsPerNode) {
        GameMachine.playGames(game,
                new MCTSPlayer<B>(game, new CPUSimulator(simsPerNode)),
                new MCTSPlayer<B>(game, new GPUSimulator(simsPerNode)),
                200, 500);
    }
}
