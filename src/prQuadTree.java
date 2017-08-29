import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Project 4 for CS 3114 Spring 2017: GIS Program
 * 
 * Programmer: Kevin Eric Cheng
 * Last Modified: 4-14-2017
 * 
 * Purpose:
 * A java generic for a QuadTree.
 */

public class prQuadTree< T extends Compare2D<? super T> > {
    
    // states whether element was removed or not
    private boolean deleted;
    
	abstract class prQuadNode {
		//nothing here
	}
	class prQuadLeaf extends prQuadNode {
		
		public prQuadLeaf() {
		   Elements = new ArrayList<T>();
		}
		
		public prQuadLeaf( T data ) {
		   Elements = new ArrayList<T>();
		   Elements.add(data);
		}

		public ArrayList<T> Elements;
	}
    class prQuadInternal extends prQuadNode {
    	
  	   public prQuadInternal() {
	       NW = null;
	       SW = null;
	       SE = null;
	       NE = null;
  	   }

  	   public prQuadNode NW, SW, SE, NE;
    }
    
    prQuadNode root;
    int numberOfOffsets;
    long xMin, xMax, yMin, yMax;
   
    // Additional data members can be declared as you see fit.
    
    // Initialize quadtree to empty state.
    public prQuadTree(long xMin, long xMax, long yMin, long yMax) {
        
        root = null;
        
        numberOfOffsets = 0;
        
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }
    
    /**
     * return number offsets in tree
     * @return numberOfOffsets
     */
    public int getNumberOfOffsets() {
        return numberOfOffsets;
    }
    
    // Pre:   elem != null
    // Post:  If elem lies within the tree's region, and elem is not already 
    //        present in the tree, elem has been inserted into the tree.
    // Return true iff elem is inserted into the tree. 
    public boolean insert(T elem) {
        
        // if elem does not exist
        if (elem == null) {
            return false;
        }
        
        // if elem is not within world boundaries
        if (elem.getX() < xMin || elem.getX() > xMax ||
                elem.getY() < yMin || elem.getY() > yMax) {
            
            return false;
        }
        
        // insert elem to tree
        root = insertHelper(elem, root, xMin, xMax, yMin, yMax);
        
        numberOfOffsets++;
        
        return true;
	}

    // Pre:  elem != null
    // Returns reference to an element x within the tree such that 
    // elem.equals(x)is true, provided such a matching element occurs within
    // the tree; returns null otherwise.
    public T find(T Elem) {
		
        // if Elem does not exist
        if (Elem == null) {
            return null;
        }
        
        // return found value
		return findHelper(Elem, root, xMin, xMax, yMin, yMax);
	}

    // Pre:  elem != null
    // Post: If elem lies in the tree's region, and a matching element occurs
    //       in the tree, then that element has been removed.
    // Returns true iff a matching element has been removed from the tree.   
    public boolean delete(T Elem) {
		
        // if Elem does not exist
        if (Elem == null) {
            return false;
        }
        
        // if Elem is outside world bounds
        if (Elem.getX() < xMin || Elem.getX() > xMax ||
                Elem.getY() < yMin || Elem.getY() > yMax) {
            return false;
        }
        
        deleted = false;
        
        // begin deletion
        root = this.deleteHelper(Elem, root, xMin, xMax, yMin, yMax);
        
		return deleted;
	}

    // Pre:  xLo < xHi and yLo < yHi
    // Returns a collection of (references to) all elements x such that x is 
    // in the tree and x lies at coordinates within the defined rectangular 
    // region, including the boundary of the region.
    public ArrayList<T> find(long xLo, long xHi, long yLo, long yHi) {
		
        // initialize an arraylist to contain elements 
        // within the specified boundary
        ArrayList<T> withinRegion = new ArrayList<T>();
        
        // run helper function to insert in bound elements to
        // arraylist
        this.regionHelper(root, withinRegion, xLo, xHi, yLo, yHi);
        
        // return arraylist
        return withinRegion;
	}
	
    /**
     * Prints string representation of tree to a file
     * @param sRoot current root
     * @param toFile writer to file
     * @param padding string for spacing
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void printTree(prQuadNode sRoot, FileWriter toFile, String padding) throws IOException {
        
        // if current root is null, then print *
        if (sRoot == null) {
            
            toFile.write(padding + "*\n");
            return;
        }
        
        // if internal node, go down southern region first
        if (sRoot.getClass().equals(prQuadInternal.class)) {
            
            prQuadInternal internal = (prQuadInternal)sRoot;
            printTree(internal.SW, toFile, padding + "    ");
            printTree(internal.SE, toFile, padding + "    ");
        }
        
        // add padding
        toFile.write(padding + "\n");
        
        // if we come across leaf, print out string representation of the points in each bucket
        // else, print @
        if (sRoot.getClass().equals(prQuadLeaf.class)) {
            prQuadLeaf leaf = (prQuadLeaf)sRoot;
            toFile.write(padding + leaf.Elements.get(0).toString());
            for (int i = 1; i < leaf.Elements.size(); i++) {
                toFile.write(" " + leaf.Elements.get(i).toString());
            }
            toFile.write("\n");
        }
        else {
            toFile.write(padding + "@\n");
        }
        
        // then go recursively down northern region
        if (sRoot.getClass().equals(prQuadInternal.class)) {
            
            prQuadInternal internal = (prQuadInternal)sRoot;
            printTree(internal.NE, toFile, padding + "    ");
            printTree(internal.NW, toFile, padding + "    ");
        }
    }
    
	// Additonal methods should be added below:
    
    // Pre: elem exists and is in world bounds
    // Post: elem is inserted as a prQuadLeaf, unless it already exists,
    //       then otherwise it changes global variable inserted to false
    // Recursively goes through tree searching for spot to insert elem.
    @SuppressWarnings("unchecked")
    private prQuadNode insertHelper(T elem, prQuadNode sRoot,
            long xLo, long xHi, long yLo, long yHi) {
        
        // if tree is empty, create new leaf with elem
        // else ...
        if (sRoot == null) {
            return new prQuadLeaf(elem);
        }
        else {

            // if current node is a leaf
            // else, it's an internal node
            if (sRoot.getClass().equals(prQuadLeaf.class)) {
                
                // create variable of current leaf we are at
                prQuadLeaf existingLeaf = ((prQuadLeaf)sRoot);
                
                // check existing leaf for any coordinates that are equal
                // if so, add offset of point we are trying to insert, to point
                // already in tree
                for (int i = 0; i < existingLeaf.Elements.size(); i++) {
                    
                    if (existingLeaf.Elements.get(i).equals(elem)) {
                        
                        int elemsOffset = elem.getOffsets().get(0);
                        existingLeaf.Elements.get(i).getOffsets().add(elemsOffset);
                        return sRoot;
                    }
                }
                
                // if leaf is not full, add point
                // else recursively reinsert all the already existing points in leaf to new internal
                //        and then insert elem to that
                if (existingLeaf.Elements.size() != 4) {
                         
                    existingLeaf.Elements.add(elem);
                    return existingLeaf;
                }
                else {
                    
                    prQuadInternal internal = new prQuadInternal();
                    
                    for (int j = 0; j < existingLeaf.Elements.size(); j++) {
                        internal = ((prQuadInternal)insertHelper(existingLeaf.Elements.get(j), internal, xLo, xHi, yLo, yHi));
                    }
                    
                    internal = ((prQuadInternal)insertHelper(elem, internal, xLo, xHi, yLo, yHi));
                    
                    return internal;
                }
            }
            else {
                
                // create variable of current internal node we are at
                
                prQuadInternal internal = (prQuadInternal)sRoot;
                
                // find midpoint of world bounds
                long xMid = ((xHi + xLo) / 2);
                long yMid = ((yHi + yLo) / 2);
                
                // find which quadrant elem falls in
                Direction elemQuad = elem.directionFrom(xMid, yMid);
                
                // which ever quadrant elem falls in, it will recursively do down
                // that quadrant and find where it needs to insert
                if (elemQuad == Direction.NE) {
                    internal.NE = this.insertHelper(elem, internal.NE, xMid, xHi, yMid, yHi);
                }
                else if (elemQuad == Direction.NW) {
                    internal.NW = this.insertHelper(elem, internal.NW, xLo, xMid, yMid, yHi);
                }
                else if (elemQuad == Direction.SW) {
                    internal.SW = this.insertHelper(elem, internal.SW, xLo, xMid, yLo, yMid);
                }
                else if (elemQuad == Direction.SE){
                    internal.SE = this.insertHelper(elem, internal.SE, xMid, xHi, yLo, yMid);
                }
                else {
                    internal.NE = this.insertHelper(elem, internal.NE, xMid, xHi, yMid, yHi);
                }
                
                return internal;
            }
        }
    }

    // Pre: elem exists
    // Post: tree unchanged
    // Recursively navigates through tree to find elem.
	@SuppressWarnings("unchecked")
    private T findHelper(T elem, prQuadNode sRoot,
	        long xLo, long xHi, long yLo, long yHi) {
	    
	    // if tree is empty, elem not found, return null
	    if (sRoot == null) {
	        return null;
	    }
	    
	    // if elem is out of bounds, return null
	    if (elem.getX() < xMin || elem.getX() > xMax ||
	            elem.getY() < yMin || elem.getY() > yMax) {
	        
	        return null;
	    }
	    
	    // if current node we are at is a leaf
	    if (sRoot.getClass().equals(prQuadLeaf.class)) {
	        
	        prQuadLeaf leaf = ((prQuadLeaf)sRoot);
	        
	        // search through leaf to see if elem exists,
	        // if so, return that point,
	        // if not return null
	        for (int i = 0; i < leaf.Elements.size(); i++) {
	            
	            if (leaf.Elements.get(i).equals(elem)) {
	                return leaf.Elements.get(i);
	            }
	        }
	        
	        return null;
	    }
	    else {
	        
	        prQuadInternal internal = ((prQuadInternal)sRoot);
	        
	        // find midpoint of world bounds
	        long xMid = (xHi + xLo) / 2;
	        long yMid = (yHi + yLo) / 2;
	        
	        // find which quadrant elem falls in
	        Direction quad = elem.directionFrom(xMid, yMid);
	        
	        // which ever quadrant elem falls in, function will recursively
	        // go down respective sub tree to find elem
	        if (quad == Direction.NE) {
	            
	            return findHelper(elem, internal.NE, xMid, xHi, yMid, yHi);
	        }
	        else if (quad == Direction.NW) {
	            
	            return findHelper(elem, internal.NW, xLo, xMid, yMid, yHi);
	        }
	        else if (quad == Direction.SW) {
	            
	            return findHelper(elem, internal.SW, xLo, xMid, yLo, yMid);
	        }
	        else {
	            
	            return findHelper(elem, internal.SE, xMid, xHi, yLo, yMid);
	        }
	    }
	}
	
	// Pre: elem exists and is in world bounds
    // Post: elem is deleted from tree and deleted global variable is changed to true, 
	//       unless it doesn't exist in tree
    // Recursively goes through tree searching for elem to delete.
	@SuppressWarnings("unchecked")
    private prQuadNode deleteHelper(T elem, prQuadNode sRoot, 
	        long xLo, long xHi, long yLo, long yHi) {
	    
	    // if tree is empty, return sRoot
	    // else...
	    if (sRoot == null) {
	        return sRoot;
	    }
	    else {
	        
	        // if current node is leaf,
	        // else, current node is internal
	        if (sRoot.getClass().equals(prQuadLeaf.class)) {
	            
	            // create variable of current leaf
	            prQuadLeaf leaf = ((prQuadLeaf)sRoot);
	            
	            // retrieve value from current leaf
	            T leafPoint = leaf.Elements.get(0);
	            
	            // if value from current leaf is equal to elem
	            if (leafPoint.equals(elem)) {
	                
	                // make leaf arraylist null
	                leaf.Elements = null;
	                
	                // make deleted true
	                deleted = true;
	                
	                // return null for current node
                    return null;
	            }
	            else {
	                
	                // return sRoot
	                return sRoot;
	            }
	        }
	        else {
	            
	            // create variable of current internal
	            prQuadInternal internal = (prQuadInternal)sRoot;
	            
	            // find midpoint of world bounds
	            long xMid = (xHi + xLo) / 2;
	            long yMid = (yHi + yLo) / 2;
	            
	            // find quadrant where elem falls in
	            Direction elemQuad = elem.directionFrom(xMid, yMid);
	            
	            // whichever quadrant elem falls in, it'll go down the respective quadrant 
	            // to find desired element to delete
	            if (elemQuad == Direction.NE) {
	                internal.NE = this.deleteHelper(elem, internal.NE, xMid, xHi, yMid, yHi);
	            }
	            else if (elemQuad == Direction.NW) {
	                internal.NW = this.deleteHelper(elem, internal.NW, xLo, xMid, yMid, yHi);
	            }
	            else if (elemQuad == Direction.SW) {
	                internal.SW = this.deleteHelper(elem, internal.SW, xLo, xMid, yLo, yMid);
	            }
	            else if (elemQuad == Direction.SE) {
	                internal.SE = this.deleteHelper(elem, internal.SE, xMid, xHi, yLo, yMid);
	            }
	            
	            // return internal (contracted if necessary)
	            return this.contract(internal);
	        }
	    }
	}
	
	// Counts the number of children internal node has, 
	// if 0, set internal to null
	// if 1, set internal to that one child
	// else, return the internal
	private prQuadNode contract(prQuadInternal internal) {
	    
	    // initialize counter at 0 to count 
	    // number of child nodes in internal
	    int numInternalChild = 0;
        
	    // if statements to check each quadrant of internal
	    // for child node
        if (internal.NE != null) {
            numInternalChild += 1;
        }
        if (internal.NW != null) {
            numInternalChild += 1;
        }
        if (internal.SW != null) {
            numInternalChild += 1;
        }
        if (internal.SE != null) {
            numInternalChild += 1;
        }
	    
        // if counter equals 0, nullify internal
	    if (numInternalChild == 0) {
	        return null;
	    }
	     
	    // if counter equals 1, change internal to
	    // leaf that contains the one node
	    if (numInternalChild == 1) {
	        
	        if (internal.NE != null) {
	            if (internal.NE.getClass().equals(prQuadLeaf.class)) {
	                return internal.NE;
	            }
	            else {
	                return internal;
	            }
	        }
	        
	        if (internal.NW != null) {
	            if (internal.NW.getClass().equals(prQuadLeaf.class)) {
                    return internal.NW;
                }
	            else {
	                return internal;
	            }
	        }
	        
	        if (internal.SW != null) {
	            if (internal.SW.getClass().equals(prQuadLeaf.class)) {
                    return internal.SW;
                }
	            else {
	                return internal;
	            }
	        }
	        
	        if (internal.SE != null) {
	            if (internal.SE.getClass().equals(prQuadLeaf.class)) {
                    return internal.SE;
                }
	            else {
	                return internal;
	            }
	        }
	    }
	    
	    // else, return internal as is
	    return internal;
	}
	
	// Recursively goes through tree searching for elements that are within
	// the bounds of xLo, xHi, yLo, and yHi, and inserts the elements that are
	// into within ArrayList
	@SuppressWarnings("unchecked")
    private void regionHelper(prQuadNode sRoot, ArrayList<T> within, 
	        long xLo, long xHi, long yLo, long yHi) {
	    
	    // if tree is empty or reaches end, cancel program
	    if (sRoot == null) {
	        return;
	    }
	    
	    // if current node is internal
	    if (sRoot.getClass().equals(prQuadInternal.class)) {
	        
	        //typecast sRoot as prQuadInternal
	        prQuadInternal internal = (prQuadInternal)sRoot;
	        
	        // recursively search through each branch
	        regionHelper(internal.NE, within, xLo, xHi, yLo, yHi);
	        regionHelper(internal.NW, within, xLo, xHi, yLo, yHi);
	        regionHelper(internal.SE, within, xLo, xHi, yLo, yHi);
	        regionHelper(internal.SW, within, xLo, xHi, yLo, yHi);
	    }
	    
	    // if current node is leaf
	    if (sRoot.getClass().equals(prQuadLeaf.class)) {
	        
	        prQuadLeaf leaf = ((prQuadLeaf)sRoot);
	        
	        // typecast world bounds to double
            double xLow = (double)xLo;
            double xHigh = (double)xHi;
            double yLow = (double)yLo;
            double yHigh = (double)yHi;
	        
	        for (int i = 0; i < leaf.Elements.size(); i++) {
	            
	            // retrieve points at leaf
	            T point = leaf.Elements.get(i);
	            
	            // if value at leaf is in bounds, insert into arraylist
	            if (point.inBox(xLow, xHigh, yLow, yHigh)) {
	                
	                within.add(point);
	            }
	        }
	    }
	}
	
}
