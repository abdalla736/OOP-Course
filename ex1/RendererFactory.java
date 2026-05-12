/**
 * The RendererFactory class is responsible for creating Renderer instances.
 * It can return different types of renderers based on the given input or type.
 */
public class RendererFactory {
    private final static String VOID_RENDERER = "void";
    private final static String CONSOLE_RENDERER = "console";

    /**
     * constructs a renderer factory
     */
    public RendererFactory(){}

    /**
     * Builds and returns a Renderer instance based on the given type.
     *
     * @param type the type of renderer to create ("console", "void")
     * @param size the size of the board used by console renderer
     * @return a Renderer instance corresponding to the given type
     */
    public Renderer buildRenderer(String type , int size){
        switch(type){
            case VOID_RENDERER:
                return new VoidRenderer();
            case CONSOLE_RENDERER:
                return new ConsoleRenderer(size);
            default:
                return null;
        }
    }
}
