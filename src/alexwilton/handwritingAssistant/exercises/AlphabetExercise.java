package alexwilton.handwritingAssistant.exercises;


import alexwilton.handwritingAssistant.DEALAssistant;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AlphabetExercise extends Exercise {
    private static String instruction = "Please trace the letters underneath:";
    private static String targetText = "Aa Bb Cc Dd Ee Ff Gg Hh Ii Jj Kk Ll Mm Nn Oo Pp Qq Rr Ss Tt Uu Vv Ww Xx Yy Zz"; //"A beautiful aroma drifted out of the kitchen from the freshly baked cakes";
    private BufferedImage alphabetImg;

    public AlphabetExercise() {
        super(instruction, targetText);
        setup();
    }

    private void setup() {
        try {
            alphabetImg = ImageIO.read(new File("images/alphabet.png"));
        } catch (IOException e) {
            System.err.println("Error! Failed to load Alphabet image.");
        }

    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        g.setColor(Color.BLUE);

        g.setFont(new Font("Droid Sans", Font.PLAIN, 25));
        g.drawString(instruction, calculateXforCentringString(g, instruction), 100);

        g.drawImage(alphabetImg, DEALAssistant.getScreenDimension().width/2-alphabetImg.getWidth()/2 , 250, alphabetImg.getWidth(), alphabetImg.getHeight(), null);

        drawFeedback(g);
    }
}
