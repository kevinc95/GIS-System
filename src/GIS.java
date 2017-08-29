import java.io.IOException;

//On my honor:
//
//- I have not discussed the Java language code in my program with
//anyone other than my instructor or the teaching assistants
//assigned to this course.
//
//- I have not used Java language code obtained from another student,
//or any other unauthorized source, either modified or unmodified. 
//
//- If any Java language code or documentation used in my program
//was obtained from another source, such as a text book or course
//notes, that has been clearly noted with a proper citation in
//the comments of my program.
//
//- I have not designed this program in such a way as to defeat or
//interfere with the normal operation of the Curator System.
//
//Kevin Eric Cheng

/**
 * Project 4 for CS 3114 Spring 2017: GIS System
 * 
 * Programmer: Kevin Cheng
 * Last Modified: 4-14-2017
 * 
 * Purpose:
 * This program indexes a database of GIS records, that are within 
 * world boundaries specified by the user, by both their coordinates 
 * and their names. This will then allow for search commands of any 
 * GIS records in the database by either specifying the name of the 
 * record, the coordinate of the record, or the region in which the 
 * records could lie.
 */

public class GIS {

    /**
     * Initializes the FileIO class, which is responsible for reading 
     * from files, creating and writing to files, and retrieving the 
     * necessary information from the coordinate and name indexes.
     * @param args The names of the files we will be creating and using
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        
        // initialize FileIO to take in the file names in args,
        // begin processing commands, and do data retrieval
        FileIO result = null;
        
        try {
            result = new FileIO(args);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
