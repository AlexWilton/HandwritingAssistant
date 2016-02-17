package alexwilton.handwritingAssistant;


import alexwilton.handwritingAssistant.exercises.Exercise;

import com.myscript.cloud.sample.ws.RecognitionListener;
import com.myscript.cloud.sample.ws.api.Stroke;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StrokeAnalyser {
    private Exercise exercise;
    private MyScriptConnection myScriptConnection;
    private ConcurrentLinkedQueue<Stroke> incomingStrokes = new ConcurrentLinkedQueue<>();

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
//            if(strokes.size() > 0 && strokeRangeToText.getOrDefault(new Key(strokes.get(0), strokes.get(strokes.size() - 1)), "").contains(" ")) {
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


    public void debug(){
        int i;
    }

    Map<Key<Stroke>, String> strokeRangeToText = new HashMap<>();
    private void analyseStroke(final Stroke[] strokes){
        try {
            myScriptConnection.recognizeStrokes(strokes, new MyScriptConnection.MessageHandler() {
                @Override
                public void handleMessage(String message) {
                    strokeRangeToText.put(new Key(strokes[0], strokes[strokes.length-1]), message);
                    System.out.println(myScriptConnection.getTextOutputResult(message));
                }
            });

        } catch (Exception e) {
            System.err.println("Couldn't recognise strokes. Exception: " + e.getClass().getSimpleName() + " Message: " + e.getMessage());
        }
    }

}
