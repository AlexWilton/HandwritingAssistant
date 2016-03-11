package alexwilton.handwritingAssistant.exercises;

import alexwilton.handwritingAssistant.HandWritingAssistant;
import alexwilton.handwritingAssistant.Word;

import java.awt.*;
import java.util.Set;

public abstract class Exercise {
    private String instruction;
    private String textToCopy;
    private Set<Word> highlightedWords;

    public Exercise(String instruction, String textToCopy) {
        this.instruction = instruction;
        this.textToCopy = textToCopy;
    }


    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);

        g.setFont(new Font("Droid Sans", Font.PLAIN, 25));
        g.drawString(instruction, calculateXforCentringString(g, instruction), 100);

        g.setFont(new Font("Droid Sans", Font.PLAIN, 35));
        g.drawString(textToCopy, calculateXforCentringString(g, textToCopy), 150);

        drawLines(g);
        highlightWords(g);
    }


    private void highlightWords(Graphics2D g) {
        if(highlightedWords == null) return;
        for(Word word : highlightedWords){
            System.out.println("Highlighting: " + word.getText());
            g.setColor(new Color(231, 76, 60, 60));
            g.setStroke(new BasicStroke());
            g.fillRect(word.getX(), word.getY(), word.getWidth(), word.getHeight());
        }
    }

    private void drawLines(Graphics2D g) {
        g.setColor(Color.GRAY);
        int startX = 10, endX = HandWritingAssistant.getScreenDimension().width - startX;
        int lineSpacing = 120; int numOfLines = 6;
        int startingYOffset = 250;
        for(int lineNum=0; lineNum<numOfLines; lineNum++){
            int y = startingYOffset + lineNum*lineSpacing;
            g.drawLine(startX,y, endX,y);
        }
    }


    private static int calculateXforCentringString(Graphics2D g, String strToCentre){
        return HandWritingAssistant.getScreenDimension().width/2 - g.getFontMetrics().stringWidth(strToCentre)/2;
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
}
