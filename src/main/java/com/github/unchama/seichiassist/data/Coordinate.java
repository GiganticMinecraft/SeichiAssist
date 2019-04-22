package com.github.unchama.seichiassist.data;

public class Coordinate {
	public int x;
	public int y;
	public int z;

	public Coordinate(){
		x = 0;
		y = 0;
		z = 0;
	}
	public Coordinate(int x,int y,int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Coordinate(Coordinate c) {
		this.x = c.x;
		this.y = c.y;
		this.z = c.z;
	}
	public void setXYZ(int x,int y,int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public void setXYZ(Coordinate c) {
		this.x = c.x;
		this.y = c.y;
		this.z = c.z;
	}
	public void setZ(int z) {
		this.z = z;

	}
	public void add(int x, int y, int z) {
		this.x += x;
		this.y += y;
		this.z += z;

	}

}
