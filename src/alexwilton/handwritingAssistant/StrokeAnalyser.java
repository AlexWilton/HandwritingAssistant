package alexwilton.handwritingAssistant;

import com.myscript.cloud.sample.ws.api.*;
import com.myscript.cloud.sample.ws.api.Stroke;

import java.util.*;

public class StrokeAnalyser {
    private ExerciseManager exerciseManager;
    private MyScriptConnection myScriptConnection;
    private List<Stroke> incomingStrokes = new LinkedList<>();
    private Canvas canvas;
    private int wordSeparatingDistance = 35;
    private List<Word> recognisedWords;

    public StrokeAnalyser(ExerciseManager exerciseManager, MyScriptConnection myScriptConnection) {
        this.exerciseManager = exerciseManager;
        this.myScriptConnection = myScriptConnection;
    }

    public void analyseStrokes(final boolean recordFailedWordAttempts){
        recognisedWords = new ArrayList<>();
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

    private ArrayList<ArrayList<Stroke>> divideStrokesIntoWords(){
        Stroke[] strokes = canvas.getStrokes().toArray(new Stroke[incomingStrokes.size()]);
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

    public void clearStroke(){
        incomingStrokes = new LinkedList<>();
        recognisedWords = new ArrayList<>();
    }

    public int getWordSeparatingDistance() {
        return wordSeparatingDistance;
    }

    public void setWordSeparatingDistance(int wordSeparatingDistance) {
        this.wordSeparatingDistance = wordSeparatingDistance;
    }
}
