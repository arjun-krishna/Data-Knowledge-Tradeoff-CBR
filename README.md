# Data-Knowledge-Tradeoff-CBR
* Study Data-Knowledge Trade-off in Case Base Reasoning System. 
* Study the importance of domain knowledge in reducing the number of case bases.


A few observations for the term paper example:
In general, most of the predictions are matching with the expected output. In some cases, like days late = 12 and medical certificate = true, though the data does not accept the paper, the CBR system predicts accepted. 

To get the weights for a given domain:
	python learn.py TermPaperExample/ --verbose

The above command reads the attribute.xml and the data.csv files and generates the weights.
