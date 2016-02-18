package com.myscript.cloud.sample.ws.api;

import java.awt.*;
import java.awt.geom.Line2D;

public class Stroke {

	private Point[] coords;

	// cache
	private Box bbox;

	public Stroke(Point... coords) {
		if (coords.length == 0) {
			throw new IllegalArgumentException();
		}
		for (Point p : coords) {
			if (p == null) {
				throw new IllegalArgumentException();
			}
		}
		this.coords = coords.clone();
	}

	public Box getBoundingBox() {
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		if (bbox == null) {
			for (Point p : coords) {
				int x = (int)p.x;
				int y = (int)p.y;
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);
				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);

			}
			bbox = new Box(minX, minY, maxX - minX, maxY - minY);
		}
		return bbox;
	}

	public Point[] getPoints() {
		return coords;
	}

	public float[] getX() {
		float[] res = new float[coords.length];
		for (int i = coords.length; --i >= 0;) {
			res[i] = coords[i].x;
		}
		return res;
	}

	public float[] getY() {
		float[] res = new float[coords.length];
		for (int i = coords.length; --i >= 0;) {
			res[i] = coords[i].y;
		}
		return res;
	}

	public Rectangle getRectangle() {
		Box box = getBoundingBox();
		return new Rectangle(box.x, box.y, box.width, box.height);
	}

	public Line2D.Float[] getRectSides(){
		Box b = getBoundingBox();
		Line2D.Float[] sides = new Line2D.Float[4];
		sides[0] = new Line2D.Float(b.x, b.y, b.x, b.y+b.height);
		sides[1] = new Line2D.Float(b.x, b.y+b.height, b.x+b.width, b.y+b.height);
		sides[2] = new Line2D.Float(b.x, b.y, b.x+b.width, b.y);
		sides[3] = new Line2D.Float(b.x+b.width, b.y, b.x+b.width, b.y+b.height);
		return sides;
	}

	public int[] getCenterPt(){
		int[] center = new int[2];
		Box b = getBoundingBox();
		center[0] = b.x + b.width/2;
		center[1] = b.y + b.height/2;
		return center;
	}
}