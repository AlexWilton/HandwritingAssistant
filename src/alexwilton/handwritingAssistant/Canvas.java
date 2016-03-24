package alexwilton.handwritingAssistant;

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

/**
 * Canvas records strokes from learner and sends them to the Stroke Analyser. It also
 * renders exercises along with their visual feedback.
 */
public class Canvas extends JComponent implements MouseListener, MouseMotionListener {
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color STROKE_COLOR = Color.BLACK;
    private static final java.awt.Stroke STROKE_STYLE = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final java.awt.Stroke DRAWING_STROKE_STYLE = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    /**
     * Boolean determines whether strokes are drawn or not when captured.
     */
    private boolean showStrokes = false;

    /**
     * Boolean used to track when a stroke is in the process of being drawn
     */
    private boolean drawing;

    /**
     * List of all strokes (regardless of whether strokes are being shown or not)
     */
    private List<Stroke> strokes = new ArrayList<>();

    /**
     * List of Points which form the current stroke being formed.
     */
    private List<Point> pendingPoints = new ArrayList<>();

    /**
     * Exercise Manager allows canvas to access and render current exercise.
     */
    private ExerciseManager exerciseManager;

    public Canvas() {
        /* Assign Mouse+MouseMotion Listeners */
        addMouseListener(this);
        addMouseMotionListener(this);
        setDoubleBuffered(true);
    }

    /**
     * Paint Component is automatically called to render the canvas.
     * @param g0 Graphics which used for rendering canvas
     */
    @Override
    protected void paintComponent(Graphics g0) {
        /* Graphics setup */
        Graphics2D g = (Graphics2D) g0;
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(STROKE_COLOR);
        g.setStroke(STROKE_STYLE);

        /* Draw Current Exercise */
        if (exerciseManager != null) exerciseManager.getCurrentExercise().draw(g);

        /* If allowed, draw strokes (including semi-formed stroke in progress) */
        if (!strokes.isEmpty() && showStrokes) {
            Iterator<Stroke> iterator = strokes.iterator();
            while (iterator.hasNext()) {
                Stroke stroke = iterator.next();
                List<Point> pointsAsList = Arrays.asList(stroke.getPoints());
                drawLineUsingPoints(g, pointsAsList);
            }
        }
        if (!pendingPoints.isEmpty() && showStrokes) {
            g.setStroke(DRAWING_STROKE_STYLE);
            drawLineUsingPoints(g, pendingPoints);
        }

    }

    /**
     * Given a list of point, draw them on as a single continous line on the graphics
     * @param g Graphics2D to draw on
     * @param points List of points.
     */
    private void drawLineUsingPoints(Graphics2D g, List<Point> points) {
        if (points != null && points.size() > 0) {
            Point previous = points.get(0);
            if (points.size() == 1) {
                g.fillRect((int) previous.x, (int) previous.y, 1, 1);
            } else {
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
            finalizePendingStroke();
            drawing = false;
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

    /**
     * Get a tight fitting rectangle containing stroke
     * @param stroke Stroke
     * @return Rectangle
     */
    private Rectangle getBoundingBox(Stroke stroke) {
        Box bbox = stroke.getBoundingBox();
        return new Rectangle(bbox.x - 1, bbox.y - 1, bbox.width + 2,
                bbox.height + 2);
    }

    /**
     * Add Pending Point.
     * @param e MouseEvent from which pending point can be extracted.
     */
    private void addPendingPoint(MouseEvent e) {
        Point lastPoint;
        Point newPoint = new Point(e.getX(), e.getY());
        if (!pendingPoints.isEmpty()) {
            lastPoint = pendingPoints.get(pendingPoints.size() - 1);
        } else {
            lastPoint = newPoint;
        }

        /* Add to pending points then redraw area containing it*/
        pendingPoints.add(newPoint);
        if (drawing) {
            Stroke pendingLine = new Stroke(new Point[]{newPoint, lastPoint});
            repaint(getBoundingBox(pendingLine));
        }
    }

    public void clearStrokes() {
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

    public List<Stroke> getStrokes() {
        return strokes;
    }

    public void setExerciseManager(ExerciseManager exerciseManager) {
        this.exerciseManager = exerciseManager;
    }

    public void setShowStrokes(boolean showStrokes) {
        this.showStrokes = showStrokes;
    }
}