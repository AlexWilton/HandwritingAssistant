package alexwilton.handwritingAssistant;


import alexwilton.handwritingAssistant.exercises.Exercise;
import com.myscript.cloud.sample.ws.MyScriptCloud;
import com.myscript.cloud.sample.ws.RecognitionListener;
import com.myscript.cloud.sample.ws.api.Stroke;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StrokeAnalyser implements RecognitionListener {
    private Exercise exercise;
    private MyScriptCloud myScriptCloud;
//    private List<Stroke> strokes;
    private ConcurrentLinkedQueue incomingStrokes = new ConcurrentLinkedQueue();

    public StrokeAnalyser(Exercise exercise, MyScriptCloud myScriptCloud) {
        this.exercise = exercise;
        this.myScriptCloud = myScriptCloud;
    }

    public void analyseStrokes(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Stroke[] strokeArray = (Stroke[]) incomingStrokes.toArray(new Stroke[]{});
                    String recognized = myScriptCloud.recognize(strokeArray);
                    recognitionResult(recognized);
                } catch (IOException e) { System.err.println("Couldn't recognise strokes. IO error.");}
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
