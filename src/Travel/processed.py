fp = open("data.csv","r")
fpw = open("processed.csv","w")

indices = [1,2,4,5,6,7,8,9,10]
for line in fp:
	line = line.strip().split(",")
	for i in range(0,len(line)):
		if i in indices:
			if i != 10:
				fpw.write(line[i]+",")
			else:
				fpw.write(line[i]+"\n")

