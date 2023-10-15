#!/bin/bash

# Clean
rm -rf ./bin
mkdir -p ./bin/META-INF
rm replica.jar
rm *.class

# Build the project
cp ./manifest.txt ./bin/META-INF/MANIFEST.MF
javac -cp 'spread.jar' -d ./bin *.java

# Decompress spread.jar
cd bin
jar xf ../spread.jar
cd ..

# Package the jar
jar cfe replica.jar Program -C ./bin/ .
zip -ur replica.jar -j ./spread.jar
chmod +x replica.jar

# Build the docs
pandoc README.md -o Report.pdf
rm group7-artifacts.zip
zip -r group7-artifacts.zip *.java *.pdf input.dat replica.jar

# Clean
rm -rf ./bin