/**
 * The NaivePlayer class represents an automatic player
 * that places his mark in the first available cell.
 */
public class NaivePlayer implements Player{
    /**
     * Default constructor fot naive player
     */
    public NaivePlayer(){}

    /**
     * Plays the turn by placing the mark in the first empty position,
     * scanning the board from top-left to bottom-right
     *
     * @param board the board to put the mark in
     * @param mark the mark to put in the board
     */
    @Override
    public void playTurn (Board board, Mark mark){
        for(int row=0; row<board.getSize(); row++){
            for(int col=0; col<board.getSize(); col++){
                if(board.putMark(mark, row, col)){
                    return;
                }
            }
        }
    }
}
