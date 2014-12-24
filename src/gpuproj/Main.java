package gpuproj;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.games.*;
import gpuproj.player.MCTSPlayer;
import gpuproj.player.UCBPlayer;
import gpuproj.simulator.CPU1Simulator;
import gpuproj.srctree.DirectorySourceProvider;
import gpuproj.srctree.Symbol;
import gpuproj.srctree.TypeIndex;

import java.io.File;

public class Main
{
    public static void main(String[] args) {
        //MCTSvsUCB(new GoMoku());
        decompClass();
    }

    private static <B extends Board<B>> void MCTSvsUCB(BoardGame<B> game) {
        GameMachine.playGames(game,
                new MCTSPlayer<B>(game, CPU1Simulator.instance),
                new UCBPlayer<B>(game, CPU1Simulator.instance),
                200, 500);
    }

    private static void decompClass() {
        TypeIndex.newInstance();
        TypeIndex.instance.sourceProviders.add(new DirectorySourceProvider(new File("D:\\QUT\\VRES\\GPU\\project\\src")));




        Symbol sym = TypeIndex.instance.resolveSingle("gpuproj.games.Ataxx", Symbol.CLASS_SYM);
        new Object();
    }
}
