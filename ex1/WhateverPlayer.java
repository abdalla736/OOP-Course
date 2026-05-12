import java.util.Random;

/**
 * The WhateverPlayer class represents an automatic player
 * that places his mark in a random available cell.
 */
public class WhateverPlayer implements Player{
    Random rand = new Random();

    /**
     * construct the whatever player
     */
    public WhateverPlayer(){}

    /**
     * play a turn by placing the mark in random cell in the board.
     *
     * @param board the board to put the mark in
     * @param mark the mark to put in the board
     */
    @Override
    public void playTurn (Board board, Mark mark){
        int randcol;
        int randrow;
        do {
            randcol = rand.nextInt(board.getSize());
            randrow = rand.nextInt(board.getSize());
        }while(!board.putMark(mark, randrow, randcol));
    }
}
