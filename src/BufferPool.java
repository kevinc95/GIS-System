import java.util.ArrayList;

/**
 * Project 4 for CS 3114 Spring 2017: GIS Program
 * 
 * Programmer: Kevin Cheng
 * Last Modified: 4-14-2017
 * 
 * Purpose:
 * This class is for the Buffer Pool, which is designed
 * to contain all the most recently used GIS records. This
 * way can count down on constantly needing to access the 
 * database for a record we just recently accessed.
 *
 */
public class BufferPool {
    
    // the container
    private ArrayList<String> pool;
    
    // counter for the number of elements in pool
    private int numberOfElements;
    
    // the total capacity pool can store
    private int sizeLimit;
    
    /**
     * Initializes the buffer pool.
     * 
     * @param numberOfElementsAllowed the total amount
     *                                  pool can carry
     */
    public BufferPool(int numberOfElementsAllowed) {
        
        pool = new ArrayList<String>();
        numberOfElements = 0;
        sizeLimit = numberOfElementsAllowed;
    }
    
    /**
     * Adds a record, in the form of a string, to the pool
     * 
     * @param record GIS record with offset at beginning
     */
    public void add(String record) {
        
        // in the case that we're adding a record that 
        // already exists in the pool, we simply call
        // remove on the record. if it returns true,
        // then the record that was equal is removed,
        // and we can add our record at index 0 (MRU)
        if (pool.remove(record)) {
            pool.add(0, record);
            return;
        }
        
        // If number of elements in pool does not equal
        // the sizeLimit, then we can simply add our record
        // and increment numberOfElements.
        // Else, pool is full, and we need to remove the 
        // record at the end of the pool(LRU) before we add
        // a new record at index 0 (MRU)
        if (numberOfElements != sizeLimit) {
            pool.add(0, record);
            numberOfElements++;
        }
        else {
            pool.remove(sizeLimit - 1);
            pool.add(0, record);
        }
    }
    
    /**
     * This method searches through the pool for a desired 
     * record based on a specified offset parameter
     * 
     * @param offset offset of the desired record
     * @return the GIS record
     */
    public String find(int offset) {
        
        // create a string of the offset parameter containg 
        // a semicolon at the end
        String offsetColon = String.valueOf(offset) + ":";
        
        // traverse through pool searching
        for (int i = 0; i < numberOfElements; i++) {
            
            // create a String variable containg the String of the record
            String line = pool.get(i);
            
            // parse the record in 2 based on spaces 
            String[] lineParsed = line.split("\\s", 2);
            
            // if the first half equals our offset with a semicolon,
            // return the other second half, which is the GIS record itself
            if (offsetColon.equals(lineParsed[0])) {
                
                return lineParsed[1];
            }
        }
        
        return null;
    }
    
    /**
     * Stringify the whole pool, formatted in multiple lines
     * as so:
     * 
     * MRU
     *      offset: record
     *      offset: record
     *      ....
     * LRU
     * 
     * @return a string of the pool
     */
    public String printPool() {
        
        StringBuilder builder = new StringBuilder();
        
        builder.append("MRU\n");
        
        for (int i = 0; i < numberOfElements; i++) {
            
            builder.append("    " + pool.get(i) + "\n");
        }
        
        builder.append("LRU\n");
        
        return builder.toString();
    }
}
