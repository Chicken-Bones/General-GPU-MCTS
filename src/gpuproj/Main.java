package gpuproj;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.games.*;
import gpuproj.player.MCTSPlayer;
import gpuproj.player.UCBPlayer;
import gpuproj.simulator.CPU1Simulator;
import gpuproj.srctree.*;

import java.io.File;
import java.util.Arrays;

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
        TypeIndex.instance().sourceProviders.add(new DirectorySourceProvider(new File("D:\\QUT\\VRES\\GPU\\project\\src")));


        ClassSymbol game = (ClassSymbol) TypeIndex.instance().resolveType("gpuproj.games.Ataxx2");
        ClassSymbol board = (ClassSymbol) game.parent.params.get(0).concrete();
        MethodSymbol checkWinner = MethodSymbol.match(game.getMethods("checkWinner"), Arrays.asList(new TypeRef(board)));
        checkWinner.loadBody();
        checkWinner.body.toString();
        new Object();
    }
}
