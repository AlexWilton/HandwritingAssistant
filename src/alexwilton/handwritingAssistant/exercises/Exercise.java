package alexwilton.handwritingAssistant.exercises;

import alexwilton.handwritingAssistant.Canvas;

import java.awt.*;

public abstract class Exercise {
    private Canvas canvas;
    private String instruction;
    private String textToCopy;

    public Exercise(Canvas canvas, String instruction, String textToCopy) {
        this.canvas = canvas;
        this.instruction = instruction;
        this.textToCopy = textToCopy;
    }

    public void displayExercise(){
        canvas.setExercise(this); //

    }

    public void draw(Graphics2D g) {
        g.drawString(instruction, canvas.getWidth()/2, 100);
    }
}
