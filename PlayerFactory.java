/**
 * The PlayerFactory class is responsible for creating Player instances.
 * It can return different types of players based on the given input or type.
 */
public class PlayerFactory {
    private final static String HUMAN="human";
    private final static String SMART="smart";
    private final static String NAIVE="naive";
    private final static String WHATEVER="whatever";

    /**
     * constructs the factory
     */
    public PlayerFactory(){}

    /**
     * Builds and returns a Player instance based on the given type.
     *
     * @param type the type of player to create ("human", "naive", "whatever", "smart")
     * @return a Player instance corresponding to the given type
     */
    public Player buildPlayer(String type){
        switch (type){
            case HUMAN:
                return new HumanPlayer();
            case SMART:
                return new SmartPlayer();
            case NAIVE:
                return new NaivePlayer();
            case WHATEVER:
                return new WhateverPlayer();
            default:
                return null;
        }
    }
}
