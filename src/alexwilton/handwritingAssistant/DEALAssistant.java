package alexwilton.handwritingAssistant;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import javax.swing.*;

import alexwilton.handwritingAssistant.exercises.Exercise;
import alexwilton.handwritingAssistant.exercises.Exercise1;
import alexwilton.handwritingAssistant.exercises.ExerciseManager;

public class DEALAssistant extends JFrame{

	private static final String APPLICATION_KEY = "22eda92c-10af-40d8-abea-fd4093c17d81"; //"c34e7a84-a0da-41cb-84f8-b2cf8459c3df"; //
	private static final String RECOGNITION_CLOUD_URL = "ws://cloud.myscript.com/api/v3.0/recognition/ws/text";// "http://cloud.myscript.com/api/v3.0/recognition/rest/text/doSimpleRecognition.json";
	private static final String HMAC_KEY = "a1fa759f-b3ce-4091-9fd4-d34bb870c601"; //"667dc91d-ce7a-4074-a74e-a4ea0a8455b8"

	private Canvas canvas;

    private ExerciseManager exerciseManager;
    private StrokeAnalyser strokeAnalyser;
    private MyScriptConnection myScriptConnection;

	public DEALAssistant() {
		super("Hand Writing Assistant");
        exerciseManager = ExerciseManager.createDefault();
		setup();
	}

	private void setup(){
		Dimension screenSize = getScreenDimension();
		setSize(screenSize.width, screenSize.height);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
        myScriptConnection = new MyScriptConnection(APPLICATION_KEY, HMAC_KEY, RECOGNITION_CLOUD_URL);
        strokeAnalyser = new StrokeAnalyser(exerciseManager, myScriptConnection);
        canvas = new Canvas(strokeAnalyser);
        strokeAnalyser.setCanvas(canvas);
        canvas.setExerciseManager(exerciseManager);

		add(canvas, BorderLayout.CENTER);
        addButtons();
	}

    private void addButtons() {
        Font btnFont = new Font("Arial", Font.PLAIN, 40);
        JButton analyseBtn = new JButton("Check For Mistakes"); analyseBtn.setFont(btnFont);
        analyseBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                strokeAnalyser.analyseStrokes();
            }
        });
        JButton nextExBtn = new JButton("Next Exercise"); nextExBtn.setFont(btnFont);
        nextExBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                exerciseManager.moveToNextExercise();
                resetExercise();
            }
        });
        JButton clearBtn = new JButton("Reset Exercise"); clearBtn.setFont(btnFont);
        clearBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                resetExercise();
            }
        });
        final TextField wordSeperatingDistance = new TextField(); wordSeperatingDistance.setFont(btnFont);
        wordSeperatingDistance.addTextListener(new TextListener() {
            @Override
            public void textValueChanged(TextEvent e) {
                try {
                    int newVal = Integer.parseInt(wordSeperatingDistance.getText());
                    strokeAnalyser.setWordSeparatingDistance(newVal);
                }catch (NumberFormatException nfe){}
            }
        });
        wordSeperatingDistance.setText(strokeAnalyser.getWordSeparatingDistance() + "");
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(1,5));
        btnPanel.add(analyseBtn);
        btnPanel.add(nextExBtn);
        btnPanel.add(wordSeperatingDistance);
        btnPanel.add(clearBtn);

        JPanel mainVerticalPanel = new JPanel();
        mainVerticalPanel.setLayout(new BoxLayout(mainVerticalPanel, BoxLayout.Y_AXIS));
        mainVerticalPanel.add(btnPanel);
        JPanel whiteBottomPadding = new JPanel();
        whiteBottomPadding.setBackground(Color.WHITE); whiteBottomPadding.setBorder(BorderFactory.createEmptyBorder(0,0,100,0));
        mainVerticalPanel.add(whiteBottomPadding);
        add(mainVerticalPanel, BorderLayout.SOUTH);
    }

    private void resetExercise(){
        exerciseManager.getCurrentExercise().setHighlightedWords(null);
        canvas.clearStrokes();
        strokeAnalyser.clearStroke();
        canvas.repaint();
    }

    public static Dimension getScreenDimension(){
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		return toolkit.getScreenSize();
	}

	public static void main(String[] args) {
        new DEALAssistant().setVisible(true);
	}
}
