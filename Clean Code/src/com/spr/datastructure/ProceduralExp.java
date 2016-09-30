package com.spr.datastructure;

//
public class ProceduralExp {
	public class Square {
		public Point topLeft;
		public double side;
	}

	public class Rectangle {
		public Point topLeft;
		public double height;
		public double width;
	}

	public class Circle {
		public Point center;
		public double radius;
	}

	public class Geometry {
		public final double PI = 3.141592653589793;

		public double area(Object shape) {
			if (shape instanceof Square) {
				Square s = (Square) shape;
				return s.side * s.side;
			}
			if (shape instanceof Circle) {
				Circle s = (Circle) shape;
				return PI * s.radius * s.radius;
			}
			if (shape instanceof Rectangle) {
				Rectangle s = (Rectangle) shape;
				return s.height * s.width;
			}
			return PI;
		}

	}
}
