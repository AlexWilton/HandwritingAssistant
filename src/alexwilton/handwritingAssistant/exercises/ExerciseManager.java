package alexwilton.handwritingAssistant.exercises;

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
//        exercises.add(new AlphabetExercise());
//        exercises.add(new HeadlineSentenceExercise());
//        exercises.add(new HeadlineSentenceExercise());
        exercises.add(new HeadlineSentenceExercise());
        exercises.add(new RecapExercise());
        exerciseManager.currentExerciseIndex = 0;
        exerciseManager.exercises = exercises;
        exerciseManager.wordFormingFailCount = new HashMap<>();
        exerciseManager.wordFormingFailCount.put("test", 2);
        exerciseManager.wordFormingFailCount.put("man", 1);
        exerciseManager.wordFormingFailCount.put("manager", 4);
        exerciseManager.wordFormingFailCount.put("lone", 2);
        exerciseManager.wordFormingFailCount.put("gone", 1);
        exerciseManager.wordFormingFailCount.put("unordered", 4);
        exerciseManager.wordFormingFailCount.put("sorted", 4);
        //track word fail count across all exercises together
        for(Exercise ex : exercises){ ex.setWordFormingFailCount(exerciseManager.wordFormingFailCount);}
        return exerciseManager;
    }



    public void moveToNextExercise(){
        currentExerciseIndex++;
        if(currentExerciseIndex >= exercises.size()) currentExerciseIndex = currentExerciseIndex % exercises.size();
        if(currentExerciseIndex == 0){
            wordFormingFailCount = new HashMap<>();
            for(Exercise ex : exercises){
                if(ex instanceof RecapExercise){
                    exercises.remove(ex);
                    RecapExercise newRecap = new RecapExercise();
                    newRecap.setWordFormingFailCount(wordFormingFailCount);
                    exercises.add(newRecap);
                    break;
                }
            }
        }
    }

    public Exercise getNextExecise(){
        moveToNextExercise();
        return getCurrentExercise();
    }

    public Exercise getCurrentExercise(){
        return exercises.get(currentExerciseIndex);
    }

    public int getCurrentExerciseNumber(){
        return currentExerciseIndex + 1;
    }

    public boolean isLastExercise(){
        return exercises.size() == currentExerciseIndex+1;
    }
}
