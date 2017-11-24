#!/bin/bash

javac -cp ../lib/\* AttritionCBR.java
for i in $(seq 1 1 10)
do
	echo "###"
	echo $i
	java -cp .:../lib/\* AttritionCBR data/train.csv data/test.csv rule_files/attrition$i.fcl
done
