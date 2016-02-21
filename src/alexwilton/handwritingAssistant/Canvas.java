package alexwilton.handwritingAssistant;

import alexwilton.handwritingAssistant.exercises.Exercise;
import com.myscript.cloud.sample.ws.api.Box;
import com.myscript.cloud.sample.ws.api.Point;
import com.myscript.cloud.sample.ws.api.Stroke;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Canvas extends JComponent implements MouseListener, MouseMotionListener {
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color STROKE_COLOR = Color.BLACK;
    private static final java.awt.Stroke STROKE_STYLE = new BasicStroke(3,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final java.awt.Stroke DRAWING_STROKE_STYLE = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private List<Stroke> strokes = new ArrayList<Stroke>();
    private boolean drawing;
    private List<Point> pendingPoints = new ArrayList<Point>();
    private Graphics2D g;
    private StrokeAnalyser strokeAnalyser;
    private Exercise exercise = null;

public Canvas(StrokeAnalyser strokeAnalyser) {
        addMouseListener(this);
        addMouseMotionListener(this);
        setDoubleBuffered(true);
        this.strokeAnalyser = strokeAnalyser;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        g = (Graphics2D) g0;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(STROKE_COLOR);
        g.setStroke(STROKE_STYLE);

        if (!strokes.isEmpty()) {
            Iterator<Stroke> iterator = strokes.iterator();

            while (iterator.hasNext()) {
                Stroke stroke = iterator.next();

                List<Point> pointsAsList = Arrays.asList(stroke.getPoints());

                drawPoints(g, pointsAsList);
            }
        }

        if (!pendingPoints.isEmpty()) {
            g.setStroke(DRAWING_STROKE_STYLE);
            drawPoints(g, pendingPoints);
        }

        if(exercise != null) exercise.draw(g);

    }

    private void drawPoints(Graphics2D g, List<Point> points) {
        if (points != null && points.size() > 0) {
            Point previous = points.get(0);
            if (points.size() == 1) {
                g.fillRect((int) previous.x, (int) previous.y, 1, 1);
            } else {
                // first point already handled
                for (int i = 0; ++i < points.size(); ) {
                    Point current = points.get(i);
                    g.drawLine((int) previous.x, (int) previous.y, (int) current.x, (int) current.y);
                    previous = current;
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        drawing = true;
        addPendingPoint(e);
    }

    public void mouseDragged(MouseEvent e) {
        addPendingPoint(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (drawing) {
            addPendingPoint(e);
            Stroke resultingStroke = finalizePendingStroke();
            drawing = false;
            triggerRecognizer(resultingStroke);
        }
    }

    private Stroke finalizePendingStroke() {
        Point[] points = pendingPoints.toArray(new Point[pendingPoints.size()]);
        pendingPoints.clear();
        Stroke stroke = new Stroke(points);
        strokes.add(stroke);
        if (drawing) {
            repaint(getBoundingBox(stroke));
        }
        return stroke;
    }

    private Rectangle getBoundingBox(Stroke stroke) {
        Box bbox = stroke.getBoundingBox();
        return new Rectangle(bbox.x - 1, bbox.y - 1, bbox.width + 2,
                bbox.height + 2);
    }

    private void addPendingPoint(MouseEvent e) {
        Point lastPoint;
        Point newPoint = new Point(e.getX(), e.getY());
        if (!pendingPoints.isEmpty()) {
            lastPoint = pendingPoints.get(pendingPoints.size() - 1);
        } else {
            lastPoint = newPoint;
        }
        pendingPoints.add(newPoint);
        if (drawing) {
            Stroke fake = new Stroke(new Point[]{newPoint, lastPoint});
            repaint(getBoundingBox(fake));
        }
    }

    private void triggerRecognizer(Stroke s) {
        strokeAnalyser.addStroke(s);
    }

    public void clearStrokes(){
        strokes = new ArrayList<>();
    }

    public void mouseMoved(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseClicked(MouseEvent arg0) {
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

//    public void repaint(){
//
//    }

}