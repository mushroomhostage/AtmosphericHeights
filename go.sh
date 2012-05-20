#!/bin/sh -x
CLASSPATH=../craftbukkit-1.2.5-R2.0.jar javac *.java -Xlint:unchecked -Xlint:deprecation
rm -rf me
mkdir -p me/exphc/AtmosphericHeights
mv *.class me/exphc/AtmosphericHeights
jar cf AtmosphericHeights.jar me/ *.yml *.java
