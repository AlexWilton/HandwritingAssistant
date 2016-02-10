package alexwilton.handwritingAssistant;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import alexwilton.handwritingAssistant.exercises.Exercise1;
import com.myscript.cloud.sample.ws.MyScriptCloud;
import com.myscript.cloud.sample.ws.RecognitionListener;

public class HandWritingAssistant extends JFrame implements RecognitionListener {

	private static final String APPLICATION_KEY = "";//"c34e7a84-a0da-41cb-84f8-b2cf8459c3df";
	private static final String RECOGNITION_CLOUD_URL = "http://cloud.myscript.com/api/v3.0/recognition/rest/text/doSimpleRecognition.json";

	private Canvas canvas;

	public HandWritingAssistant() {
		super("Hand Writing Assistant");
		setup();
		startExercises();
	}

	private void setup(){
		Dimension screenSize = getScreenDimension();

		setSize(screenSize.width, screenSize.height);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		MyScriptCloud text = new MyScriptCloud(RECOGNITION_CLOUD_URL, APPLICATION_KEY);
		canvas = new Canvas(text);
		text.setListener(this);
		add(canvas, BorderLayout.CENTER);
	}

	public static Dimension getScreenDimension(){
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		return toolkit.getScreenSize();
	}

	private void startExercises() {
		Exercise1 exercise1 = new Exercise1(canvas);
		exercise1.displayExercise();
	}

	public void recognitionResult(String recognized) {
		System.out.println( (recognized == null) ? "couldn't recognise stroke!!!" : recognized + "\n------------------\n");
	}

	public static void main(String[] args) {
		final HandWritingAssistant demo = new HandWritingAssistant();

		demo.setVisible(true);
	}
}
