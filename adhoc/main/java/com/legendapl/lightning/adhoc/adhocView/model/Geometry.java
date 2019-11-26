package com.legendapl.lightning.adhoc.adhocView.model;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;

import javafx.scene.Node;
import javafx.scene.layout.Region;

public class Geometry {
	
	public static class Double {
		public static boolean equal(double a1, double a2) {
			return Math.abs(a1 - a2) < 1e-9;
		}
		public static boolean lessEqual(double a1, double a2) {
			return a1 < a2 || equal(a1, a2);
		}
		public static boolean largeEqual(double a1, double a2) {
			return a1 > a2 || equal(a1, a2);
		}
	}
	
	public static final GeometryFactory Factory = new GeometryFactory();
	
	/*-------------------------------------Point-----------------------------------------*/
	
	public static Point createPoint(double x, double y) {
		return Factory.createPoint(new Coordinate(x, y));
	}
	
	public static Point createPoint(Node node) {
		return Factory.createPoint(new Coordinate(node.getLayoutX(), node.getLayoutY()));
	}
	
	public static List<Point> createPoints(List<Node> list) {
		List<Point> points = new ArrayList<>();
		for (Node node : list) {
			points.add(createPoint(node));
		}
		return points;
	}
	
	public static Point getMiddlePoint(Point p1, Point p2) {
		return createPoint((p1.getX() + p2.getX()) * 0.5, (p1.getY() + p2.getY()) * 0.5);
	}
	
	/*-------------------------------------Vertex-----------------------------------------*/
	
	public static Vertex createVertex(Point p1, Point p2) {
		return new Vertex(p2.getX() - p1.getX(), p2.getY() - p1.getY());
	}
	
	public static double crossProduct(Vertex p, Vertex v) {
        return (p.getX() * v.getY() - p.getY() * v.getX());
    }

	public static double dot(Vertex p, Vertex v) {
        return (p.getX() * v.getX() + p.getY() * v.getY());
    }
	
	/*-------------------------------------Rectangle-----------------------------------------*/
	
	public static Polygon createRectangle(Region region) {
		Coordinate cos[] = new Coordinate[5];
		cos[0] = new Coordinate(region.getLayoutX(), region.getLayoutY());
		cos[1] = new Coordinate(region.getLayoutX() + region.getWidth(), region.getLayoutY());
		cos[2] = new Coordinate(region.getLayoutX() + region.getWidth(), region.getLayoutY() + region.getHeight());
		cos[3] = new Coordinate(region.getLayoutX(), region.getLayoutY() + region.getHeight());
		cos[4] = new Coordinate(region.getLayoutX(), region.getLayoutY());
		return Factory.createPolygon(cos);
	}
	
	public static List<Polygon> createRectangles(List<? extends Region> list) {
		List<Polygon> polygons = new ArrayList<>();
		for (Region region : list) {
			polygons.add(createRectangle(region));
		}
		return polygons;
	}
	
	public static Polygon getClosestRectangle(Point point, List<Polygon> polygons) {
		Integer index = getClosestRectangleIndex(point, polygons);
		if (-1 == index) {
			return null;
		} else {
			return polygons.get(index);
		}
	}
	
	public static Integer getClosestRectangleIndex(Point point, List<Polygon> polygons) {
		int index = -1;
		double minDis = 0;
		for (int i = 0; i < polygons.size(); i ++) {
			double dis = point.distance(polygons.get(i));
			if (0 == i || dis < minDis) {
				minDis = dis;
				index = i;
			}
		}
		return index;
	}
	
	public static Point getMiddlePoint(Polygon rectangle) {
		return getMiddlePoint(
				Factory.createPoint(rectangle.getCoordinates()[0]),
				Factory.createPoint(rectangle.getCoordinates()[2]));
	}
	
	public static List<Point> getRectanglePoints(Polygon rectangle) {
		List<Point> points = new ArrayList<>();
		Coordinate[] cos = rectangle.getCoordinates();
		for (Coordinate co : cos) {
			points.add(Factory.createPoint(co));
		}
		return points;
	}
	
	/*-------------------------------------TEST-----------------------------------------*/
	
	public static void main(String[] args) {
		Coordinate cos[] = new Coordinate[5];
		cos[0] = new Coordinate(0, 0);
		cos[1] = new Coordinate(0, 1);
		cos[2] = new Coordinate(1, 1);
		cos[3] = new Coordinate(1, 0);
		cos[4] = new Coordinate(0, 0);
		Polygon polygon = Factory.createPolygon(cos);
		System.out.printf("dis = %f\n", polygon.distance(Factory.createPoint(new Coordinate(0.5, 0.5))));
		System.out.printf("dis = %f\n", polygon.distance(Factory.createPoint(new Coordinate(2, 0))));
		System.out.printf("dis = %f\n", polygon.distance(Factory.createPoint(new Coordinate(2, -1))));
		System.out.printf("dis = %f\n", polygon.distance(Factory.createPoint(new Coordinate(2, 0.5))));
		System.out.printf("dis = %f\n", polygon.distance(Factory.createPoint(new Coordinate(1, 1))));
	}
	
}
