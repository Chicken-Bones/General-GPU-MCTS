package gpuproj.game;

/**
 * Contains static utility functions for manipulating 8x8 bitboards represented as longs, where the bit index for a given x,y is y*8+x (y<<3|x) and the origin is in the lower left corner.
 */
public class BitBoard
{
    public static long checkerboard = 0xAAAAAAAAAAAAAAAAL;
    public static char SQUARE = '\u25A1';

    public static String format8(long board, char high) {
        return format8(board, high, '0');
    }

    public static String format8(long board, char high, char low) {
        StringBuilder sb = new StringBuilder(64+7);
        for(int y = 7; y >= 0; y--) {
            for(int x = 0; x < 8; x++)
                sb.append((board >> (y<<3|x) & 1) == 0 ? low : high);
            if(y != 0)
                sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Overlays all non-0 chars from boards onto base
     */
    public static String overlay(String base, String... boards) {
        char[] chars = base.toCharArray();
        for(int c = 0; c < chars.length; c++)
            for(int b = 0; b < boards.length; b++) {
                char ch = boards[b].charAt(c);
                if(ch != '0') chars[c] = ch;
            }
        return String.valueOf(chars);
    }

    public static long shift(long board, int i) {
        return i < 0 ? board >>> -i : board << i;
    }

    public static long shiftDir(long board, int dir) {
        return shift(board, shiftDirs[dir]) & maskDirs[dir];
    }

    /**
     * Shift integers for directions, including diagonals
     * {up, up-right, right, down-right, down, down-left, left, up-left}
     */
    public static int[] shiftDirs = new int[]{8, 9, 1, -7, -8, -9, -1, -7};
    /**
     * Shift masks for directions including diagonals
     * {up, up-right, right, down-right, down, down-left, left, up-left}
     */
    public static long[] maskDirs = new long[]{
            0xFFFFFFFFFFFFFFFFL,
            0xFEFEFEFEFEFEFEFEL,
            0xFEFEFEFEFEFEFEFEL,
            0xFEFEFEFEFEFEFEFEL,
            0xFFFFFFFFFFFFFFFFL,
            0x7F7F7F7F7F7F7F7FL,
            0x7F7F7F7F7F7F7F7FL,
            0x7F7F7F7F7F7F7F7FL};

    public static long shiftU(long board) {
        return board << 8;
    }

    public static long shiftD(long board) {
        return board >>> 8;
    }

    public static long shiftR(long board) {
        return board << 1 & 0xFEFEFEFEFEFEFEFEL;
    }

    public static long shiftL(long board) {
        return board >>> 1 & 0x7F7F7F7F7F7F7F7FL;
    }

    public static long dilate(long board) {
        return board | shiftU(board) | shiftD(board) | shiftR(board) | shiftL(board);
    }

    public static long dilate8(long board) {
        board |= shiftR(board) | shiftL(board);
        return board | shiftU(board) | shiftD(board);
    }

    public static String format(byte[][] board, Object... chars) {
        StringBuilder sb = new StringBuilder(board.length*board[0].length+board.length);
        for (int y = board[0].length-1; y >= 0; y--) {
            x: for (int x = 0; x < board.length; x++) {
                int b = board[x][y];
                for (int i = 0; i < chars.length; i += 2)
                    if ((Integer) chars[i] == b) {
                        sb.append(chars[i + 1]);
                        continue x;
                    }
                sb.append(0);
            }
            if(y > 0) sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * @return The winner based on who has the most pieces on the board\
     * @param white The bitboard of white pieces
     * @param black The bitboard of black pieces
     */
    public static int scoreWinner(long white, long black) {
        int w = Long.bitCount(white);
        int b = Long.bitCount(black);
        return w == b ? 2 : w > b ? 0 : 1;
    }
}
