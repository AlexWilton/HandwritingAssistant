package alexwilton.handwritingAssistant.exercises;

import alexwilton.handwritingAssistant.HandWritingAssistant;

import java.awt.*;

public abstract class Exercise {
    private String instruction;
    private String textToCopy;

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
}
