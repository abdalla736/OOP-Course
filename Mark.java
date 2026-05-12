/**
 * Enum represent one mark
 */
public enum Mark {
    BLANK , X , O ;

    /**
     * returns a string representation to the mark
     * @return a string representation to the mark.
     */
    public String toString(){
        switch(this){
            case X :
                return "X";
            case O :
                return "O";
            case BLANK :
            default:
                return null;
        }
    }
}
