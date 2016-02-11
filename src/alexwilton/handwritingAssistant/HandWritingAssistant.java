package alexwilton.handwritingAssistant;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import alexwilton.handwritingAssistant.exercises.Exercise;
import alexwilton.handwritingAssistant.exercises.Exercise1;
import com.myscript.cloud.sample.ws.RecognitionListener;

public class HandWritingAssistant extends JFrame{

	private static final String APPLICATION_KEY = "22eda92c-10af-40d8-abea-fd4093c17d81"; //"c34e7a84-a0da-41cb-84f8-b2cf8459c3df"; //
	private static final String RECOGNITION_CLOUD_URL = "ws://cloud.myscript.com/api/v3.0/recognition/ws/text";// "http://cloud.myscript.com/api/v3.0/recognition/rest/text/doSimpleRecognition.json";
	private static final String HMAC_KEY = "a1fa759f-b3ce-4091-9fd4-d34bb870c601"; //"667dc91d-ce7a-4074-a74e-a4ea0a8455b8"

	private Canvas canvas;

    private Exercise currentExercise;
    private StrokeAnalyser strokeAnalyser;
    private MyScriptConnection myScriptConnection;

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

//		MyScriptCloud myScriptCloud = new MyScriptCloud(RECOGNITION_CLOUD_URL, APPLICATION_KEY, HMAC_KEY);
        myScriptConnection = new MyScriptConnection(APPLICATION_KEY, HMAC_KEY, RECOGNITION_CLOUD_URL);
        strokeAnalyser = new StrokeAnalyser(currentExercise, myScriptConnection);
        canvas = new Canvas(strokeAnalyser);
        canvas.setExercise(currentExercise);

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
