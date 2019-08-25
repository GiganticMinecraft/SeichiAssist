package com.github.unchama.buildassist;

@Deprecated
public class Coordinate {
	private int x;
	private int y;
	private int z;

	public Coordinate() {
		x = 0;
		y = 0;
		z = 0;
	}

	public Coordinate(final int x, final int y, final int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Coordinate(final Coordinate c) {
		this.x = c.x;
		this.y = c.y;
		this.z = c.z;
	}

	public void setXYZ(final int x, final int y, final int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setXYZ(final Coordinate c) {
		this.x = c.x;
		this.y = c.y;
		this.z = c.z;
	}

	public void setZ(final int z) {
		this.z = z;

	}

	public void add(final int x, final int y, final int z) {
		this.x += x;
		this.y += y;
		this.z += z;

	}
}
