/**
 * The Tournament class represents one tournament
 */
public class Tournament {

    private final static int TWO_PLAYERS=2;
    private final static int INDEX_OF_FIRST_PLAYER=0;
    private final static int INDEX_OF_SECOND_PLAYER=1;
    private final static int FIRST_ARG=0;
    private final static int SECOND_ARG=1;
    private final static int THIRD_ARG=2;
    private final static int FORTH_ARG=3;
    private final static int FIFTH_ARG=4;
    private final static int SIXTH_ARG=5;


    private Player players[]=new Player[TWO_PLAYERS];
    private Renderer renderer;
    private int rounds;

    /**
     * Constructs a Tournament object with the given parameters.
     * A tournament runs multiple rounds between two players and renders
     * the board using the provided Renderer.
     *
     * @param rounds the number of rounds to play in the tournament
     * @param renderer the renderer responsible for displaying the board
     * @param player1 the first player participating in the tournament
     * @param player2 the second player participating in the tournament
     */
    public Tournament(int rounds, Renderer renderer , Player player1 ,Player player2) {
        this.rounds = rounds;
        this.renderer = renderer;
        this.players[INDEX_OF_FIRST_PLAYER] = player1;
        this.players[INDEX_OF_SECOND_PLAYER] = player2;
    }

    /**
     * Plays a full tournament between two players for a given number of rounds.
     * Each round creates a new game with the specified board size and win streak length.
     * The tournament keeps track of wins, losses, and ties, and may display the results
     * using the provided renderer.
     *
     * @param size the size of the board
     * @param winStreak the number of consecutive marks needed to win
     * @param playerName1 the name of the first player
     * @param playerName2 the name of the second player
     */
    public void playTournament(int size, int winStreak , String playerName1 , String playerName2) {
        int player1wins=0;
        int player2wins=0;
        int ties=0;
        for(int i=0;i<rounds;i++){
            int indexRound=i%2;
            Game game=new Game(this.players[indexRound],this.players[1-indexRound],
                    size,winStreak,this.renderer );
            Mark markWinner=game.run();

            if(markWinner==Mark.X){
                if(indexRound==0){
                    player1wins++;
                } else{
                    player2wins++;
                  }
            } else if(markWinner==Mark.O){
                if(indexRound==0){
                    player2wins++;
                } else{
                    player1wins++;
                  }
              } else{
                ties++;
              }
        }
        System.out.println("######### Results #########");
        System.out.println("Player 1, "+playerName1+" won: "+player1wins+" rounds");
        System.out.println("Player 2, "+playerName2+" won: "+player2wins+" rounds");
        System.out.println("Ties: "+ties);
    }

    /**
     * The entry point of the program.
     * Initializes the tournament and starts the gameplay.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        int rounds=Integer.parseInt(args[FIRST_ARG]);
        int size=Integer.parseInt(args[SECOND_ARG]);
        int  winStreak=Integer.parseInt(args[THIRD_ARG]);
        String rendererName=args[FORTH_ARG];
        String playerName1=args[FIFTH_ARG];
        String PlayerName2=args[SIXTH_ARG];

        PlayerFactory playerFactory=new PlayerFactory();
        RendererFactory rendererFactory=new RendererFactory();

        Player player1=playerFactory.buildPlayer(playerName1);
        Player player2=playerFactory.buildPlayer(PlayerName2);
        Renderer renderer=rendererFactory.buildRenderer(rendererName,size);
        Tournament tournament=new Tournament(rounds,renderer,player1,player2);
        tournament.playTournament(size,winStreak,playerName1,PlayerName2);

    }
}
