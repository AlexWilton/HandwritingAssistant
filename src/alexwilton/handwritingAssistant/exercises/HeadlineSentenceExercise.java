package alexwilton.handwritingAssistant.exercises;


import dhuckaby.NonsenseGenerator;

import java.awt.*;

public class HeadlineSentenceExercise extends Exercise {

    public HeadlineSentenceExercise() {
        instruction = "Please copy the following sentence underneath:";
        textToCopy = NonsenseGenerator.getInstance().makeHeadline();
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
