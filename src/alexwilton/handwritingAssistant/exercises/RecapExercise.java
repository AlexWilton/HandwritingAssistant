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

    /**
     * Maximum number of words to show on a recap page.
     */
    private static int MAXIMUM_NUM_WORDS_PER_PAGE = 5;

    /**
     * Order List of Words which the learner failed to write in previous exercises.
     * Entries are sorted most frequent to least frequent
     */
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


    /**
     * Draw Recap Exercise. Draws exercise type, instruction, lines, words to copy and visual feedback for learner.
     * @param g Graphics which the exercise is drawn on.
     */
    @Override
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
            textToCopy += (textToCopy.equals("") ? "" : " ") + word;
            int y = startingYOffset + lineNum*lineSpacing;
            g.drawString(word, startX, y);
        }

        drawLines(g);
        drawFeedback(g);
    }

    /**
     * Are there more words to show than are currently being shown on displayed recap page.
     * @return True if another page of words exists, else false.
     */
    public boolean hasAnotherPage() {
        if(orderedWordFails == null) sortWordFails();
        return orderedWordFails.size() > MAXIMUM_NUM_WORDS_PER_PAGE;
    }

    /**
     * Move to the next page of words. (if another page exists)
     */
    public void moveToNextPage() {
        if(hasAnotherPage())
            orderedWordFails = orderedWordFails.subList(MAXIMUM_NUM_WORDS_PER_PAGE, orderedWordFails.size());
    }

}

