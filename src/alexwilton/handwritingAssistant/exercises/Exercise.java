package alexwilton.handwritingAssistant.exercises;

import alexwilton.handwritingAssistant.DEALAssistant;
import alexwilton.handwritingAssistant.StrokeAnalyser;
import alexwilton.handwritingAssistant.Word;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Superclass for Exercise.
 * An Exercise provides a learner with an handwriting instruction along with
 * giving the learner feedback on how closely their handwritten strokes correspond with the expected text.
 */
public abstract class Exercise {
    /**
     * Instruction to be displayed to the leaner to inform them how to go about the task.
     */
    protected String instruction;

    /**
     * Expected text which the learner should be writing.
     */
    protected String textToCopy;

    /**
     * Words the exercises wants to be highlighted
     */
    private Set<Word> highlightedWords;

    /**
     * Map for keeping track of the number of times a learner fails to form each word.
     */
    protected Map<String, Integer> wordFormingFailCount;

    /**
     * Boolean for specifying whether a handwriting checking should be case sensitive.
     */
    protected boolean caseSensitiveChecking = true;

    /**
     * Should a "No Mistakes Detected!" message currently be being shown.
     */
    private boolean showNoMistakesMessage = false;

    /**
     * Constructor for Exercise to be overridden.
     */
    public Exercise() {}

    /**
     * Constructor for Exercise which sets learner instruction and expected handwriting as text.
     * @param instruction Instruction for learner,
     * @param textToCopy Expected handwriting as text.
     */
    public Exercise(String instruction, String textToCopy) {
        this.instruction = instruction;
        this.textToCopy = textToCopy;
    }

    /**
     * Draw Exercise on Canvas in the exercise area of the gui.
     * @param g Graphics which the exercise is drawn on.
     */
    public void draw(Graphics2D g){
        g.setFont(new Font("Droid Sans", Font.PLAIN, 25));
        //first regex adds space before numbers. second regex adds space before capital
        String exText = getClass().getSimpleName().replaceAll("(?<=\\D)(?=\\d)", " ").replaceAll("([A-Z])", " $1");
        g.drawString(exText, calculateXforCentringString(g, exText), 50);
    }

    /**
     * Draw feedback on graphics for learner.
     * Highlights mis-written words and states what should have been written
     * in blue and what was written underneath.
     * @param g Graphics to draw feedback on.
     */
    protected void drawFeedback(Graphics2D g) {
        if(highlightedWords == null) return;
        for(Word word : highlightedWords){
            g.setColor(new Color(231, 76, 60, 60));
            g.setStroke(new BasicStroke());
            g.fillRect(word.getX(), word.getY(), word.getWidth(), word.getHeight());
            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.setColor(Color.BLUE);
            g.drawString(word.getExpected(), word.getX(), word.getY()+word.getHeight()+20);
            g.setColor(Color.BLACK);
            g.drawString(word.getText(), word.getX(), word.getY()+word.getHeight()+40);
        }

        if(showNoMistakesMessage){
            String text = "No Mistakes Detected!";
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.PLAIN, 35));
            g.drawString(text, calculateXforCentringString(g,text), DEALAssistant.getScreenDimension().height-280);
        }
    }

    /**
     * Draw five horizontal lines to act as guide lines for the learner to write on.
     * @param g Graphics to draw on.
     */
    protected void drawLines(Graphics2D g) {
        g.setColor(Color.GRAY);
        int startX = 10, endX = DEALAssistant.getScreenDimension().width - startX;
        int lineSpacing = 120; int numOfLines = 5;
        int startingYOffset = 250;
        for(int lineNum=0; lineNum<numOfLines; lineNum++){
            int y = startingYOffset + lineNum*lineSpacing;
            g.drawLine(startX,y, endX,y);
        }
    }


    /**
     * Calculate x co-ordinate which would result in a string being centred.
     * @param g Graphics from which the font for rendering the string can be extracted
     * @param strToCentre String to be centred
     * @return X Co-ordinate which will result in string being centred
     */
    protected static int calculateXforCentringString(Graphics2D g, String strToCentre){
        return DEALAssistant.getScreenDimension().width/2 - g.getFontMetrics().stringWidth(strToCentre)/2;
    }


    /**
     * Generate visual feedback for learner.
     * Compare recognised words with expected words and highlight any differences.
     * @param recognisedWords Ordered list of recognised words (created from analysed user strokes)
     * @param strokeAnalyser Stroke Analyser (allows a reattempt of stroke analysis in cases where the word separating distance is too large)
     * @param recordFailedWordAttempts Boolean whether failed attempts are recorded.
     */
    public void generateFeedback(List<Word> recognisedWords, StrokeAnalyser strokeAnalyser, boolean recordFailedWordAttempts) {
        /* Highlight all non-expected words */
        String[] targetWords = textToCopy.split(" ");
        int targetCount = targetWords.length;
        highlightedWords = new HashSet<>();
        for(int i=0; i<recognisedWords.size() && i<targetCount; i++){
            String writtenWord = recognisedWords.get(i).getText().replaceAll("[^a-zA-Z]", "");
            String targetWord = targetWords[i].replaceAll("[^a-zA-Z]", ""); //remove non-letters from comparison.
            if(!caseSensitiveChecking){writtenWord = writtenWord.toLowerCase(); targetWord = targetWord.toLowerCase();}
            if(!targetWord.equals(writtenWord)){
                recognisedWords.get(i).setExpected(targetWord);
                highlightedWords.add(recognisedWords.get(i));
            }

            //check for two words grouped as one
            if(writtenWord.contains(" ")) {
                strokeAnalyser.setWordSeparatingDistance(strokeAnalyser.getWordSeparatingDistance() - 5);
                strokeAnalyser.analyseStrokes(recordFailedWordAttempts);
                highlightedWords = new HashSet<>();
                return;
            }
        }

        /* Give position affirmation to learner if no mistakes made */
        showNoMistakesMessage = highlightedWords.size() == 0;

        /* Record Failed Word Attempts (needed for Recap Exercise) */
        if(recordFailedWordAttempts){
            for(Word word : highlightedWords){
                String failedText = word.getExpected();
                int currentCount = wordFormingFailCount.getOrDefault(failedText, 0);
                wordFormingFailCount.put(failedText, currentCount + 1);
            }
        }
    }

    public void setWordFormingFailCount(Map<String, Integer> wordFormingFailCount) {
        this.wordFormingFailCount = wordFormingFailCount;
    }

    public void setHighlightedWords(Set<Word> highlightedWords) {
        this.highlightedWords = highlightedWords;
    }

    public boolean isShowNoMistakesMessage() {
        return showNoMistakesMessage;
    }

    public void setShowNoMistakesMessage(boolean showNoMistakesMessage) {
        this.showNoMistakesMessage = showNoMistakesMessage;
    }
}
