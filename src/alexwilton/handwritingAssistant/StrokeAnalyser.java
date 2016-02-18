package alexwilton.handwritingAssistant;


import alexwilton.handwritingAssistant.exercises.Exercise;

import com.myscript.cloud.sample.ws.api.Box;
import com.myscript.cloud.sample.ws.api.Stroke;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.awt.geom.Line2D;
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
//        List<Stroke> strokes = new LinkedList<>();
//        strokes.add(incomingStrokes.poll());
//        while(!incomingStrokes.isEmpty()){
//            /*Reset stroke list if new word found*/
//            if(strokes.size() > 0 && strokeRangeToText.getOrDefault(new Pair(strokes.get(0), strokes.get(strokes.size() - 1)), "").contains(" ")) {
//                strokes = new LinkedList<>(); continue;
//            }
//            strokes.add(incomingStrokes.poll());
//            int sizeBefore = strokeRangeToText.size();
//            analyseStroke(strokes.toArray(new Stroke[]{}));
//            while(strokeRangeToText.size() == sizeBefore){
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
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
        double minDistance = 5;
        //Group strokes together based on distance
        Stroke[] strokes = incomingStrokes.toArray(new Stroke[incomingStrokes.size()]);
        ArrayList<Set<Stroke>> strokeGroups = new ArrayList<>();
        for(Stroke stroke : strokes){
            Set<Stroke> closetStrokeGroup = null;
            double closetStrokeDist = 0;
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
            Stroke[] sArray = (Stroke[]) strokeSet.toArray(new Stroke[strokeSet.size()]);
            words.add(extractWordFromStrokes(sArray, new Pair<>(sArray[0], sArray[sArray.length - 1]), ""));
        }
        exercise.setHighlightedWords(words);
        canvas.repaint();
    }

    private double minDistanceBetweenStrokes(Stroke s1, Stroke s2){
        int[] s1center = s1.getCenterPt();
        int[] s2center = s2.getCenterPt();

        Line2D centerTocenterLine = new Line2D.Float(s1center[0], s1center[1], s2center[0], s2center[1]);
        Line2D.Float s1CloserSide = null;
        double minDistS1SidesToS2Center= Float.MAX_VALUE;
        for(Line2D.Float side : s1.getRectSides()){
            double distToS2 = side.ptLineDist(s2center[0], s2center[1]);
            if(distToS2 < minDistS1SidesToS2Center){
                minDistS1SidesToS2Center = distToS2;
                s1CloserSide = side;
            }
        }

        Line2D.Float s2CloserSide = null; double minDistS2SidesToS1Center= Float.MAX_VALUE;
        for(Line2D.Float side : s2.getRectSides()){
            double distToS1 = side.ptLineDist(s1center[0], s1center[1]);
            if(distToS1 < minDistS2SidesToS1Center){
                minDistS2SidesToS1Center = distToS1;
                s2CloserSide = side;
            }
        }

        assert s1CloserSide != null;
        assert s2CloserSide != null;
        double dist1 = s1CloserSide.ptLineDist(s2CloserSide.getP1());
        double dist2 = s1CloserSide.ptLineDist(s2CloserSide.getP2());
        return (dist1 > dist2) ? dist1 : dist2;
    }

    public void oldHighlightWords(){
        //extract words
        Stroke[] strokes = incomingStrokes.toArray(new Stroke[incomingStrokes.size()]);
        HashMap<Stroke, Word> words = new HashMap<>();
        HashMap<Stroke, Integer> maxLengthStartingAtStroke = new HashMap<>();
        for(Pair<Stroke> strokeKey : strokeRangeToText.keySet()){
            String text = strokeRangeToText.get(strokeKey);
            int currentMaxLength = maxLengthStartingAtStroke.getOrDefault(strokeKey, 0);
            if(!text.contains(" ") && text.length() > currentMaxLength){
                maxLengthStartingAtStroke.put(strokeKey.getX(), text.length());
                Word word = extractWordFromStrokes(strokes, strokeKey, text);
                removeWordsContaining(word, words);
                words.put(strokeKey.getX(), word);
            }
        }

        //highlight words
        exercise.setHighlightedWords(new HashSet<>(words.values()));
        canvas.repaint();
    }

    private void removeWordsContaining(Word newWord, HashMap<Stroke, Word> wordMap) {
        Set<Stroke> keysToRemove = new HashSet<>();
        for(Stroke key : wordMap.keySet()){
            Word word = wordMap.get(key);
            if(newWord.contains(word)) keysToRemove.add(key);
        }
        for(Stroke key : keysToRemove) wordMap.remove(key);
    }

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
}
