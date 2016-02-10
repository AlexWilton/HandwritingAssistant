package alexwilton.handwritingAssistant.exercises;


import alexwilton.handwritingAssistant.Canvas;

public class Exercise1 extends Exercise {
    private static String instruction = "Please copy the following sentence underneath:";
    private static String textToCopy = "A beautiful aroma drifted out of the kitchen from the freshly baked cakes";

    public Exercise1() {
        super(instruction, textToCopy);
    }
}
