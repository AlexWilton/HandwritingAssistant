package alexwilton.handwritingAssistant;


import alexwilton.handwritingAssistant.exercises.Exercise;

import com.myscript.cloud.sample.ws.RecognitionListener;
import com.myscript.cloud.sample.ws.api.Stroke;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StrokeAnalyser implements RecognitionListener {
    private Exercise exercise;
    private MyScriptConnection myScriptConnection;
    private ConcurrentLinkedQueue<Stroke> incomingStrokes = new ConcurrentLinkedQueue<>();

    public StrokeAnalyser(Exercise exercise, MyScriptConnection myScriptConnection) {
        this.exercise = exercise;
        this.myScriptConnection = myScriptConnection;
        setupMessageHandler();
    }

    private void setupMessageHandler() {
        myScriptConnection.addMessageHandler(new MyScriptConnection.MessageHandler() {
            @Override
            public void handleMessage(String message) {
                System.out.println("The MEssage!!: " + message);
            }
        });
    }

    public void analyseStrokes(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Stroke[] strokeArray = incomingStrokes.toArray(new Stroke[]{});
                    myScriptConnection.recognizeStrokes(strokeArray);

                } catch (Exception e) { System.err.println("Couldn't recognise strokes. Exception: " + e.getClass().getSimpleName() + " Message: " + e.getMessage());}
            }
        }).run();
    }

    public void addStroke(Stroke stroke){
        incomingStrokes.add(stroke);
    }

    @Override
    public void recognitionResult(String recognized) {
        System.out.println( (recognized == null) ? "couldn't recognise stroke!!!" : recognized + "\n------------------\n");
    }
}
