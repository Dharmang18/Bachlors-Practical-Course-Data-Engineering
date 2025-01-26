#!/bin/bash

mvn package
rm ./target/bpc-1.0.jar
mv ./target/bpc-1.0-Main.jar ./bpc.jar
