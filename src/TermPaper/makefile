TermPaper : TermPaperCBR.class
	#java -cp .:../jFuzzyLogic.jar -cp .:../myCBR-3.1.jar TermPaperCBR data/term_paper_data.csv termpaper.fcl
	java -cp .:../lib/\* TermPaperCBR data/term_paper_reduced.csv term_paper.fcl
	
TermPaperCBR.class : TermPaperCBR.java
	#javac -cp .:../jFuzzyLogic.jar -cp .:../myCBR-3.1.jar TermPaperCBR.java
	javac -cp ../lib/\* TermPaperCBR.java

clean :
	rm *.class
