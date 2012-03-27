#!/bin/sh -x
CLASSPATH=../craftbukkit-1.1-R8.jar javac *.java -Xlint:unchecked -Xlint:deprecation
rm -rf me
mkdir -p me/exphc/AtmosphericHeights
mv *.class me/exphc/AtmosphericHeights
jar cf AtmosphericHeights.jar me/ *.yml *.java
