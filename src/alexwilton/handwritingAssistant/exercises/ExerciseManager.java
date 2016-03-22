package alexwilton.handwritingAssistant.exercises;

import java.util.ArrayList;
import java.util.List;

/**
 * Exercise Manager maintains information about every Exercise and is used to select current exercise.
 */
public class ExerciseManager {
    private ExerciseManager(){}

    private int currentExerciseIndex;
    private List<Exercise> exercises;

    public static ExerciseManager createDefault(){
        ExerciseManager exerciseManager = new ExerciseManager();
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise1());
        exercises.add(new Exercise2());
        exerciseManager.exercises = exercises;
        exerciseManager.currentExerciseIndex = 0;
        return exerciseManager;
    }

    public void moveToNextExercise(){
        currentExerciseIndex = (currentExerciseIndex + 1 % exercises.size());
        if(currentExerciseIndex >= exercises.size()) currentExerciseIndex = currentExerciseIndex % exercises.size();
    }

    public Exercise getNextExecise(){
        moveToNextExercise();
        return getCurrentExercise();
    }

    public Exercise getCurrentExercise(){
        return exercises.get(currentExerciseIndex);
    }
}
