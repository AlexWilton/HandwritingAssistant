package alexwilton.handwritingAssistant;

public class Word {
    private int x,y,width,height;
    private String text;
    private static final int CONTAINS_FUDGE_FACTOR = 5;
    private String expected;

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

    /**
     * Check whether word contains another word by checking 4 corners
     * @param word
     * @return
     */
    public boolean contains(Word word) {
        int[][] pts = {{word.x, word.y}, {word.x, word.y+height}, {word.x+width, word.y}, {word.x+width, word.y+height}};
        for(int[] pt : pts) {
            if (contains(pt[0], pt[1])) return true;
        }
        return false;
    }

    public boolean contains(int ptX, int ptY){
        if(!(x < ptX+CONTAINS_FUDGE_FACTOR && ptX-CONTAINS_FUDGE_FACTOR < x + width )) return false;
        if(!(y < ptY+CONTAINS_FUDGE_FACTOR &&  ptY-CONTAINS_FUDGE_FACTOR < y + height )) return false;
        return true;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getExpected() {
        return expected;
    }
}
