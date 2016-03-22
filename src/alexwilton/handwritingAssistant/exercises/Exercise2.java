package alexwilton.handwritingAssistant.exercises;


import java.awt.*;

public class Exercise2 extends Exercise {
    private static String instruction = "Please copy the following sentence underneath:";
    private static String textToCopy = "The man sat down."; //"A beautiful aroma drifted out of the kitchen from the freshly baked cakes";

    public Exercise2() {
        super(instruction, textToCopy);
    }

    public void draw(Graphics2D g) {
        super.draw(g);
        g.setColor(Color.BLUE);

        g.setFont(new Font("Droid Sans", Font.PLAIN, 25));
        g.drawString(instruction, calculateXforCentringString(g, instruction), 100);

        g.setFont(new Font("Droid Sans", Font.PLAIN, 35));
        g.drawString(textToCopy, calculateXforCentringString(g, textToCopy), 150);

        drawLines(g);
        highlightWords(g);
    }
}
