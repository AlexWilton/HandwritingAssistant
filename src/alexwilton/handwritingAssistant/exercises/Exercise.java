package alexwilton.handwritingAssistant.exercises;

import alexwilton.handwritingAssistant.DEALAssistant;
import alexwilton.handwritingAssistant.StrokeAnalyser;
import alexwilton.handwritingAssistant.Word;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class Exercise {
    protected String instruction;
    protected String textToCopy;
    private Set<Word> highlightedWords;

    protected boolean caseSensitiveChecking = true;

    public Exercise(String instruction, String textToCopy) {
        this.instruction = instruction;
        this.textToCopy = textToCopy;
    }

    public Exercise() {}


    public void draw(Graphics2D g){
        g.setFont(new Font("Droid Sans", Font.PLAIN, 25));
        String exText = getClass().getSimpleName().replaceAll("(?<=\\D)(?=\\d)", " ");
        g.drawString(exText, calculateXforCentringString(g, exText), 50);
    }

    protected void highlightWords(Graphics2D g) {
        if(highlightedWords == null) return;
        for(Word word : highlightedWords){
            System.out.println("Highlighting: " + word.getText());
            g.setColor(new Color(231, 76, 60, 60));
            g.setStroke(new BasicStroke());
            g.fillRect(word.getX(), word.getY(), word.getWidth(), word.getHeight());
            g.setFont(new Font("Arial", Font.PLAIN, 25));
            g.setColor(Color.BLUE);
            g.drawString(word.getExpected(), word.getX(), word.getY()+word.getHeight()+20);
            g.setColor(Color.BLACK);
            g.drawString(word.getText(), word.getX(), word.getY()+word.getHeight()+40);
        }
    }

    protected void drawLines(Graphics2D g) {
        g.setColor(Color.GRAY);
        int startX = 10, endX = DEALAssistant.getScreenDimension().width - startX;
        int lineSpacing = 120; int numOfLines = 6;
        int startingYOffset = 250;
        for(int lineNum=0; lineNum<numOfLines; lineNum++){
            int y = startingYOffset + lineNum*lineSpacing;
            g.drawLine(startX,y, endX,y);
        }
    }


    protected static int calculateXforCentringString(Graphics2D g, String strToCentre){
        return DEALAssistant.getScreenDimension().width/2 - g.getFontMetrics().stringWidth(strToCentre)/2;
    }


    public Set<Word> getHighlightedWords() {
        return highlightedWords;
    }

    public void setHighlightedWords(Set<Word> highlightedWords) {
        this.highlightedWords = highlightedWords;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getTextToCopy() {
        return textToCopy;
    }

    /**
     * Use ordered list of recognised words to generate visual feedback
     * @param recognisedWords
     */
    public void generateFeedback(List<Word> recognisedWords, StrokeAnalyser strokeAnalyser) {
        /*Highlight all non-expected words*/
        String[] targetWords = textToCopy.split(" ");
        int targetCount = targetWords.length;
        HashSet<Word> wordsToHighLight = new HashSet<>();
        for(int i=0; i<recognisedWords.size() && i<targetCount; i++){
            String writtenWord = recognisedWords.get(i).getText().replaceAll("[^a-zA-Z]", "");
            String targetWord = targetWords[i].replaceAll("[^a-zA-Z]", ""); //remove not letters from comparison.
            if(!caseSensitiveChecking){writtenWord = writtenWord.toLowerCase(); targetWord = targetWord.toLowerCase();}
            if(!targetWord.equals(writtenWord)){
                recognisedWords.get(i).setExpected(targetWord);
                wordsToHighLight.add(recognisedWords.get(i));
            }

            //check for two words grouped as one
            if(writtenWord.contains(" ")) {
                strokeAnalyser.setWordSeparatingDistance(strokeAnalyser.getWordSeparatingDistance() - 5);
                strokeAnalyser.analyseStrokes();
                highlightedWords = new HashSet<>();
                return;
            }

        }
        highlightedWords = wordsToHighLight;
    }
}
