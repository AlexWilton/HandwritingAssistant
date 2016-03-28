package alexwilton.handwritingAssistant;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import alexwilton.handwritingAssistant.exercises.Exercise;
import alexwilton.handwritingAssistant.exercises.RecapExercise;

/**
 * DEAL Assistant aids learners in their handwriting by providing a series of exercises.
 * A java GUI records handwriting strokes, the strokes are analysed then visual feedback is
 * provided to the learner to aid their learning.
 */
public class DEALAssistant extends JFrame{

    /**
     * Application Key required for connecting to MyScript Cloud.
     */
	private static final String APPLICATION_KEY = "22eda92c-10af-40d8-abea-fd4093c17d81";

    /**
     * URL where MyScript Cloud service is located.
     */
	private static final String RECOGNITION_CLOUD_URL = "ws://cloud.myscript.com/api/v3.0/recognition/ws/text";

    /**
     * HMAC Key for hashing authentication challenges received from MyScript.
     */
	private static final String HMAC_KEY = "a1fa759f-b3ce-4091-9fd4-d34bb870c601";

    private ExerciseManager exerciseManager;
    private MyScriptConnection myScriptConnection;
	private Canvas canvas;
    private StrokeAnalyser strokeAnalyser;

	public DEALAssistant() {
		super("Hand Writing Assistant");
		setup();
	}

    /**
     * Setup System.
     * GUI and core application component initialisation.
     */
	private void setup(){
        /* Main GUI container setup */
		Dimension screenSize = getScreenDimension();
		setSize(screenSize.width, screenSize.height);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

        /* Core Application components initialisation */
        exerciseManager = ExerciseManager.createDefault();
        myScriptConnection = new MyScriptConnection(APPLICATION_KEY, HMAC_KEY, RECOGNITION_CLOUD_URL);
        strokeAnalyser = new StrokeAnalyser(exerciseManager, myScriptConnection);
        canvas = new Canvas();
        canvas.setExerciseManager(exerciseManager);
        canvas.setShowStrokes(true);
        strokeAnalyser.setCanvas(canvas);

        /* Attach canvas (displays exercises+feedback and captures strokes) and add buttons */
		add(canvas, BorderLayout.CENTER);
        addButtons();
	}

    /**
     * Add buttons to GUI.
     * Add three buttons: "Check For Mistakes", "Next Exercise" and "Reset Exercise".
     */
    private void addButtons() {
        Font btnFont = new Font("Arial", Font.PLAIN, 40);
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(1,3));
        JButton analyseBtn = new JButton("Check For Mistakes"); analyseBtn.setFont(btnFont);
        analyseBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                strokeAnalyser.analyseStrokes(false);
            }
        });
        btnPanel.add(analyseBtn);
        final JButton nextExBtn = new JButton("Next Exercise"); nextExBtn.setFont(btnFont);
        nextExBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(nextExBtn.contains(e.getPoint())) nextClick(nextExBtn);
            }
        });
        btnPanel.add(nextExBtn);
        JButton clearBtn = new JButton("Reset Exercise"); clearBtn.setFont(btnFont);
        clearBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                resetExercise();
            }
        });
        btnPanel.add(clearBtn);

        /* Place button panel south of canvas along with some bottom padding. */
        JPanel mainVerticalPanel = new JPanel();
        mainVerticalPanel.setLayout(new BoxLayout(mainVerticalPanel, BoxLayout.Y_AXIS));
        mainVerticalPanel.add(btnPanel);
        JPanel whiteBottomPadding = new JPanel();
        whiteBottomPadding.setBackground(Color.WHITE); whiteBottomPadding.setBorder(BorderFactory.createEmptyBorder(0,0,100,0));
        mainVerticalPanel.add(whiteBottomPadding);
        add(mainVerticalPanel, BorderLayout.SOUTH);
    }

    /**
     * Method defines functionailty to be performed when "Next Exercise Button" is clicked.
     * (Note, button label changes depending on what's coming next)
     * @param nextExBtn Next Button
     */
    private void nextClick(JButton nextExBtn){
        /* Treat Recap Exercise differently */
        Exercise currentExercise = exerciseManager.getCurrentExercise();
        if(currentExercise instanceof RecapExercise){
            //move to next page on recap exercise
            RecapExercise ex = (RecapExercise) currentExercise;
            if(ex.hasAnotherPage()) {
                ex.moveToNextPage();
                resetExercise();
                return;
            }
        }else{
            //for non-recap exercises, analyse strokes (recording word fails) then move to next exercise when ready.
            strokeAnalyser.analyseStrokes(true);
            myScriptConnection.waitUntilFinished();
            currentExercise = exerciseManager.getNextExecise();
        }

        /* Label button "Next" if there exists another page on Recap Exercise.
         * Else label button "Finish (Quit)" and close program when clicked. */
        if(currentExercise instanceof RecapExercise) {
            RecapExercise ex = (RecapExercise) currentExercise;
            if (ex.hasAnotherPage()) {
                nextExBtn.setText("Next");
            } else {
                nextExBtn.setText("Finish (Quit)");
                nextExBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        System.exit(0);
                    }
                });
            }
        }
        resetExercise();
    }

    /**
     * Resets current exercise by clearing the strokes from the canvas and updates the canvas.
     */
    private void resetExercise(){
        exerciseManager.getCurrentExercise().setHighlightedWords(null);
        canvas.clearStrokes();
        canvas.repaint();
    }

    /**
     * Get the Application's Screen Dimension
     * @return Dimension
     */
    public static Dimension getScreenDimension(){
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		return toolkit.getScreenSize();
	}

    /**
     * Create and show a DEALAssistant instance.
     * @param args Unused.
     */
	public static void main(String[] args) {
        new DEALAssistant().setVisible(true);
	}
}
