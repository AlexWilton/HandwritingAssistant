package alexwilton.handwritingAssistant;


import alexwilton.handwritingAssistant.exercises.Exercise;

import com.myscript.cloud.sample.ws.api.*;
import com.myscript.cloud.sample.ws.api.Stroke;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StrokeAnalyser {
    private Exercise exercise;
    private MyScriptConnection myScriptConnection;
    private ConcurrentLinkedQueue<Stroke> incomingStrokes = new ConcurrentLinkedQueue<>();
    private Canvas canvas;

    public StrokeAnalyser(Exercise exercise, MyScriptConnection myScriptConnection) {
        this.exercise = exercise;
        this.myScriptConnection = myScriptConnection;
    }

    public void addStroke(Stroke stroke){
        incomingStrokes.add(stroke);
    }


    public void analyseStrokes(){
        Stroke[] strokes = incomingStrokes.toArray(new Stroke[]{});
        for(int startIndex=0; startIndex<strokes.length; startIndex++){
            for(int endIndex=startIndex; endIndex<strokes.length; endIndex++){
                int numOfStrokes = endIndex - startIndex + 1;
                Stroke[] strokesToAnalyse = new Stroke[numOfStrokes];
                System.arraycopy(strokes, startIndex, strokesToAnalyse, 0, numOfStrokes);
                analyseStroke(strokesToAnalyse);
            }
        }
    }


    public void highlightWords(){
        double minDistance = 30;
        //Group strokes together based on distance
        Stroke[] strokes = incomingStrokes.toArray(new Stroke[incomingStrokes.size()]);
        ArrayList<Set<Stroke>> strokeGroups = new ArrayList<>();
        for(Stroke stroke : strokes){
            Set<Stroke> closetStrokeGroup = null;
            double closetStrokeDist = Double.MAX_VALUE;
            for(Set<Stroke> sGroup : strokeGroups){
                for(Stroke s : sGroup){
                    double dist = minDistanceBetweenStrokes(stroke, s);
                    if(dist < closetStrokeDist){
                        closetStrokeDist = dist;
                        closetStrokeGroup = sGroup;
                    }
                }
            }
            if(closetStrokeDist > minDistance){
                Set<Stroke> newGroup = new HashSet<>();
                newGroup.add(stroke);
                strokeGroups.add(newGroup);
            }else{
                if(closetStrokeGroup == null){
                    closetStrokeGroup = new HashSet<Stroke>();
                    strokeGroups.add(closetStrokeGroup);
                }
                closetStrokeGroup.add(stroke);
            }
        }

        HashSet<Word> words = new HashSet<>();
        for(Set<Stroke> strokeSet : strokeGroups){
            Stroke[] sArray = strokeSet.toArray(new Stroke[strokeSet.size()]);
            words.add(extractWordFromStrokes(sArray, new Pair<>(sArray[0], sArray[sArray.length - 1]), ""));
        }
        exercise.setHighlightedWords(words);
        canvas.repaint();
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

//
//    private void removeWordsContaining(Word newWord, HashMap<Stroke, Word> wordMap) {
//        Set<Stroke> keysToRemove = new HashSet<>();
//        for(Stroke key : wordMap.keySet()){
//            Word word = wordMap.get(key);
//            if(newWord.contains(word)) keysToRemove.add(key);
//        }
//        for(Stroke key : keysToRemove) wordMap.remove(key);
//    }

    private Word extractWordFromStrokes(Stroke[] strokes, Pair<Stroke> strokeBounds, String text) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        boolean startStrokeFound = false;
        for(Stroke stroke : strokes){
            //first iterate through to first stroke of word
            if(startStrokeFound || stroke == strokeBounds.getX())
                startStrokeFound = true;
            else
                continue;

            Box box = stroke.getBoundingBox();
            if(box.x < minX) minX = box.x;
            if(box.y < minY) minY = box.y;
            if(box.x + box.width > maxX) maxX = box.x + box.width;
            if(box.y + box.height > maxY) maxY = box.y + box.height;

            //check for end stroke of word
            if(stroke == strokeBounds.getY()) break;
        }

        return new Word(minX, minY, maxX-minX, maxY-minY, text);
    }

    Map<Pair<Stroke>, String> strokeRangeToText = new HashMap<>();
    private void analyseStroke(final Stroke[] strokes){
        try {
            myScriptConnection.recognizeStrokes(strokes, new MyScriptConnection.MessageHandler() {
                @Override
                public void handleMessage(String json) {
                    String text = myScriptConnection.getTextOutputResult(json);
                    System.out.println("Text Rec: " + text);
                    strokeRangeToText.put(new Pair<>(strokes[0], strokes[strokes.length-1]), text);
                }
            });

        } catch (Exception e) {
            System.err.println("Couldn't recognise strokes. Exception: " + e.getClass().getSimpleName() + " Message: " + e.getMessage());
        }
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void clearStroke(){
        incomingStrokes = new ConcurrentLinkedQueue<>();
    }
}
