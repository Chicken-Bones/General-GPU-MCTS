package gpuproj;

import gpuproj.game.Board;
import gpuproj.game.BoardGame;
import gpuproj.player.MCTSPlayer;
import gpuproj.player.UCBPlayer;
import gpuproj.simulator.CPU1Simulator;
import gpuproj.srctree.*;
import gpuproj.translator.RetentionSetEvaluator;
import gpuproj.translator.TranslatedStruct;

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
                new MCTSPlayer<>(game, CPU1Simulator.instance),
                new UCBPlayer<>(game, CPU1Simulator.instance),
                200, 500);
    }

    private static void decompClass() {
        TypeIndex.newInstance();
        TypeIndex.instance().sourceProviders.add(new DirectorySourceProvider(new File("D:\\QUT\\VRES\\GPU\\project\\src")));


        SourceClassSymbol game = (SourceClassSymbol) TypeIndex.instance().resolveType("gpuproj.games.Ataxx2");
        SourceClassSymbol board = (SourceClassSymbol) game.parent.params.get(0).concrete();
        MethodSymbol checkWinner = MethodSymbol.match(game.getMethods("checkWinner"), Arrays.asList(board));
        MethodSymbol playRandomMove = MethodSymbol.match(game.getMethods("playRandomMove"), Arrays.asList(board));
        RetentionSetEvaluator eval = new RetentionSetEvaluator();
        eval.add(board);
        eval.add(checkWinner);
        eval.add(playRandomMove);
        eval.search();
        TranslatedStruct struct = TranslatedStruct.translate(board);
        new Object();

    }
}
