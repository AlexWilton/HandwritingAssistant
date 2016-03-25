package alexwilton.handwritingAssistant.exercises;


import dhuckaby.NonsenseGenerator;

import java.awt.*;

/**
 * Headline Sentence Exercise.
 * Exercise asks learner to hand-write a randomly generated nonsense headline sentence.
 */
public class HeadlineSentenceExercise extends Exercise {

    public HeadlineSentenceExercise() {
        instruction = "Please copy the following sentence underneath:";
        textToCopy = NonsenseGenerator.getInstance().makeHeadline();
    }

    /**
     * Draw Headline Exercise. Draws exercise type, instruction, lines and visual feedback for learner.
     * @param g Graphics which the exercise is drawn on.
     */
    public void draw(Graphics2D g) {
        super.draw(g);
        g.setColor(Color.BLUE);

        g.setFont(new Font("Droid Sans", Font.PLAIN, 25));
        g.drawString(instruction, calculateXforCentringString(g, instruction), 100);

        g.setFont(new Font("Droid Sans", Font.PLAIN, 35));
        g.drawString(textToCopy, calculateXforCentringString(g, textToCopy), 150);

        drawLines(g);
        drawFeedback(g);
    }
}
