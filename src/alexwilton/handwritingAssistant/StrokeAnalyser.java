package alexwilton.handwritingAssistant;

import com.myscript.cloud.sample.ws.api.*;
import com.myscript.cloud.sample.ws.api.Stroke;

import java.util.*;

/**
 * Stroke Analyser takes strokes from the canvas, divides them into stroke words (potential words) and analyses each
 * stroke group separately. The analysis results are then sent to the current exercise which determines how to
 * appropriately display feedback to the learner.
 */
public class StrokeAnalyser {
    private ExerciseManager exerciseManager;
    private MyScriptConnection myScriptConnection;
    private Canvas canvas;

    /**
     * Maximum allowed distance in pixels between strokes in order than a collection of
     * strokes is considered a stroke group for analysing as a word.
     */
    private int wordSeparatingDistance = 35;

    public StrokeAnalyser(ExerciseManager exerciseManager, MyScriptConnection myScriptConnection) {
        this.exerciseManager = exerciseManager;
        this.myScriptConnection = myScriptConnection;
    }

    /**
     * Strokes are extracted from the canvas. They are then divided them into stroke words (potential words) and each
     * stroke group are analysed separately. The analysis results are then sent to the current exercise which
     * determines how to appropriately display feedback to the learner.
     * @param recordFailedWordAttempts Should failed word attempts be recorded.
     */
    public void analyseStrokes(final boolean recordFailedWordAttempts){
        final List<Word> recognisedWords = new ArrayList<>();
        ArrayList<ArrayList<Stroke>> strokeSetList = divideStrokesIntoWords();
        for(ArrayList<Stroke> wordStrokes : strokeSetList){
            Stroke[] sArray = wordStrokes.toArray(new Stroke[wordStrokes.size()]);
            try {
                myScriptConnection.recognizeStrokes(sArray, new MyScriptConnection.MessageHandler() {
                    private Stroke[] strokesToAnalyse;
                    @Override
                    public void handleMessage(String text) {
                        System.out.println("Text: " + text);
                        recognisedWords.add(extractWordFromStrokes(strokesToAnalyse, text));
                        canvas.repaint();
                    }

                    private MyScriptConnection.MessageHandler init(Stroke[] strokeToAnalyse){
                        this.strokesToAnalyse = strokeToAnalyse;
                        return this;
                    }
                }.init(sArray));

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Couldn't recognise strokes. Exception: " + e.getClass().getSimpleName() + " Message: " + e.getMessage());
            }
        }
        if(strokeSetList.size() > 0) myScriptConnection.waitUntilFinished();
        exerciseManager.getCurrentExercise().generateFeedback(recognisedWords, StrokeAnalyser.this, recordFailedWordAttempts);
        canvas.repaint();
    }

    /**
     * Divide strokes into stroke groups. Any stroke which is less than the 'wordSeparatingDistance' to another stroke
     * are in the same stroke group. A stroke group is represented by a List of Strokes.
     * @return A List of Stroke groups.
     */
    private ArrayList<ArrayList<Stroke>> divideStrokesIntoWords(){
        Stroke[] strokes = canvas.getStrokes().toArray(new Stroke[canvas.getStrokes().size()]);
        ArrayList<ArrayList<Stroke>> strokeGroups = new ArrayList<>();
        for(Stroke stroke : strokes){
            ArrayList<Stroke> closetStrokeGroup = null;
            double closetStrokeDist = Double.MAX_VALUE;
            for(ArrayList<Stroke> sGroup : strokeGroups){
                for(Stroke s : sGroup){
                    double dist = minDistanceBetweenStrokes(stroke, s);
                    if(dist < closetStrokeDist){
                        closetStrokeDist = dist;
                        closetStrokeGroup = sGroup;
                    }
                }
            }
            if(closetStrokeDist > wordSeparatingDistance){
                ArrayList<Stroke> newGroup = new ArrayList<>();
                newGroup.add(stroke);
                strokeGroups.add(newGroup);
            }else{
                if(closetStrokeGroup == null){
                    closetStrokeGroup = new ArrayList<>();
                    strokeGroups.add(closetStrokeGroup);
                }
                closetStrokeGroup.add(stroke);
            }
        }
        return strokeGroups;
    }

    /**
     * Calculate minimum distance between two strokes.
     * @param s1 First Stroke.
     * @param s2 Second Stroke.
     * @return Minimum distance between the two strokes.
     */
    private double minDistanceBetweenStrokes(Stroke s1, Stroke s2){
        double minDistance  = Double.MAX_VALUE;
        for(Point pt1 : s1.getPoints()){
            for(Point pt2 : s2.getPoints()){
                double dist = Math.hypot(pt1.x - pt2.x, pt1.y - pt2.y);
                if(dist < minDistance) minDistance = dist;
            }
        }
        return minDistance;
    }

    /**
     * Construct a word object which comprises of its strokes and the recognised text from MyScript for the strokes.
     * @param strokes Strokes.
     * @param text Recognised text from MyScript for the strokes.
     * @return Word.
     */
    private Word extractWordFromStrokes(Stroke[] strokes, String text) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        for(Stroke stroke : strokes){
            Box box = stroke.getBoundingBox();
            if(box.x < minX) minX = box.x;
            if(box.y < minY) minY = box.y;
            if(box.x + box.width > maxX) maxX = box.x + box.width;
            if(box.y + box.height > maxY) maxY = box.y + box.height;
        }
        return new Word(minX, minY, maxX-minX, maxY-minY, text);
    }


    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public int getWordSeparatingDistance() {
        return wordSeparatingDistance;
    }

    public void setWordSeparatingDistance(int wordSeparatingDistance) {
        this.wordSeparatingDistance = wordSeparatingDistance;
    }
}
