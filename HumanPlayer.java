/**
 * HumanPlayer class represents a human player
 */
public class HumanPlayer implements Player{

    private final static String OCCUPIED_PLACE="Mark position is already occupied. " +
            "Please choose a valid position:";
    private final static String INVALID_MARK="Invalid mark position. Please choose a valid position:";
    private final static String PLACEHOLDER_FOR_REQUESTED_MARK="<mark>";
    private final static String TYPE_COORDINATE="Player"+PLACEHOLDER_FOR_REQUESTED_MARK+
            ",type coordinates: ";

    /**
     * Default constructor to human player
     */
    public HumanPlayer(){}

    /**
     * Plays a single turn for the human player
     *
     * @param board the board to put the mark in
     * @param mark the mark to put in the board
     */
    @Override
    public void playTurn (Board board, Mark mark){
        String typeOrder=TYPE_COORDINATE;
        System.out.println(typeOrder.replaceAll(PLACEHOLDER_FOR_REQUESTED_MARK,mark.toString()));
        while(true){
            int in=KeyboardInput.readInt();
            int col=in%10;//get the right number
            int row=in/10;////get the left number

            if(row<0 || row>= board.getSize() || col<0 || col>=board.getSize()){
                System.out.println(INVALID_MARK);
                continue;
            } else if (board.getMark(row,col)!=Mark.BLANK){
                System.out.println(OCCUPIED_PLACE);
                continue;
            }
            board.putMark(mark,row,col);
            break;
        }
    }
}
