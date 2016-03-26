package alexwilton.handwritingAssistant;

/**
 * A Word object comprises of the area occupied by its strokes and the recognised text from MyScript for the strokes.
 * The expected text for a word can be added (but is optional).
 */
public class Word {

    /**
     * X co-ordinate of top-left corner.
     */
    private int x;

    /**
     * Y co-ordinate of top-left corner.
     */
    private int y;

    /**
     * Width of rectangle.
     */
    private int width;

    /**
     * Height of Rectangle.
     */
    private int height;

    /**
     * Recognised text from MyScript for the strokes.
     */
    private String text;

    /**
     * The expected text for a word can be added.
     * Can be null as optional usage.
     */
    private String expected;

    /**
     * Construct a Word object from the rectangle dimensions and text.
     * @param x x co-ordinate of top-left corner.
     * @param y y co-ordinate of top-left corner.
     * @param width width of rectangle.
     * @param height height of rectangle.
     * @param text recognised text from MyScript for the strokes.
     */
    public Word(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getText() {
        return text;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getExpected() {
        return expected;
    }
}
