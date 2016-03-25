package alexwilton.handwritingAssistant.exercises;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Recap Exercise gives the leaner the opportunity to reattempted writing words
 * which they failed to form correctly. Words for reattempted are ordered with
 * the words with the highest fail frequency first and lowest fail frequency last.
 */
public class RecapExercise extends Exercise {

    private List<Map.Entry<String, Integer>> orderedWordFails;

    public RecapExercise() {
        instruction = "For each word, please copy it out on the line to its right";
        textToCopy = "";
        caseSensitiveChecking = false;
    }


    /**
     * Sort Words with the highest fail frequency first and lowest fail frequency last.
     * Result is stored in orderedWordFails List.
     */
    private void sortWordFails() {
        orderedWordFails = new ArrayList<>(wordFormingFailCount.entrySet());
        Collections.sort(
            orderedWordFails,
            new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                    return Integer.compare(b.getValue(), a.getValue());
                }
            }
        );
    }


    public void draw(Graphics2D g) {
        super.draw(g);
        g.setColor(Color.BLUE);

        /* Initialise orderedWordFails if necessary */
        if(orderedWordFails == null) sortWordFails();

        /* If nothing to recap */
        if(orderedWordFails.size() == 0)
            instruction = "Well Done! No mistakes were made. Click 'Finish' to close the program";

        /* Write Instruction for leaner */
        g.setFont(new Font("Droid Sans", Font.PLAIN, 25));
        g.drawString(instruction, calculateXforCentringString(g, instruction), 100);
        g.setFont(new Font("Droid Sans", Font.PLAIN, 30));


        /* Write each word for learner to recap on a separate line. Maximum 5 words per page. */
        int startX = 20;
        int lineSpacing = 120; int numOfLines = 5;
        int startingYOffset = 240;
        for(int lineNum=0; lineNum<numOfLines && lineNum<orderedWordFails.size(); lineNum++){
            String word = orderedWordFails.get(lineNum).getKey();
            System.out.println(word + " (" + orderedWordFails.get(lineNum).getValue() + ")");
            textToCopy += (textToCopy.equals("") ? "" : " ") + word;
            int y = startingYOffset + lineNum*lineSpacing;
            g.drawString(word, startX, y);
        }

        drawLines(g);
        drawFeedback(g);
    }

    public boolean hasAnotherPage() {
        if(orderedWordFails == null) sortWordFails();
        return orderedWordFails.size() > 5;
    }

    public void moveToNextPage() {
        if(hasAnotherPage())
            orderedWordFails = orderedWordFails.subList(5, orderedWordFails.size());
    }

}

