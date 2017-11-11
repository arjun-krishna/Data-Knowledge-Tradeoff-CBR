fp = open("travel.csv","r")
fpw = open("data.csv","w")
count = 0
transports = ["Car","Train","Coach","Plane"]
months = ["November","December","January","February","March","April","May","June","July","August","September","October"]
hotels = ["ZeroStar","OneStar","TwoStars","ThreeStars","FourStars","FiveStars","HolidayFlat"]
for line in fp:
	#print count
	if count == 1:
		count += 1
		continue
	if count == 0:
		line = line.strip().split("&")[1:-1]
		for i in range(0,4):
			fpw.write(line[i]+",")
		for i in range(0,4):
			fpw.write("is"+transports[i]+",")
		for i in range(5,len(line)):
			if i != len(line)-1:
				fpw.write(line[i]+",")
			else:
				fpw.write(line[i]+"\n")
	else:
		line = line.strip().split("&")[1:-1]
		for i in range(0,4):
			fpw.write(line[i]+",")
		for i in range(0,4):
			if line[4] == transports[i]:
				fpw.write("1,")
			else:
				fpw.write("0,")
		for i in range(5,len(line)):
			if i == 5:
				fpw.write(line[i]+",")
			elif i == 6:
				fpw.write(str(months.index(line[i]))+",")
			elif i == 7:
				fpw.write(str(hotels.index(line[i]))+",")
			else:
				fpw.write(line[i]+"\n")
				
	
	count+=1
	
fpw.close()
