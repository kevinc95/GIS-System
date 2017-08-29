import java.util.ArrayList;

/**
 * Project 4 for CS 3114 Spring 2017: GIS Program
 * 
 * Programmer: Kevin Cheng
 * Last Modifed: 4-14-2017
 *
 * Purpose:
 * This is a wrapper class for hashtable.
 */
public class NameIndex {

    // initialize hashtable with key of stype string and value of type integer
    HashTable<String, Integer> table;
    
    /**
     * intialize NameIndex with a starting size
     * @param startingSize 
     */
    public NameIndex(int startingSize) {
        table = new HashTable<String, Integer>(startingSize);
    }
    
    /**
     * return size of table
     * @return size
     */
    public int size() {
        
        return table.getSize();
    }
    
    /**
     * return number of offsets
     * @return numberOfOffsets
     */
    public int numberOfNames() {
        
        return table.getNumberOfOffsets();
    }
    
    /**
     * return numberOfEntries
     * @return numberOfEntries
     */
    public int numberOfEntries() {
        
        return table.getNumberOfEntries();
    }
    
    /**
     * resets numberOfNames to 0
     */
    public void resetNumberOfNames() {
        
        table.resetNumberOfOffsets();
    }
    
    /**
     * return longest probe sequence
     * @return longestProbe
     */
    public int longestProbe() {
        
        return table.getLongestProbe();
    }
    
    /**
     * reset longestProbe to 0
     */
    public void resetLongestProbe() {
        
        table.resetLongestProbe();
    }
    
    /**
     * Insert to index
     * @param name name of record
     * @param state state of record
     * @param offsets list of offsets
     */
    public void insert(String name, String state, ArrayList<Integer> offsets) {
        
        table.insert(name + ":" + state, offsets);
    }
    
    /**
     * Search for offsets with the same name and state
     * @param name name of record
     * @param state state record
     * @return return arraylist with list of offsets
     */
    public ArrayList<Integer> find(String name, String state) {
        
        return table.find(name + ":" + state);
    }
    
    /**
     * prints out a string representation of the nameIndex
     * @return string of each entry name index
     */
    public String print() {
        
        return table.toString();
    }
}
