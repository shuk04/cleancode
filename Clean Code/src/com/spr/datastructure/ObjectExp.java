package com.spr.datastructure;

//
public class ObjectExp {
	public class Square implements Shape {
		public Point topLeft;
		public double side;

		@Override
		public double area() {

			return side * side;

		}
	}

	public class Rectangle implements Shape {
		public Point topLeft;
		public double height;
		public double width;

		@Override
		public double area() {
			return height * width;

		}
	}

	public class Circle implements Shape {
		public Point center;
		public double radius;

		@Override
		public double area() {
			return PI * radius * radius;
		}
	}

	public interface Shape {
		double area();
	}

	public final double PI = 3.141592653589793;

}
