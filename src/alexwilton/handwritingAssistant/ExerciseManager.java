package alexwilton.handwritingAssistant;

import alexwilton.handwritingAssistant.exercises.AlphabetExercise;
import alexwilton.handwritingAssistant.exercises.Exercise;
import alexwilton.handwritingAssistant.exercises.HeadlineSentenceExercise;
import alexwilton.handwritingAssistant.exercises.RecapExercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exercise Manager maintains information about every Exercise and is used to select current exercise.
 */
public class ExerciseManager {
    private ExerciseManager(){}

    private int currentExerciseIndex;
    private List<Exercise> exercises;
    private Map<String, Integer> wordFormingFailCount;

    /**
     * Create a default Exercise Manager. Consists of 1 Alphabet Exercise and 3 Headline Sentence Exercises
     * @return Default Exercise Manager.
     */
    public static ExerciseManager createDefault(){
        ExerciseManager exerciseManager = new ExerciseManager();
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new AlphabetExercise());
        exercises.add(new HeadlineSentenceExercise());
        exercises.add(new HeadlineSentenceExercise());
        exercises.add(new HeadlineSentenceExercise());
        exercises.add(new RecapExercise());
        exerciseManager.currentExerciseIndex = 0;
        exerciseManager.exercises = exercises;
        exerciseManager.wordFormingFailCount = new HashMap<>();
        //track word fail count across all exercises together
        for(Exercise ex : exercises){ ex.setWordFormingFailCount(exerciseManager.wordFormingFailCount);}
        return exerciseManager;
    }

    /**
     * Change current exercise to the next exercise.
     * If last exercise, make no change.
     */
    public void moveToNextExercise(){
        if(!isLastExercise()) currentExerciseIndex++;
    }

    /**
     * Move to next exercise.
     * @return New Current Exercise.
     */
    public Exercise getNextExecise(){
        moveToNextExercise();
        return getCurrentExercise();
    }

    /**
     * Returns current exercise
     * @return Current Exercise.
     */
    public Exercise getCurrentExercise(){
        return exercises.get(currentExerciseIndex);
    }

    /**
     * Is Last Exercise?
     * @return Whether current exercise is the last exercise.
     */
    public boolean isLastExercise(){
        return exercises.size() == currentExerciseIndex+1;
    }
}
