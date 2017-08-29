import java.util.ArrayList;

public class Point implements Compare2D<Point> {

	private long xcoord;
	private long ycoord;
	private ArrayList<Integer> offsets;
	
	public Point() {
		xcoord = 0;
		ycoord = 0;
		offsets = new ArrayList<Integer>();
	}
	public Point(long x, long y) {
		xcoord = x;
		ycoord = y;
		offsets = new ArrayList<Integer>();
	}
	public long getX() {
		return xcoord;
	}
	public long getY() {
		return ycoord;
	}
	public ArrayList<Integer> getOffsets() {
	    return offsets;
	}
	
	public Direction directionFrom(long X, long Y) {
		
		if (X - this.getX() < 0 && Y - this.getY() <= 0) {
		    
		    return Direction.NE;
		}
		else if (X - this.getX() >= 0 && Y - this.getY() < 0) {
		    
		    return Direction.NW;
		}
		else if (X - this.getX() > 0 && Y - this.getY() >= 0) {
		    
		    return Direction.SW;
		}
		else if (X - this.getX() <= 0 && Y - this.getY() > 0) {
		    
		    return Direction.SE;
		}
		else {
		    return Direction.NE;
		}
	}
	
	public Direction inQuadrant(double xLo, double xHi, double yLo, double yHi) {

	    double thisX = (double)this.getX();
	    double thisY = (double)this.getY();
	    
	    double xAxis = ((xHi + xLo) / 2);
        double yAxis = ((yHi + yLo) / 2);
	    
	    if (thisX < xLo || thisX > xHi || 
	            thisY < yLo || thisY > yHi ||
	            (thisX == yAxis && thisY == xAxis)) {
	        
	        return Direction.NOQUADRANT;
	    }
	    
	    if (thisX > yAxis && thisY >= xAxis) {
	        
	        return Direction.NE;
	    }
	    else if (thisX <= yAxis && thisY > xAxis) {
	        
	        return Direction.NW;
	    }
	    else if (thisX < yAxis && thisY <= xAxis) {
	        
	        return Direction.SW;
	    }
	    else {
	        
	        return Direction.SE;
	    }
	}
	
	public boolean inBox(double xLo, double xHi, double yLo, double yHi) {
		
	    double thisX = (double)this.getX();
        double thisY = (double)this.getY();
        
        if (thisX < xLo || thisX > xHi ||
                thisY < yLo || thisY > yHi) {    
            
            return false;
        }
        
        return true;
	}
	
	public String toString() {
		
	    StringBuilder builder = new StringBuilder();
	    
	    builder.append("[");
	    builder.append("(" + xcoord + ", " + ycoord + "), ");
	    
	    for (int i = 0; i < offsets.size() - 1; i++) {
	        builder.append(offsets.get(i) + ", ");
	    }
	    
	    builder.append(offsets.get(offsets.size() - 1));
	    builder.append("]");
	    
		return builder.toString();
	}
	
	public boolean equals(Object o) {
	    
	    if (o == this) {
	        
	        return true;
	    }
	    else if (o == null) {
	        
	        return false;
	    }
	    else if (this.getClass() == o.getClass()) {
	        
	        Point other = (Point)o;
	        
	        return this.getX() == other.getX() &&
	                this.getY() == other.getY();
	    }
	    else {
	        
	        return false;
	    }
	}
}
