import numpy as np
import matplotlib.pyplot as plt
num_rules = 10
cases = []
acc = []
prec = []
rec = []
for i in range(0,num_rules):
	raw_input()
	raw_input()
	raw_input()
	acc_line = raw_input()
	acc.append(float(acc_line.split("=")[1].replace("%","").strip()))
	raw_input()
	prec_line = raw_input()
	prec.append(float(prec_line.split("=")[1].replace("%","").strip()))
	raw_input()
	rec_line = raw_input()
	rec.append(float(rec_line.split("=")[1].replace("%","").strip()))
	
	cases_line = raw_input()
	cases.append(float(cases_line.split(":")[1].strip()))
	
cases = np.array(cases)
acc = np.array(acc)
prec = np.array(prec)
rec = np.array(rec)

#plt.plot(cases,acc,'ro-')
l1, = plt.plot(cases,prec,'bo-',label="Precision")
l2, = plt.plot(cases,rec,'ro-', label="Recall")
plt.xlabel("Number of cases")
plt.ylabel("Evaluation Measure")
plt.title("Precision and Recall for Attrition=\"No\"")
plt.legend(handles=[l1, l2],loc=2)
plt.show()
