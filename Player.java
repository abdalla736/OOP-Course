/**
 * interface that represent player
 */
public interface Player {
    /**
     * player play the current turn and puts mark in board
     *
     * @param board the board to put the mark in
     * @param mark the mark to put in the board
     */
    void playTurn(Board board, Mark mark);
}
