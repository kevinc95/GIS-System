import java.util.ArrayList;

/**
 * Project 4 for CS 3114 Spring 2017: GIS Program
 * 
 * Programmer: Kevin Cheng
 * Last Modified: 4-14-2017
 * 
 * Purpose:
 * This is a hashtable generic.
 */
public class HashTable<K, V> {

    /**
     * Entry class generic containing key (nameState) and values(offsets)
     * @author Kevin Cheng
     */
    @SuppressWarnings("hiding")
    private class Entry<K, V> {
        
        // concatenation of name and state ("name:state")
        K nameState;
        
        // list of offsets that have the same name and state
        ArrayList<V> offsets;
        
        /**
         * Initializes Entry object with key and values
         * @param key
         * @param values
         */
        public Entry(K key, ArrayList<V> values) {
            
            nameState = key;
            offsets = values;
        }
        
        /**
         * Returns Entry as a string in the format:
         * 
         * "[name:state, [offset(s)]]"
         */
        public String toString() {
            
            StringBuilder builder = new StringBuilder();
            
            // append nameState
            builder.append("[" + nameState.toString() + ", [");
            
            // traverse through offsets arraylist stringifying each offset
            for (int i = 0; i < offsets.size() - 1; i++) {
                
                builder.append(offsets.get(i).toString() + ", ");
            }
            
            builder.append(offsets.get(offsets.size() - 1).toString() + "]]");
            
            return builder.toString();
        }
    }
    
    // current size of the array
    private int size;
    
    // current number of entries
    private int numberOfEntries;
    
    // longest probe sequence done
    private int longestProbe;
    
    // number of locations
    private int numberOfOffsets;
    
    // boolean for if rehashing is occuring
    boolean rehash;
    
    // an array of entries
    private Entry<K, V>[] hashtable;
    
    /**
     * Initializes the HashTable with a startingSize
     * @param startingSize
     */
    @SuppressWarnings("unchecked")
    public HashTable(int startingSize) {
        
        size = startingSize;
        numberOfEntries = 0;
        longestProbe = 0;
        numberOfOffsets = 0;
        rehash = false;
        hashtable = new HashTable.Entry[size];
    }
    
    /**
     * returns size
     * @return size
     */
    public int getSize() {
        return size;
    }
    
    /**
     * return number of entries
     * @return number of entries
     */
    public int getNumberOfEntries() {
        return numberOfEntries;
    }
    
    /**
     * returns longest probe sequence
     * @return longest probe sequence
     */
    public int getLongestProbe() {
        return longestProbe;
    }
    
    /**
     * return number of offsets
     * @return number of offsets
     */
    public int getNumberOfOffsets() {
        return numberOfOffsets;
    }
    
    /**
     * resets the number of offsets to 0
     * so we can count the number of offsets for each import 
     * in GIS system
     */
    public void resetNumberOfOffsets() {
        numberOfOffsets = 0;
    }
    
    /**
     * resets the longest probe sequence to 0
     * so we can count the longest probe sequence per import in gis
     */
    public void resetLongestProbe() {
        longestProbe = 0;
    }
    
    /**
     * inserts into the table
     * @param nameState the concatenation of name and state as so "name:state"
     * @param offsets arraylist containing all the offsets of the nameState
     */
    public void insert(K nameState, ArrayList<V> offsets) {
        
        // check to see if this insert is being used for rehashing
        // if not, increment number of offsets
        // if yes, don't increment cause we're simply re-adding same number of elements
        if (!rehash) {
            numberOfOffsets++;
        }
        
        // create entry of nameState and offsets and insert
        insertHelper(new Entry<K, V>(nameState, offsets));
    }
    
    /**
     * inserts entry to the hashtable
     * @param entry entry with nameState and offsets
     */
    private void insertHelper(Entry<K, V> entry) {
        
        // type cast nameState to string
        String place = ((String)entry.nameState);
        
        // find initial index in table with nameState being used for elfHash function
        int homeSlot = (int) Math.abs((int)elfHash(place)) % size;
        
        // if initial index is empty, insert
        // if initial index is not empty.....
        if (hashtable[homeSlot] == null) {
            hashtable[homeSlot] = entry;
        }
        else {
            
            // Check to see if our entry.nameState and the entry.nameState already 
            // at homeSlot are equal. If so, then just add offset of our entry to 
            // the offsets of homeSlot entry and return
            if (hashtable[homeSlot].nameState.equals(entry.nameState)) {
                
                V entrysOffset = entry.offsets.get(0);
                hashtable[homeSlot].offsets.add(entrysOffset);
                return;
            }
            
            // starting number to use in quadratic probing
            int n = 1;
            
            // find first probeSlot from homeSlot
            int probeSlot = (homeSlot + (((n * n) + n) / 2)) % size;
            
            // do a while loop to keep probing till we find a slot in table that is empty
            while (hashtable[probeSlot] != null) {
                
                // check each entry in each non empty slot to see if any of them ar equal to our entry
                // in nameState
                if (hashtable[probeSlot].nameState.equals(entry.nameState)) {
                    
                    V entrysOffset = entry.offsets.get(0);
                    hashtable[probeSlot].offsets.add(entrysOffset);
                    return;
                }
                
                // increment n for the next probe
                n++;
                
                // calculate next probe
                probeSlot = (homeSlot + (((n * n) + n) / 2)) % size;
            }
            
            // what ever is the highest value n has ever gone will equal the longest probe
            if (n > longestProbe) {
                longestProbe = n; 
            }
            
            // once we've reached an empty slot, insert to that slot
            hashtable[probeSlot] = entry;
        }
        
        // increment number of entries
        numberOfEntries++;
        
        // check if table is 70 percent full
        if (this.seventyPercentFull()) {
            
            //if so, set rehash equal to true so numberOfOffsets does not
            //increment when reinserting to new bigger table
            rehash = true;
            
            //rehash table
            this.resize();
            
            //set rehash boolean back to false to do incrementation again
            rehash = false;
        }
    }
    
    /**
     * search through table for key value(nameState) and return 
     * arraylist of offsets
     * 
     * @param key key to find
     * @return ArrayList of offsets
     */
    public ArrayList<V> find(K key) {
        
        // turn key to string
        String place = ((String)key);
        
        // find the initial homeSlot key would be at
        int homeSlot = (int) Math.abs((int)elfHash(place)) % size;

        // if homeSlot in hashtable is null, key doesn't exist, return null
        // else......
        if (hashtable[homeSlot] == null) {
            return null;
        }
        else {
            
            // if entry.nameState in homeSlot equals key, return the entry.offsets
            // else.....
            if (hashtable[homeSlot].nameState.equals(key)) {
                return hashtable[homeSlot].offsets;
            }
            else {
                
                // set starting number for quadratic probing
                int n = 1;
                
                // calculate first probeSlot
                int probeSlot = (homeSlot + (((n * n) + n) / 2)) % size;
                
                // continue probing while each probeSlot does not equal null
                while (hashtable[probeSlot] != null) {
                    
                    //if entry.nameState in probeSlot is equal to key, return offsets
                    if (hashtable[probeSlot].nameState.equals(key)) {
                        return hashtable[probeSlot].offsets;
                    }
                    
                    //if increment for next probeSlot
                    n++;
                    
                    //calculate probeSlot
                    probeSlot = (homeSlot + (((n * n) + n) / 2)) % size;
                }
                
                // if while loop ends without finding, then null
                return null;
            }
        }
        
    }

    /**
     * Stringifies the whole hashtable 
     */
    public String toString() {
        
        StringBuilder builder = new StringBuilder();
        
        // traverse whole hashtable slot by slot
        for (int i = 0; i < size; i++) {
            
            // if not null at slot, print out slot number and entry
            if (hashtable[i] != null) {
                
                Entry<K, V> entry = hashtable[i];
                builder.append("    " + String.valueOf(i) + ": " + entry.toString() + "\n");
            }
        }
        
        return builder.toString();
    }
    
    /**
     * Boolean to check if hashtable is seventy percent full
     * @return boolean if hashtable is 7/10 full or not
     */
    private boolean seventyPercentFull() {
        
        // typecast number of entries and size to double
        double number = (double)numberOfEntries;
        double length = (double)size;
        
        // calulate percentage
        double percentage = (number / length) * 100;

        if (percentage >= 70) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Doubles the size of the hashtable and rehashes already existing entries
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        
        // double size
        int newSize = size * 2;
        
        // creates copy of old table
        Entry<K, V>[] oldTable = hashtable.clone();
        
        int oldSize = size;
        
        // reinitialize main hashtable array to empty array with double the size
        // set size global variable to newSize
        hashtable = new HashTable.Entry[newSize];
        size = newSize;
        
        // set numberOfEntries to 0 so it will return to the value previously was before resize 
        numberOfEntries = 0;
        
        // traverse old hashtable reinserting any entries it comes across to the new table
        for (int i = 0; i < oldSize; i++) {
            if (oldTable[i] != null) {
                
                this.insert(oldTable[i].nameState, oldTable[i].offsets);
            }
        }
    }
    
    /**
     * Hash function which takes in strings
     * @param toHash string to hash
     * @return long value to moded
     */
    private static long elfHash(String toHash) {

        long hashValue = 0;
        for (int Pos = 0; Pos < toHash.length(); Pos++) { // use all elements

           hashValue = (hashValue << 4) + toHash.charAt(Pos);  // shift/mix

           long hiBits = hashValue & 0xF000000000000000L;      // get high nybble
   
           if (hiBits != 0) {
              hashValue ^= hiBits >> 56; // xor high nybble with second nybble
           }

           hashValue &= ~hiBits;         // clear high nybble
        }

        return ( hashValue & 0x0FFFFFFFFFFFFFFFL );
     }
}
