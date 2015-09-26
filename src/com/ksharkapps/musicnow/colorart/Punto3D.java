package com.ksharkapps.musicnow.colorart;

public class Punto3D {

	private double x,y,z;
	
	public Punto3D() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	public Punto3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double distancia(Punto3D punto2) {
		return Math.sqrt(
				Math.pow(this.x - punto2.getX(), 2) +
				Math.pow(this.y - punto2.getY(), 2) +
				Math.pow(this.z - punto2.getZ(), 2));
	}
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
	
	/*public Punto3D(long x, long y, long z) {
		
	}*/
	
}
