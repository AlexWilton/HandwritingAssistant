package alexwilton.handwritingAssistant.exercises;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RecapExercise extends Exercise {

    private List<Map.Entry<String, Integer>> orderedWordFails;

    public RecapExercise() {
        instruction = "For each word, please copy it out on the line to its right";
        textToCopy = "";
        caseSensitiveChecking = false;
    }

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

        g.setFont(new Font("Droid Sans", Font.PLAIN, 25));
        g.drawString(instruction, calculateXforCentringString(g, instruction), 100);
        g.setFont(new Font("Droid Sans", Font.PLAIN, 30));

         if(orderedWordFails == null) sortWordFails();

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
        highlightWords(g);
    }

    public boolean hasAnotherPage() {
        if(orderedWordFails == null) sortWordFails();
        return orderedWordFails.size() > 5;
    }

    public void moveToNextPage() {
        if(hasAnotherPage()){
                orderedWordFails = orderedWordFails.subList(5, orderedWordFails.size());
        }
    }

    public void resetRecap() {
        sortWordFails();
    }
}

