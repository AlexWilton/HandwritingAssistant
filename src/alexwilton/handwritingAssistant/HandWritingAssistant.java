package alexwilton.handwritingAssistant;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import alexwilton.handwritingAssistant.exercises.Exercise;
import alexwilton.handwritingAssistant.exercises.Exercise1;
import com.myscript.cloud.sample.ws.MyScriptCloud;
import com.myscript.cloud.sample.ws.RecognitionListener;

public class HandWritingAssistant extends JFrame{

	private static final String APPLICATION_KEY = "c34e7a84-a0da-41cb-84f8-b2cf8459c3df";
	private static final String RECOGNITION_CLOUD_URL = "http://cloud.myscript.com/api/v3.0/recognition/rest/text/doSimpleRecognition.json";

	private Canvas canvas;

    private Exercise currentExercise;
    private StrokeAnalyser strokeAnalyser;

	public HandWritingAssistant() {
		super("Hand Writing Assistant");
        currentExercise = new Exercise1();
		setup();
	}

	private void setup(){
		Dimension screenSize = getScreenDimension();

		setSize(screenSize.width, screenSize.height);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		MyScriptCloud text = new MyScriptCloud(RECOGNITION_CLOUD_URL, APPLICATION_KEY);
		canvas = new Canvas(text);
        canvas.setExercise(currentExercise);
        strokeAnalyser = new StrokeAnalyser(currentExercise, text);
        text.setStrokeAnalyser(strokeAnalyser);
		add(canvas, BorderLayout.CENTER);
        addButtons();
	}

    private void addButtons() {
        Button analyseBtn = new Button("Analyse");
        analyseBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                strokeAnalyser.analyseStrokes();
            }
        });
        Panel btnPanel = new Panel();
        btnPanel.setLayout(new GridLayout(5,1));
        btnPanel.add(analyseBtn);
        add(btnPanel, BorderLayout.EAST);
    }

    public static Dimension getScreenDimension(){
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		return toolkit.getScreenSize();
	}

	public static void main(String[] args) {
        new HandWritingAssistant().setVisible(true);
	}
}
