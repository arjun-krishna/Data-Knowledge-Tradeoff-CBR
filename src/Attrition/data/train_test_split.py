import random
fp = open("attrition.csv","r")
fp1 = open("train.csv","w")
fp2 = open("test.csv","w")

train_ratio = 0.8

count = 0

for line in fp:
	if count == 0:
		fp1.write(line)
		fp2.write(line)
	else:
		num = random.random()
		if num < train_ratio:
			fp1.write(line)
		else:
			fp2.write(line)
	count += 1
