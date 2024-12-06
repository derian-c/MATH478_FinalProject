# MATH478_FinalProject
An animation showcasing Kruskal's Algorithm using JavaFX

## _Dependencies_
- **Java:** At least 21, latest version can be downloaded at https://jdk.java.net/23/
- **JavaFX:** This code uses JavaFX 23.0.1, can be downloaded at https://gluonhq.com/products/javafx/

## _Installation_
Dowload this code repository by using git:
`git clone https://github.com/derian-c/MATH478_FinalProject.git`
Check to make sure Java 21 or later is installed `java -version`
If Java 21 or later is installed, you can skip the Java installation steps.
Download latest JDK at the link in the previous section.
Extract the files by running `tar -xf name_of_file`
Add the bin directory of the extracted directory to the PATH environment variable by running `PATH=$PATH:./name_of_java_directory/bin`
Download JavaFX at the link in the previous section.
Extract file in the same way and add make an environment variable called PATH_TO_FX that points to the javafx/lib directory by running `export PATH_TO_FX=path/to/javafx-sdk-23.0.1/lib`

##_Compiling_##
Compile using the following command `javac --module-path $PATH_TO_FX --add-modules ALL-MODULE-PATH KruskalsAnimation.java`

##_Running_##
Run using the following command `java --module-path $PATH_TO_FX --add-modules ALL-MODULE-PATH KruskalsAnimation`
If you want the JVM to have extrea heap memory (for particularly large datasets), you can add the -XmxSIZE flag. 
For example, if you want to allow for 4GB of max heap storage, run with the command `java -Xmx4g --module-path $PATH_TO_FX --add-modules ALL-MODULE-PATH KruskalsAnimation`

##_Input File Format_##
The input file must have the following format:
x1,y1
x2,y2
x3,y3
.
.
.
xn,yn

Each line is a set of x and y coordinates for a point, separated with a comma in the middle. There can be no empty lines between points, no space after or before the comma, and no new line at the end of the file.

##_Program Commands_#
SPACE_BAR: Pauses animation
E: Ends animation (skips to the end)
N: Generates new set of points (if no input file was given)
Q: Restarts animation
