import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Project 4 for CS 3114 Spring 2017: GIS System
 * 
 * Programmer: Kevin Cheng
 * Last Modified: 4-14-2017
 * 
 * This is a wrapper class of the prQuadTree.
 *
 */
public class CoordinateIndex {

    // prQuadTree with Point type
    prQuadTree<Point> tree;
    
    /**
     * Initialize the CoordinateIndex with world bounds 
     * 
     * @param xMin west bound
     * @param xMax east bound
     * @param yMin south bound
     * @param yMax north bound
     */
    public CoordinateIndex(long xMin, long xMax, long yMin, long yMax) {
        
        //tree initialized with world bounds
        tree = new prQuadTree<Point>(xMin, xMax, yMin, yMax);
    }
    
    /**
     * Returns number of record locations added to index
     * @return number of offsets added to tree
     */
    public int getNumberOfLocations() {
        return tree.getNumberOfOffsets();
    }
    
    /**
     * inserts into our index
     * 
     * @param point Point object, with coordinates and offset(s)
     * @return boolean if inserted or not
     */
    public boolean insert(Point point) {
        
        return tree.insert(point);
    }
    
    /**
     * Searches through index for a point object
     * @param point Point to be found
     * @return Point that was found
     *          or null, if not found
     */
    public Point find(Point point) {
        
        return tree.find(point);
    }
    
    /**
     * Returns an arraylist of the points that fall within the specified region
     *
     * @param regionXMin 
     * @param regionXMax
     * @param regionYMin
     * @param regionYMax
     * @return ArrayList of points that fall within region
     */
    public ArrayList<Point> regionSearch(long regionXMin, long regionXMax, 
            long regionYMin, long regionYMax) {
        
        return tree.find(regionXMin, regionXMax, regionYMin, regionYMax);
    }
    
    /**
     * Writes a string representation of the quadtree to a file
     * 
     * @param writer writer to the file
     * @throws IOException
     */
    public void print(FileWriter writer) throws IOException {
        
        tree.printTree(tree.root, writer, "    ");
    }
}
