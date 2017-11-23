test : MyCBRTest.class
	java -cp .:myCBR-3.1.jar MyCBRTest

MyCBRTest.class : MyCBRTest.java
	javac -cp .:myCBR-3.1.jar MyCBRTest.java
	
termpaper_reduced: TermPaperCBR_reduced.class
	java -cp .:myCBR-3.1.jar TermPaperCBR_reduced ../data/term_paper_reduced.csv
	
termpaper: TermPaperCBR.class
	java -cp .:myCBR-3.1.jar TermPaperCBR ../data/term_paper_data.csv
	
TermPaperCBR_reduced.class : TermPaperCBR_reduced.java
	javac -cp .:myCBR-3.1.jar TermPaperCBR_reduced.java
	
TermPaperCBR.class : TermPaperCBR.java
	javac -cp .:myCBR-3.1.jar TermPaperCBR.java

main : Main.class
	java -cp .:jdom-2.0.6.jar Main

Main.class : Main.java FCBR/*
	javac -cp .:jdom-2.0.6.jar Main.java
	
	
travel: TravelCBR.class
	java -cp .:myCBR-3.1.jar TravelCBR ../data/travel_data.csv
	
TravelCBR.class : TravelCBR.java
	javac -cp .:myCBR-3.1.jar TravelCBR.java

clean :
	rm *.class
