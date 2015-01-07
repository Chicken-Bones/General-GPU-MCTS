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

public class Main
{
    public static void main(String[] args) {
        //MCTSvsUCB(new GoMoku());
        TypeIndex.sourceProviders.add(new DirectorySourceProvider(new File("D:\\QUT\\VRES\\GPU\\project\\src")));
        CPUvsGPU(new Squavalath(), 32);
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
