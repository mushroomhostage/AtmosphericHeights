#!/bin/sh -x
CLASSPATH=../craftbukkit-1.1-R8.jar javac *.java -Xlint:unchecked -Xlint:deprecation
rm -rf me
mkdir -p me/exphc/ThinnerAir
mv *.class me/exphc/ThinnerAir
jar cf ThinnerAir.jar me/ *.yml *.java
