package alexwilton.handwritingAssistant;


import alexwilton.handwritingAssistant.exercises.Exercise;

import com.myscript.cloud.sample.ws.api.Box;
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

        //extract words
        Stroke[] strokes = incomingStrokes.toArray(new Stroke[]{});
        HashMap<Stroke, Word> words = new HashMap<>();
        HashMap<Stroke, Integer> maxLengthStartingAtStroke = new HashMap<>();
        for(Pair<Stroke> key : strokeRangeToText.keySet()){
            String value = strokeRangeToText.get(key);
            int currentMaxLength = maxLengthStartingAtStroke.getOrDefault(key, 0);
            if(!value.contains(" ") && value.length() > currentMaxLength){
                maxLengthStartingAtStroke.put(key.getX(), value.length());
                Word word = extractWordFromStrokes(strokes, key, value);
                words.put(key.getX(), word);
            }
        }

        //highlight words
        exercise.setHighlightedWords(new HashSet<>(words.values()));
        canvas.repaint();
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
                public void handleMessage(String message) {
                    strokeRangeToText.put(new Pair<>(strokes[0], strokes[strokes.length-1]), message);
                    System.out.println(myScriptConnection.getTextOutputResult(message));
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
