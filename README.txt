Instructions for running the project:

1. Create a new project in Eclipse and put all the .java files inside the src folder of the newly created project.

2. Add the external "Parallel Java" Library to the created project i.e. "pj20120620.jar" file.

3. Open a terminal and after you have reached the src directory of the project, you need to set the CLASSPATH.

4. type "export CLASSPATH=:./pj20120620.jar"

5. Compile the project i.e. "javac *.java".

6. Run the project by typing "java DocPlacMain testDataInputFile"

7. The testDataInputFile is the input file which contains all the parameters required for simulation with their values.

Note: You can put as many cases as you want in the testDataInputFile. Just make sure there is a blank line between every case since the program starts the computation of the case when it encounters a blank line.