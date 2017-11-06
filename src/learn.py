"""
@desc : Learn weights in case of prod-sum
"""
import argparse
import pandas	as pd
import xml.etree.ElementTree as ET

verbose = False
debug = False

def parse(xml_file) :
	tree = ET.parse(xml_file)
	root = tree.getroot()

	X = []
	y = []

	for item in root.findall('./attribute') :
		name = ''
		attr_type = {}
		levels = []
		for child in item :
			if child.tag == 'name' :
				name = child.text.strip()
			if child.tag == 'type' :
				for e in child :
					attr_type[e.tag] = e.text.strip()
			if child.tag == 'levels' :
				for v in child :
					x = {}
					for a in v :
						if a.tag == 'name' :
							x['name'] = a.text.strip()
						if a.tag == 'function' :
							x['function'] = {}
							for c in a :
								try :
									x['function'][c.tag] = float(c.text.strip()) 
								except ValueError :
									x['function'][c.tag] = None
					levels.append(x)

		if (debug) :
			print '============================='
			print name
			print attr_type
			print levels
			print '============================='

		if attr_type['var'] == 'known' :
			X.append({'name' : name, 'type' : attr_type, 'levels' : levels})
		else :
			y.append({'name' : name, 'type' : attr_type, 'levels' : levels})

	return X, y

def fuzzy_value(value, func) :
	if func :
		if func['ul'] == None :														# Left Skew
			if value <= func['uh'] :
				return 1.0

			else :
				if value >= func['uh'] + func['dh'] :
					return 0.0	
				else :
					return (1 - ((value-func['uh'])/func['dh']))

		if func['uh'] == None :														# Right Skew
			if value >= func['ul'] :
				return 1.0

			else :
				if value <= func['ul'] - func['dl'] :
					return 0.0
				else :
					return (1 - ((func['ul']-value)/func['dl']))

		if (func['ul'] != None and func['uh'] != None) :			# Trapezoid Function
			if (func['ul'] <= value and value <= func['uh']) :
				return 1.0

			elif (value < func['ul']) :	# Left Trapezium
				if value <= func['ul'] - func['dl'] :
					return 0.0
				else :
					return (1 - ((func['ul']-value)/func['dl']))

			else :										# right Trapezium
				if value >= func['uh'] + func['dh'] :
					return 0.0	
				else :
					return (1 - ((value-func['uh'])/func['dh']))
	else :
		return value

def fuzzify(data, X) :
	cols = []
	M = {}
	headers = []
	for e in X :
		if len(e['levels']) :
			for l in e['levels'] :
				tag = l['name'] + '_' + e['name']
				cols.append(tag)
				try :
					M[e['name']].append(l['name'])
				except KeyError :
					M[e['name']] = []
					M[e['name']].append(l['name'])
		else :
			cols.append(e['name'])
		headers.append(e['name'])
	

	df = pd.DataFrame([], columns=cols)

	
	for index, row in data.iterrows() :
		fuzzy_row = {}
		for attr in headers :
			try :
				for h in M[attr] :
					func = None
					for e in X :
						if len(e['levels']) :
							for l in e['levels'] :
								if l['name'] == h :
									tag = l['name'] + '_' + e['name']
									func = l['function']
									fuzzy_row[tag] = fuzzy_value(row[attr], func)

			except KeyError :
				func = None
				fuzzy_row[attr] = fuzzy_value(row[attr], func)

		# print fuzzy_row	
		df = df.append(fuzzy_row, ignore_index=True)

	return df



if __name__ == "__main__" :

	parser = argparse.ArgumentParser(description='Learn Weights for fuzzy rules')

	parser.add_argument('dir', help='directory containing data, attributes.xml')

	parser.add_argument('--verbose', action="store_true", help='print verbose')

	parser.add_argument('--debug', action="store_true", help='set debug flag')

	args = parser.parse_args()

	verbose = args.verbose
	debug = args.debug

	try :
		X, y = parse(args.dir+'/attributes.xml')
	except IOError :
		print "Directory [",args.dir,"] doesn't contain attributes.xml"
		exit()

	# features = []
	# for item in X :
	# 	features.append(item)
	# for item in y :
	# 	features.append(item)

	try :
		data = pd.read_csv(args.dir+'/data.csv')
	except IOError:
		print "Directory [",args.dir,"] doesn't contain data.csv"
		exit()		

	fuzzy_X = fuzzify(data, X)
	fuzzy_y = fuzzify(data, y)

	if verbose :
		print 'Obtained Fuzzy_X Matrix = ', list(fuzzy_X)
		print 'Obtained Fuzzy_y Matrix = ', list(fuzzy_y)

	from sklearn.linear_model import LinearRegression

	clf = LinearRegression(fit_intercept=False)

	if verbose :
		print '================================'
		print 'Fitting a Linear Model'
		print '================================'

	clf.fit(fuzzy_X, fuzzy_y)

	if verbose :
		print '--------------------------------'
		print 'Weights = ', clf.coef_
		print '--------------------------------'
	
	