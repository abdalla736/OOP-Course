/**
 * Board class presents the board of the game Tic Tac Tao
 */
public class Board {
    private int size;
    private Mark[][] board;
    private final static int DEFAULT_SIZE_BOARD=4;

    /**
     * Default constructor, construct the Board
     */
    public Board(){
        this.size = DEFAULT_SIZE_BOARD;
        this.board = new Mark[size][size];
         for(int row = 0; row < size; row++){
             for(int col = 0; col < size; col++){
                 board[row][col] = Mark.BLANK;
             }
         }
    }

    /**
     * construct the board with size*size
     *
     * @param size the size of the board to be initialized with.
     */
    public Board (int size ){
         this.size = size;
         this.board = new Mark[size][size];
         for(int row = 0; row < size; row++){
             for(int col = 0; col < size; col++){
                 board[row][col] = Mark.BLANK;
             }
         }
    }

    /**
     * get the size of the board
     *
     * @return the size of the board
     */
    public int getSize (){
         return this.size;
    }

    /**
     * put mark in board[row][col]
     *
     * @param mark the mark to put in the board
     * @param row the row to put the mark in
     * @param col the col to put the mark in
     * @return true if succeded to put the mark in the board, false else.
     */
    public boolean putMark(Mark mark , int row , int col){
        if (mark == Mark.BLANK || row>=this.size || col>=this.size ||
                row<0 || col<0 || board[row][col] != Mark.BLANK){
             return false;
         }
         this.board[row][col] = mark;
         return true;
    }

    /**
     * get the mark that stored in board[row][col]
     *
     * @param row the row that contain the mark
     * @param col the col that contain the mark
     * @return the mark in board[row][col] if the row and col indexes is valid,else return blank (0)
     */
    public Mark getMark (int row , int col){
         if( row>=this.size || col>=this.size || row<0 || col<0 ){
             return Mark.BLANK;
         }
         return this.board[row][col];
    }
}
