/**
 * The Game class represents one game
 */
public class Game {

    private final static int TWO_PLAYERS=2;
    private final static int INDEX_OF_FIRST_PLAYER_X=0;
    private final static int INDEX_OF_SECOND_PLAYER_O=1;
    private final static int DEFAULT_WIN_STREAK=3;
    
    //members of the class game
    private Board board;
    private Player[] players=new Player[TWO_PLAYERS];
    private Renderer renderer;
    private int winStreak;

    /**
     * construct the game by initialize the given players and renderer,
     * and sit win streak to the default value (3) also initialize the board to default value.
     *
     * @param playerX the first player X
     * @param playerO the second player O
     * @param renderer the renderer of the current game
     */
    public Game (Player playerX , Player playerO , Renderer renderer){
        this.players[INDEX_OF_FIRST_PLAYER_X] = playerX;
        this.players[INDEX_OF_SECOND_PLAYER_O] = playerO;
        this.renderer = renderer;
        this.winStreak = DEFAULT_WIN_STREAK;
        board = new Board();

    }

    /**
     * construct the game by initialize the given players, renderer,
     * size and win streak of the current game
     *
     * @param playerX the first player X
     * @param playerO the second player O
     * @param size the size of the current board in the game
     * @param winStreak the win streak of the current game
     * @param renderer the renderer of the current game
     */
    public Game (Player playerX , Player playerO , int size , int winStreak , Renderer renderer){
        this.players[INDEX_OF_FIRST_PLAYER_X] = playerX;
        this.players[INDEX_OF_SECOND_PLAYER_O] = playerO;
        this.renderer = renderer;
        this.winStreak = winStreak;
        board = new Board(size);

    }

    /**
     * get the current win streak
     * @return the current win streak
     */
    int getWinStreak(){
        return winStreak;
    }

    /**
     * get the current board size
     * @return the current board size
     */
    int getBoardSize(){
        return board.getSize();
    }

    /**
     * run the game
     * @return the mark of the game winner
     */
    public Mark run(){
        Mark currentMark;
        int numOfIteration=0;
        renderer.renderBoard(board);
        while(numOfIteration<=(board.getSize()*board.getSize())-1) {
            int numberPlayer = numOfIteration % players.length;
            Player currentPlayer = players[numberPlayer];

            //if we are in even turn then the mark is X, else O
            currentMark = (numOfIteration % 2 == 0) ? Mark.X : Mark.O;
            currentPlayer.playTurn(board, currentMark);
            renderer.renderBoard(board);

            if (playerWin(currentMark)) {
                return currentMark;
            }
            numOfIteration++;
        }
        return Mark.BLANK;
    }

    private boolean playerWin( Mark m){
        int size=board.getSize();

        for(int row=0;row<size;row++){
            for(int col=0;col<size;col++){
                if(checkWin(row,col,1,0,m) || checkWin(row,col,0,-1,m)||
                checkWin(row,col,-1,-1,m) || checkWin(row,col,1,-1,m)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkWin(int row, int col, int directionRow, int directionCol, Mark m){
        int size=board.getSize();
        int streak=this.winStreak;
        for(int i=0;i<streak;i++){
            int r=row+directionRow*i;
            int c=col+directionCol*i;

            if(r>=size||c>=size||r<0||c<0){
                return false;
            }
            if(board.getMark(r,c)!=m){
                return false;
            }

        }
        return true;

    }
}
