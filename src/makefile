test : MyCBRTest.class
	java -cp .:myCBR-3.1.jar MyCBRTest

MyCBRTest.class : MyCBRTest.java
	javac -cp .:myCBR-3.1.jar MyCBRTest.java
	
termpaper: TermPaperCBR.class
	java -cp .:myCBR-3.1.jar TermPaperCBR ../data/term_paper_data.csv
	
TermPaperCBR.class : TermPaperCBR.java
	javac -cp .:myCBR-3.1.jar TermPaperCBR.java

clean :
	rm *.class
