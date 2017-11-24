Attrition : AttritionCBR.class
	java -cp .:../lib/\* AttritionCBR data/train.csv data/test.csv attrition.fcl
	
AttritionCBR.class : AttritionCBR.java
	javac -cp ../lib/\* AttritionCBR.java
	
test: Attrition.class
	java -cp .:../lib/\* Attrition
	
Attrition.class: Attrition.java
	javac -cp ../lib/\* Attrition.java

clean :
	rm *.class
