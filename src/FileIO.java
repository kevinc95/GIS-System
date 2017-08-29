import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Project 4 for CS 3114 Spring 2017: GIS System
 * 
 * Programmer: Kevin Cheng
 * Last Modified: 4-14-2017
 * 
 * Purpose:
 * This class deals with the creation of the database file
 * (name of file specified by args[0]), the reading of 
 * the command file(name specified by args[1]), and the creation
 * of the log file(name specified by args[2]). This class runs the 
 * commands from the command file and retrieves data from our
 * database file to be used for our log file.
 */
public class FileIO {
    
    // keeps track of what command we are on
    private int commandNumber;
    
    // keeps track of what offset the database file ends at
    private int currentEndOfFile;
    
    // world bounds
    private long xMin, xMax, yMin, yMax;
    
    /**
     * Initializes the FileIO object and begins system.
     * 
     * @param files Name of the files
     * @throws IOException
     */
    public FileIO(String[] files) throws IOException {
        
        commandNumber = 1;
        currentEndOfFile = 265;
        xMin = 0;
        xMax = 0;
        yMin = 0;
        yMax = 0;
        
        // if database file name already exists as
        // a file in directory, delete it. we will
        // recreate file from scratch
        File dbFile = new File(files[0]);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        // begins system
        this.generateResult(files);
    }
    
    /**
     * This method generates the database file(fileNames[0]) and 
     * log file(fileNames[2]) based on the command file's(fileNames[1]) 
     * command's.
     * 
     * @param fileNames name of files
     * @throws IOException
     */
    public void generateResult(String[] fileNames) throws IOException {
        
        // if arguments does not contain three elements, something is
        // wrong with our execution command in the command prompt.
        // else continue with system
        if (fileNames.length != 3) {
            
            System.out.println(
                    "The format of the command should be 'java GIS "
                    + "<database file name> <command script file name> "
                    + "<log file name>'.");
            System.exit(-1);
        }
        else {
            
            // initialize command file and a scanner
            File commandFile = null;
            Scanner commandScanner = null;
            
            try {
                commandFile = new File(fileNames[1]);
                commandScanner = new Scanner(commandFile);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("The command file specified does not exist"
                        + " in the current directory.");
                System.exit(-1);
            }
            
            // initialize database file and create a new file,
            // create RAF so we can access file by seeking offset
            // intialize log file and a FileWriter
            File databaseFile = new File(fileNames[0]);
            databaseFile.createNewFile();
            RandomAccessFile databaseAccess = new RandomAccessFile(databaseFile, "r");;
            File logFile = new File(fileNames[2]);
            FileWriter logWriter = new FileWriter(logFile);
            
            // initialize our data structures
            CoordinateIndex tree = null;
            NameIndex table = null;
            BufferPool pool = null;
            
            // create variable that will represent a line in the command file
            String currentCommandLine;
            
            // while loop to traverse each line in command file
            while (commandScanner.hasNextLine()) {
                
                currentCommandLine = commandScanner.nextLine();
                
                // scan current command line we are on and parse by spaces
                Scanner lineParser = new Scanner(currentCommandLine);
                lineParser.useDelimiter("\\s");
                
                // if current command line is empty, do nothing and go to next line
                // else begin reading line 
                if (!currentCommandLine.equals("")) {
                    
                    // retrieve first element in command line
                    String token = lineParser.next();
                    
                    // if first element is semi colon, add line to log file
                    // if first element is world...
                    // else, check if it's other commands
                    if (token.equals(";") || token.substring(0, 1).equals(";")) {
                        logWriter.write(currentCommandLine + "\n");
                        lineParser.close();
                    }
                    else if (token.equals("world")) {
                        
                        // write command to log
                        logWriter.write(currentCommandLine + "\n\n");
                        
                        // split up the world command by spaces and create array
                        String[] commandParsed = currentCommandLine.split("\\s");
                        
                        // convert each coordinate in world command to seconds
                        xMin = this.dmsToSecLong(commandParsed[1]);
                        xMax = this.dmsToSecLong(commandParsed[2]);
                        yMin = this.dmsToSecLat(commandParsed[3]);
                        yMax = this.dmsToSecLat(commandParsed[4]);
                        
                        // initialize data structures
                        tree = new CoordinateIndex(xMin, xMax, yMin, yMax);
                        table = new NameIndex(1024);
                        pool = new BufferPool(15);
                        
                        // print introduction in log
                        logWriter.write("GIS PROGRAM\nby Kevin Cheng\n\n");
                        
                        logWriter.write("Database File: " + fileNames[0] + "\n");
                        logWriter.write("Command File: " + fileNames[1] + "\n");
                        logWriter.write("Log File: " + fileNames[2] + "\n");
                        
                        logWriter.write("World boundaries converted to seconds: " + 
                                String.valueOf(xMin) + " " + String.valueOf(xMax) + " " + 
                                String.valueOf(yMin) + " " + String.valueOf(yMax) + "\n\n");
                        
                        lineParser.close();
                    }
                    else {
                        
                        // print out each consecutive command with what command number it is in log
                        logWriter.write("Command " + String.valueOf(commandNumber) + ": " + currentCommandLine + "\n\n");
                        
                        // if elseif else statement to see what command to execute
                        if (token.equals("import")) {
                            
                            // get next element after "import", which is the GIS record file name
                            // from which to add to our database file
                            token = lineParser.next();
                            
                            // get GIS record file and put scanner to it
                            File toDatabaseFile = new File(token);
                            Scanner toDatabase = new Scanner(toDatabaseFile);
                            
                            // initialize writer for database file
                            FileWriter databaseWriter = new FileWriter(databaseFile, true);
                            
                            // these instructions are to help with getting the number of 
                            // records inserted into each data structure for JUST THIS
                            // CALL to import, not overall
                            table.resetNumberOfNames();
                            int previousNumberOfLocations = tree.getNumberOfLocations();
                            
                            // insert to database file, tree, and table
                            this.appendInsertToDatabase(toDatabase, databaseWriter, tree, table);
                            
                            databaseWriter.close();
                            
                            // write out number of inserts for each data structure and longest probe sequence
                            logWriter.write("    Imported Features by name: " + table.numberOfNames() + "\n");
                            logWriter.write("    Longest probe sequence: " + table.longestProbe() + "\n");
                            logWriter.write("    Imported Locations: " + (tree.getNumberOfLocations() - previousNumberOfLocations) + "\n\n");
                            
                            // reset longest probe sequence to 0, so we can get longest 
                            // probe sequence for the next import
                            table.resetLongestProbe();
                            
                            toDatabase.close();
                        }
                        else if (token.equals("debug")) {
                            
                            // get next element after "debug" to see what structure to debug
                            token = lineParser.next();
                            
                            // if quad, print out quadtree,
                            // if hash, print hashtable with size and number of entries
                            // if pool, print pool from most recently used to least
                            // if world, then I'm happy I didn't need to do that
                            // else, something is wrong
                            if (token.equals("quad")) {
                                tree.print(logWriter);
                                logWriter.write("\n");
                            }
                            else if (token.equals("hash")) {
                                
                                logWriter.write("    Format of display is\n    Slot number: data record\n");
                                logWriter.write("    Current table size is " + table.size() + "\n");
                                logWriter.write("    Number of elements in table is " + table.numberOfEntries() + "\n\n");
                                
                                logWriter.write(table.print() + "\n");
                            }
                            else if (token.equals("pool")) {
                                logWriter.write(pool.printPool() + "\n");
                            }
                            else if (token.equals("world")) {
                                logWriter.write("Not required. heh :P\n\n");
                            }
                            else {
                                logWriter.write("The container to be debugged is not specified.\n\n");
                            }
                        }
                        else if (token.equals("what_is_at")) {
                            
                            // retrieve coordinates 
                            String[] commandParsed = currentCommandLine.split("\\s");
                            String latitude = commandParsed[1];
                            String longitude = commandParsed[2];

                            // convert corrdinates to seconds
                            long x = this.dmsToSecLong(longitude);
                            long y = this.dmsToSecLat(latitude);
                            
                            // search tree for coordinates
                            Point point = tree.find(new Point(x, y));
                            
                            // if not found, record doesn't exist
                            // if found....
                            if (point == null) {
                                logWriter.write("    Nothing was found at (" + 
                                                this.longTranslate(longitude) + ", " + 
                                                this.latTranslate(latitude) + ").\n\n");
                            }
                            else {
                                
                                logWriter.write("   The following features were found at (" + 
                                        this.longTranslate(longitude) + ", " + 
                                        this.latTranslate(latitude) + "):\n\n");
                                
                                // get the offsets of the point found in tree
                                ArrayList<Integer> offsets = point.getOffsets();
                                
                                // traverse through arraylist containing offsets
                                for (int i = 0; i < offsets.size(); i++) {
                                    
                                    // initialize line that will contain GIS record from database
                                    String line = null;
                                    
                                    // check to see if the line is still in the pool
                                    String recentLine = pool.find(offsets.get(i));
                                    
                                    // if it is, line will equal GIS record from pool
                                    // if not, random access database file with offset
                                    if (recentLine != null) {
                                        line = recentLine;
                                    }
                                    else {
                                        databaseAccess.seek(offsets.get(i));
                                        line = databaseAccess.readLine();
                                    }
                                    
                                    // split GIS record by "|", create array, and retrieve data
                                    String[] data = line.split("\\|", 20);
                                    
                                    String featureName = data[1];
                                    String countyName = data[5];
                                    String state = data[3];
                                    
                                    // write info to log
                                    logWriter.write("    " + offsets.get(i) + ": " + featureName + "  " + 
                                                    countyName + "  " + state + "\n");
                                    
                                    // add record to pool
                                    pool.add(offsets.get(i) + ": " + line);
                                }
                                
                                logWriter.write("\n");
                            }
                            
                        }
                        else if (token.equals("what_is")) {
                            
                            // split current command into array based on spaces
                            String[] commandParsed = currentCommandLine.split("\\s");
                            
                            // retrieving feature name this way to deal with any spaces in
                            // feature name
                            StringBuilder featureName = new StringBuilder();
                            
                            for (int i = 1; i < commandParsed.length - 2; i++) {
                                featureName.append(commandParsed[i] + " ");
                            }
                            featureName.append(commandParsed[commandParsed.length - 2]);
                            String state = commandParsed[commandParsed.length - 1];
                            
                            // search through hashtable for specified feature name and state 
                            // to retrieve offsets
                            ArrayList<Integer> offsets = table.find(featureName.toString(), state);
                            
                            // if not found, does not exist
                            // else, get offsets  to access database
                            if (offsets == null) {
                                logWriter.write("    No records match " + featureName.toString() + " and "+ state + ".\n\n");
                            }
                            else {
                             
                                // traverse through arraylist to get offsets
                                for (int i = 0; i < offsets.size(); i++) {
                                    
                                    // GIS record
                                    String line = null;
                                    
                                    // see if record still exists in pool based on offset
                                    String recentLine = pool.find(offsets.get(i));
                                    
                                    // if yes, line equal that recent line
                                    // else, access database
                                    if (recentLine != null) {
                                        line = recentLine;
                                    }
                                    else {
                                        databaseAccess.seek(offsets.get(i));
                                        line = databaseAccess.readLine();
                                    }
                                    
                                    // split GIS record and retrieve necessary data
                                    String[] data = line.split("\\|", 20);
                                    
                                    String countyName = data[5];
                                    
                                    // convert longitude and latitude into more readable format
                                    String longitude = this.longTranslate(data[8]);
                                    String latitude = this.latTranslate(data[7]);
                                    
                                    // write out found data
                                    logWriter.write("   " + offsets.get(i) + ": " + countyName + " (" + longitude + ", " + latitude + ")\n");
                                    
                                    // add GIS record to pool
                                    pool.add(offsets.get(i) + ": " + line);
                                }
                                
                                logWriter.write("\n");
                            }
                            
                        }
                        else if (token.equals("what_is_in")) {
                            
                            // split apart command
                            String[] commandParsed = currentCommandLine.split("\\s");
                            
                            // calculate bounds of the requested region
                            long xLo = this.dmsToSecLong(commandParsed[commandParsed.length - 3]) - 
                                    Long.valueOf(commandParsed[commandParsed.length - 1]);
                            long xHi = this.dmsToSecLong(commandParsed[commandParsed.length - 3]) + 
                                    Long.valueOf(commandParsed[commandParsed.length - 1]);
                            long yLo = this.dmsToSecLat(commandParsed[commandParsed.length - 4]) - 
                                    Long.valueOf(commandParsed[commandParsed.length - 2]);
                            long yHi = this.dmsToSecLat(commandParsed[commandParsed.length - 4]) + 
                                    Long.valueOf(commandParsed[commandParsed.length - 2]);
                            
                            // get arraylist of points containging all points within region
                            ArrayList<Point> pointsWithin = tree.regionSearch(xLo, xHi, yLo, yHi);
                            
                            // if arraylist is empty, write to log nothing is within requested region
                            // else, write statement that something is
                            if (pointsWithin.isEmpty()) {
                                logWriter.write("    Nothing was found in (" +
                                        this.longTranslate(commandParsed[commandParsed.length - 3]) + " +/- " 
                                        + commandParsed[commandParsed.length - 1] +
                                        ", " + this.latTranslate(commandParsed[commandParsed.length - 4]) + " +/- " 
                                        + commandParsed[commandParsed.length - 2] + ").\n");
                            }
                            else {
                                logWriter.write("    The following features were found in (" + 
                                                this.longTranslate(commandParsed[commandParsed.length - 3]) + " +/- " 
                                                + commandParsed[commandParsed.length - 1] +
                                                ", " + this.latTranslate(commandParsed[commandParsed.length - 4]) + " +/- " 
                                                + commandParsed[commandParsed.length - 2] + "):\n\n");
                            }
                            
                            // traverse through arraylist processing each point
                            for (int i = 0; i < pointsWithin.size(); i++) {
                                
                                Point point = pointsWithin.get(i);
                                
                                // get current point's offsets
                                ArrayList<Integer> offsets = point.getOffsets();
                                
                                for (int j = 0; j < offsets.size(); j++) {
                                    
                                    //GIS record
                                    String line = null;
                                    
                                    //check pool for record
                                    String recentLine = pool.find(offsets.get(j));
                                    
                                    // set equalto line if record exists
                                    // else access database
                                    if (recentLine != null) {
                                        line = recentLine;
                                    }
                                    else {
                                        databaseAccess.seek(offsets.get(j));
                                        line = databaseAccess.readLine();
                                    }
                                    
                                    //split record
                                    String[] data = line.split("\\|", 20);
                                    
                                    // if "what_is_in" line has only 5 elements,
                                    // no filters, retrieve date
                                    // if 6 elements, most likely -long filter
                                    // elongate data retrieval and write
                                    // if 7, most likely -filter
                                    if (commandParsed.length == 5) {
                                        String name = data[1];
                                        String state = data[3];
                                        
                                        //translate to readable format
                                        String longitude = this.longTranslate(data[8]);
                                        String latitude = this.latTranslate(data[7]);
                                        
                                        // write out
                                        logWriter.write("    " + offsets.get(j) + ": " + name + " " + state + 
                                                " (" + longitude + ", " + latitude + ")\n");
                                        
                                        // add record to database
                                        pool.add(offsets.get(j) + ": " + line);
                                    }
                                    else if (commandParsed.length == 6) {
                                        
                                        // if -long filter exists, data retrieval and write out, 
                                        // else something is wrong with command
                                        if (commandParsed[1].equals("-long")) {
                                            
                                            // retrieve necessary data and write
                                            String featureID = data[0];
                                            String featureName = data[1];
                                            String featureCat = data[2];
                                            String state = data[3];
                                            String county = data[5];
                                            String longitude = this.longTranslate(data[8]);
                                            String latitude = this.latTranslate(data[7]);
                                            String elevFt = data[16];
                                            String USGS = data[17];
                                            String date = data[18];
                                            
                                            logWriter.write("    Feature ID: " + featureID + "\n");
                                            logWriter.write("    Feature Name: " + featureName + "\n");
                                            logWriter.write("    Feature Cat: " + featureCat + "\n");
                                            logWriter.write("    State: " + state + "\n");
                                            logWriter.write("    County: " + county + "\n");
                                            logWriter.write("    Longitude: " + longitude + "\n");
                                            logWriter.write("    Latitude: " + latitude + "\n");
                                            
                                            if (!elevFt.equals("")) {
                                                logWriter.write("    Elev in ft: " + elevFt + "\n");
                                            }
                                            
                                            if (!USGS.equals("")) {
                                                logWriter.write("    USGS Quad: " + USGS + "\n");
                                            }
                                            
                                            if (!date.equals("")) {
                                                logWriter.write("    Date created: " + date + "\n");
                                            }
                                            
                                            logWriter.write("\n");
                                            
                                            // add record to pool
                                            pool.add(offsets.get(j) + ": " + line);
                                        }
                                        else {
                                            logWriter.write("Something is wrong with this command.\n");
                                        }
                                        
                                    }
                                    else if (commandParsed.length == 7){
                                        if (commandParsed[1].equals("-filter")) {
                                            
                                            // if desired filter is water...
                                            // if desired filter is pop...
                                            // if desired filter is structure...
                                            // else, something is wrong with this command.
                                            if (commandParsed[2].equals("water")) {
                                                
                                                // only retrieve data and write if feature cat equals one of these
                                                if (data[2].equals("Arroyo") || data[2].equals("Bay") ||
                                                        data[2].equals("Bend") || data[2].equals("Canal") ||
                                                        data[2].equals("Channel") || data[2].equals("Falls") ||
                                                        data[2].equals("Glacier") || data[2].equals("Gut") ||
                                                        data[2].equals("Harbor") || data[2].equals("Lake") ||
                                                        data[2].equals("Rapids") || data[2].equals("Reservoir") ||
                                                        data[2].equals("Sea") || data[2].equals("Spring") ||
                                                        data[2].equals("Stream") || data[2].equals("Swamp") ||
                                                        data[2].equals("Well")) {
                                                    
                                                    String name = data[1];
                                                    String state = data[3];
                                                    
                                                    String longitude = this.longTranslate(data[8]);
                                                    String latitude = this.latTranslate(data[7]);
                                                    
                                                    logWriter.write("    " + offsets.get(j) + ": " + name + " " + state + 
                                                            " (" + longitude + ", " + latitude + ")\n");
                                                    
                                                    pool.add(offsets.get(j) + ": " + line);
                                                }
                                            }
                                            else if (commandParsed[2].equals("pop")) {
                                                
                                                // only do data retrieval and write if feature cat equals "Populated place"
                                                if (data[2].equals("Populated Place")) {
                                                    
                                                    String name = data[1];
                                                    String state = data[3];
                                                    
                                                    String longitude = this.longTranslate(data[8]);
                                                    String latitude = this.latTranslate(data[7]);
                                                    
                                                    logWriter.write("    " + offsets.get(j) + ": " + name + " " + state + 
                                                            " (" + longitude + ", " + latitude + ")\n");
                                                    
                                                    pool.add(offsets.get(j) + ": " + line);
                                                }
                                            }
                                            else {
                                                
                                                // only do data retrieval and write out if feature cat equals one of these
                                                if (data[2].equals("Airport") || data[2].equals("Bridge") ||
                                                        data[2].equals("Building") || data[2].equals("Church") ||
                                                        data[2].equals("Dam") || data[2].equals("Hospital") ||
                                                        data[2].equals("Levee") || data[2].equals("Park") ||
                                                        data[2].equals("Post Office") || data[2].equals("School") ||
                                                        data[2].equals("Tower") || data[2].equals("Tunnel")) {
                                                    
                                                    String name = data[1];
                                                    String state = data[3];
                                                    
                                                    String longitude = this.longTranslate(data[8]);
                                                    String latitude = this.latTranslate(data[7]);
                                                    
                                                    logWriter.write("    " + offsets.get(j) + ": " + name + " " + state + 
                                                            " (" + longitude + ", " + latitude + ")\n");
                                                    
                                                    pool.add(offsets.get(j) + ": " + line);
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        logWriter.write("Something is wrong with this command.\n");
                                    }
                                }
                            }
                            
                            logWriter.write("\n");
                        }
                        else {
                            //quit
                            logWriter.write("    Terminating execution of commands.\n");
                            break;
                        }
                        
                        //increment commandNumber for next command in while loop
                        commandNumber++;
                        lineParser.close();
                    }
                }
                
            }
            
            logWriter.close();
            databaseAccess.close();
        }
        
    }
    
    /**
     * This helper method translates a string of a longitude 
     * coordinate in dms format to a long in seconds.
     * 
     * @param dms string of a longitude coordinate in dms format
     * @return long of coordinate in seconds
     */
    private long dmsToSecLong(String dms) {
        
        // parse string accordingly
        String d = dms.substring(0, 3);
        String m = dms.substring(3, 5);
        String s = dms.substring(5, 7);
        String hemisphere = dms.substring(7);
        
        // convert to long
        long degrees = Long.valueOf(d);
        long minutes = Long.valueOf(m);
        long seconds = Long.valueOf(s);
        
        // calculate to seconds
        long coordinate = (degrees * 60 * 60) + (minutes * 60) + seconds;
        
        // if hemisphere is W turn coordinate negative
        if (hemisphere.equals("W")) {
            
            coordinate *= -1;
        }
        
        return coordinate;
    }
    
    /**
     * This helper function translates a string of a latitude coordinate in dms
     * to a long in seconds
     * 
     * @param dms string of a latitude coordinate in dms
     * @return long of the coordinate in seconds
     */
    private long dmsToSecLat(String dms) {
        
        // parse string accordingly
        String d = dms.substring(0, 2);
        String m = dms.substring(2, 4);
        String s = dms.substring(4, 6);
        String hemisphere = dms.substring(6);
        
        // convert to long
        long degrees = Long.valueOf(d);
        long minutes = Long.valueOf(m);
        long seconds = Long.valueOf(s);
        
        // calulcate to seconds
        long coordinate = (degrees * 60 * 60) + (minutes * 60) + seconds;
        
        // if hemisphere is S, turn coordinate negative
        if (hemisphere.equals("S")) {
            
            coordinate *= -1;
        }
        
        return coordinate;
    }
    
    /**
     * Turns a string of a longitude coordinate in dms
     * to a more readable format
     * Ex: XXd XXm XXs East\West 
     * 
     * @param dms string of longitude coordinate
     * @return String of readable coordinate
     */
    private String longTranslate(String dms) {
        
        // parse string
        String d = dms.substring(0, 3);
        String m = dms.substring(3, 5);
        String s = dms.substring(5, 7);
        String hemisphere = dms.substring(7);
        
        // check for any leading 0's in d, m, and s
        // and get rid of them if they exist
        if (d.substring(0, 1).equals("0")) {
            d = dms.substring(1, 3);
        }
        
        if (m.substring(0, 1).equals("0")) {
            m = dms.substring(4, 5);
        }
        
        if (s.substring(0, 1).equals("0")) {
            s = dms.substring(6, 7);
        }
        
        // if hemisphere is E, end returning string in East,
        // vice versa is it's W
        if (hemisphere.equals("E")) {
            return d + "d " + m + "m " + s + "s East";
        }
        else {
            return d + "d " + m + "m " + s + "s West";
        }
    }
    
    /**
     * Turns a string of a latitude coordinate in dms
     * to a more readable format
     * Ex: XXd XXm XXs North\South 
     * 
     * @param dms string of latitude coordinate
     * @return String of readable coordinate
     */
    private String latTranslate(String dms) {
        
        // parse string
        String d = dms.substring(0, 2);
        String m = dms.substring(2, 4);
        String s = dms.substring(4, 6);
        String hemisphere = dms.substring(6);
        
        // check for any leading zeros
        // if there is, get rid of it
        if (m.substring(0, 1).equals("0")) {
            m = dms.substring(3, 4);
        }
        
        if (s.substring(0, 1).equals("0")) {
            s = dms.substring(5, 6);
        }
        
        // if hemisphere is N, end returning string in North,
        // if S, vice versa
        if (hemisphere.equals("N")) {
            return d + "d " + m + "m " + s + "s North";
        }
        else {
            return d + "d " + m + "m " + s + "s South";
        }
    }
    
    /**
     * This helper function is responsible for appending valid records to the database
     * file, determining the offset of where those records end up in the file, and
     * inserting coordinates with respective offsets into the tree, and inserting
     * names with respective offsets into the table
     * 
     * @param from scanner of GIS record file
     * @param databaseWriter writer to the database file
     * @param tree Coordinate Index
     * @param table Name index
     * @throws IOException 
     */
    private void appendInsertToDatabase(Scanner from, FileWriter databaseWriter,
            CoordinateIndex tree, NameIndex table) throws IOException {
     
        // retrieve first line from GIS record file, which is most likely 
        // the sample record
        String currentLine = from.nextLine();
        
        // if currentEndOfFile is 265, which is what I initiated currentEndOfFile as
        // and is the length of the sample record, (this implies database file is empty), 
        // insert sample record as first line of database file
        if (currentEndOfFile == 265) {
            
            databaseWriter.write(currentLine + "\n");
        }    
            
        // traverse GIS record file, checking each record if it's valid
        while (from.hasNextLine()) {
                
            currentLine = from.nextLine();
                
            String[] data = currentLine.split("\\|", 20);
                
            String longitude = data[8];
            String latitude = data[7];
                
            // if longitude and latitude of record is "Unknown" or empty,
            // do not consider them and continue
            // else....
            if ((!longitude.equals("Unknown") && !latitude.equals("Unknown")) &&
                        (!longitude.equals("") && !latitude.equals(""))) {
                long xcoord = this.dmsToSecLong(longitude);
                long ycoord = this.dmsToSecLat(latitude);
                    
                // intialize point object with record's coordinates
                Point point = new Point(xcoord, ycoord);
                    
                //check to see if point is within the bounds of the world
                // if yes...
                if (point.inBox(xMin, xMax, yMin, yMax)) {
                    
                    // add record to database
                    databaseWriter.write(currentLine + "\n");
                        
                    // add currentEndOfFile as the offset to point object
                    point.getOffsets().add(currentEndOfFile);
                    tree.insert(point);
                        
                    // create arraylist containg currentEndOfFile as offset
                    ArrayList<Integer> offsets = new ArrayList<Integer>();
                    offsets.add(currentEndOfFile);
                        
                    // add offset arraylist, along with the name and state of the record
                    // to insert method for nameindex/hashtable
                    table.insert(data[1], data[3], offsets);
                        
                    // finally increment currentEndOfFile by the length of the record we are
                    // currently on since the record was added to the database
                    currentEndOfFile += currentLine.length() + 1;
                }
            }
        }

    }
}
