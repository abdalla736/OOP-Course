/**
 * The SmartPlayer represents an automatic player,
 * it places the mark in a smart way,
 * and wins all other automatic player.
 */
public class SmartPlayer implements Player{

    /**
     * constructs the smart player
     */
    public SmartPlayer(){}

    /**
     * play a turn by placing the mark smartly
     *
     * @param board the board to put the mark in
     * @param mark the mark to put in the board
     */
    @Override
    public void playTurn (Board board, Mark mark){
        for(int rowNext = 0; rowNext<board.getSize(); rowNext++) {
            for (int col = 0; col < board.getSize(); col++) {
                for (int row = rowNext; row < board.getSize(); row++) {
                    if (board.getMark(row, col) != mark && board.getMark(row, col) != Mark.BLANK) {
                        break;
                    } else {
                        if (board.putMark(mark, row, col)) {
                            return;
                        }
                    }
                }
            }
        }
    }
}
