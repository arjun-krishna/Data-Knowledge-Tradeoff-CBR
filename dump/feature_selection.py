from sklearn.feature_selection import VarianceThreshold
import numpy as np
from sklearn.feature_selection import SelectKBest, chi2
f = open("Travel/processed.csv")
line = f.readline()  # skip the header

K = 3 # Number of features needed
line = line.strip().split(",")
data = np.loadtxt(f,delimiter=",")

#sel = VarianceThreshold(threshold=(.5 * (1 - .5)))

#res = sel.fit_transform(data)

#print res.shape
#print res.shape
#print res[0]
#print data[0]

X = data[:,1:]
y = data[:,1]

X_new = SelectKBest(chi2, k=K).fit_transform(X, y)

for i in range(0,data.shape[1]):	
	for j in range(0,X_new.shape[1]):
		if (data[:,i] == X_new[:,j]).sum() > 1000:
			print line[i]
