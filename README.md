# GIS System

The concept of this project was created, and assigned to me as a final project, by my Data Structures and Algorithms professor, Mr. William McQuain, at Virginia Tech.

The GIS System takes in GIS records from database files, stores that data using a Quadtree and a Hashtable, and allows for search functionalities to retrieve specific GIS records from the System.

# GIS Database Files and Command Output Sample Files

The GISDatabaseFiles folder contains text files each, containing varying amounts of GIS records. The amount of data in these files can go from a small amount of records to a very large amount (about 100,000 records).

The CommandResultSampleFiles folder contains example command (input) and result (output) files to give examples of how the files should be formatted, and what the correct output should be generally. The way the files are organized is that command file, Script01.txt, corresponds with result file, McLog01.txt. Then Script02.txt corresponds with McLog02.txt. Then Script03.txt to McLog.txt, and so on.

# Execution

If you wish to download and run this program, make sure your computer is capable of running Java programs on its system. move any database files you wish to store into the system from the GISDatabaseFiles folder into the src folder. If you wish to test to see if the GIS System actually works correctly, you can move a command file from the CommandResultsSampleFiles folder to the src folder. Then move any database files that the command file specifically imports into the system from the GISDatabaseFiles folder into the into the src folder as well. (To understand the commands that the command file uses, you can check the assignment spec, GIS.pdf, on pages 6 and 7.) 

Once you have moved the necessary files into the src folder, navigate from your command prompt/terminal, to the src directory. Assuming your computer runs Java programs, enter the command 
> javac *.java 

to compile all the java files in the src directory. 
Then enter executing command:
> java GIS <database file name> <command script file name> <log file name>

where <database file name> represents the name you wish to give the database text file that will be a combination of all the database files the command file imports into the system, <command script file name> represents the name of the command file you will be using, and <log file name> represents the name you wish to give the output file that will be a result of the input command file.

Once that command is entered, the program will run and output the combined database file and the log file, each with their given name. You can compare the resulting log file with the corresponding log file to the command file we used in the GISDatabase folder to see if the outputs are the same. 

If you wish to create your own command file to use on the system, make sure your command file follows the same format as the other command files, and make sure the database files you will be using are in the src folder before you run.

If you wish to understand more about this project, you can access the assignment spec, GIS.pdf, for more details.