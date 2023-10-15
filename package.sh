#!/bin/bash

# Clean
rm -rf ./bin
mkdir -p ./bin/META-INF
rm replica.jar
rm *.class

# Build the project
cp ./manifest.txt ./bin/META-INF/MANIFEST.MF
javac -cp 'spread.jar' -d ./bin *.java


# Package the jar
jar cvfm replica.jar ./bin/META-INF/MANIFEST.MF -C ./bin/ .
zip -ur replica.jar -j ./spread.jar
zip replica.jar META-INF/MANIFEST.MF -d  # Delete the existing manifest file from the JAR
cp ./manifest.txt ./bin/META-INF/MANIFEST.MF
cd bin
zip ../replica.jar META-INF/MANIFEST.MF  # Add the new manifest file to the JAR
cd ..
chmod +x replica.jar

# Build the docs
pandoc README.md -o Report.pdf
rm group7-artifacts.zip
zip -r group7-artifacts.zip *.java *.pdf input.dat replica.jar

# Clean
rm -rf ./bin