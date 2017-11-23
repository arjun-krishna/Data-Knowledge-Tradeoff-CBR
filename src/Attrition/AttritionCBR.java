import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.model.*;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.*;
import de.dfki.mycbr.core.similarity.config.*;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.Gpr;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Variable;


public class AttritionCBR {

	public static ArrayList<Attrition> cases;
	public static HashMap<String, Attrition> case_map;
	public static FIS fis;

	// Params
	public static boolean DISPLAY_DEFUZZIFICATION = false;
	public static boolean INTERACTIVE_INTERFACE = true;
	public static boolean CASE_DELETION = false;
	public static boolean ACCURACY_CHECK = true;
	public static boolean IS_KNOWLEDGE = true;

	public static void main (String[] args) throws Exception{

		// read dataset
		getCases(args[0]);
		try {
			Project p = new Project();
			
			// Create Concept for term paper
			Concept attrition = p.createTopConcept("Attrition");
			
			int[] min_vals = {18, 102, 1, 1, 1, 1, 1, 1009, 0, 0, 0, 1, 0, 0, 0};
			int[] max_vals = {60, 1499, 29, 4, 4, 5, 4, 19999, 3, 40, 6, 4, 40, 18, 17, 1};
			
			// Add descriptions for the concepts along with their domains. Look for classes of the type *Desc.
			IntegerDesc[] featuredescs = new IntegerDesc[15];
			for(int k=0;k<15;k++){
				featuredescs[k] = new IntegerDesc(attrition, "feature "+k,min_vals[k],max_vals[k]);
			}
			
			// Add a similarity function for the attribute using the add*Fct function. The function types can be specified(refer documentation)
			IntegerFct[] featureFct = new IntegerFct[15];
			for(int k=0;k<15;k++){
				featureFct[k] = featuredescs[k].addIntegerFct("featurefct "+k, true);
				featureFct[k].setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
				featureFct[k].setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			}		
			
			// Create an amalgamation function, to define the global similarity. // Add the amalgamation function for the concept, so that this global similarity is used.
			AmalgamationFct glob_sim = attrition.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM, "Attrition", true);
			glob_sim.setActiveFct(featuredescs[1], featureFct[1]);
			glob_sim.setActiveFct(featuredescs[2], featureFct[2]);
			glob_sim.setActiveFct(featuredescs[7], featureFct[7]);
			glob_sim.setActiveFct(featuredescs[9], featureFct[9]);
			glob_sim.setActiveFct(featuredescs[12], featureFct[12]);

			//Change the weights here.
			glob_sim.setWeight(featuredescs[1],3);
			glob_sim.setWeight(featuredescs[2],2);
			glob_sim.setWeight(featuredescs[7],4);
			glob_sim.setWeight(featuredescs[9],2);
			glob_sim.setWeight(featuredescs[12],2);
			// create casebase
			DefaultCaseBase cb = p.createDefaultCB("myCaseBase");
			// add instances to the casebase
			Instance i;
			for (int j=0; j<cases.size(); j++) {
				i = attrition.addInstance("tp"+j);
				for(int k=0;k<15;k++){
					i.addAttribute(featuredescs[k], cases.get(j).features[k]);
				}
				cb.addCase(i);
				case_map.put("tp"+j, cases.get(j));				
			}
			System.out.println("Added cases to case base");
			// set up query and retrieval. the retrieval method, number of cases to retrieve can be set
			
			Retrieval retriever; 

			// Fuzzy Reasoner Initialization
			fis = FIS.load(args[1], true);
			if( fis == null ) { 
				System.err.println("Can't load file => " + args[1]);
				System.exit(0);
			}

	    // Case Accuracy
	    if (ACCURACY_CHECK) {
	    	double accuracy = 0.0;
	    	double tp = 0.0, tn = 0.0, fp = 0.0, fn = 0.0;
	    	double precision, recall;
	    	for (int j=0; j<cases.size(); j++) {
	    	//for(int j=0;j<100;j++){
	    			//System.out.println(j);
	    			retriever = new Retrieval(attrition, cb);
					retriever.setRetrievalMethod(Retrieval.RetrievalMethod.RETRIEVE_K_SORTED);
					retriever.setK(3);
					Attrition at = cases.get(j);
					int[] features = at.features;

					boolean isAttrition = false;
					
					double attr = fuzzyReason(features, false);
					if(IS_KNOWLEDGE){
						if (attr >= 0.4 && attr <= 0.6) {
							Instance q = retriever.getQueryInstance();
							for(int k=0;k<15;k++){
								q.addAttribute(featuredescs[k].getName(), features[k]);
							}
							retriever.start();
							isAttrition = getPrediction(retriever);

						} else if (attr < 0.4) {
							isAttrition = false;
						} else {
							isAttrition = true;
						}
					}
					else{
									
						Instance q = retriever.getQueryInstance();
							for(int k=0;k<15;k++){
								q.addAttribute(featuredescs[k].getName(), features[k]);
							}
							retriever.start();
							isAttrition = getPrediction(retriever);
					}
					
					if (isAttrition == at.result) {
						accuracy += 1;
					}
					if(at.result){
						if(isAttrition){
							tp += 1;
						}
						else{
							fn += 1;
						}
					}
					else{
						if(isAttrition){
							fp += 1;
						}
						else{
							tn += 1;
						}
					}
				}
				accuracy /= cases.size();
				System.out.println("Accuracy on Case Base = "+String.format("%.02f", accuracy*100)+"%");
				precision = tn/(tn+fn);
				System.out.println("Precision on Case Base = "+String.format("%.02f", precision*100)+"%");
				recall = tn/(tn+fp);
				System.out.println("Recall on Case Base = "+String.format("%.02f", recall*100)+"%");
	    }

			// Case Base Reduction
			if (CASE_DELETION) {
				PrintWriter writer = new PrintWriter("reduced.csv", "UTF-8");
				int j = cases.size()-1;
				while(j>=0) {
					Attrition at = cases.get(j);
					int[] features = at.features;

					double acceptance = fuzzyReason(features, false);

					if (acceptance >= 0.4 && acceptance <= 0.6) {
						for(int k=0;k<15;k++){
							writer.print(features[k]+",");
						}
						writer.println(at.result?1:0);
						cases.remove(j);
						//System.err.println("Case Needed (case number: "+j+", line number: "+(j+2)+", "+acceptance+" )");
					}
					j--;
				}
				writer.close();
			}

			
			/*if (INTERACTIVE_INTERFACE) {
				// Get input
				Scanner in = new Scanner(System.in);

				int days;
				boolean med_cert;
				
				System.out.print("Enter the number of days late: ");
				days = in.nextInt();
				
				System.out.print("Is medical certificate submitted? Enter \"true\" or \"false\": ");
				med_cert = in.nextBoolean();	
				
				Instance q = retriever.getQueryInstance();
				q.addAttribute(daysDesc.getName(), days);
				q.addAttribute(medcertDesc.getName(), med_cert);
				
				int med_int;
				med_int = med_cert? 1 : 0;

				double acceptance = fuzzyReason(days, med_int, DISPLAY_DEFUZZIFICATION);
				System.out.println();

				if(acceptance >= 0.4 && acceptance <= 0.6) {
					retriever.start();
					boolean isAccepted = getPrediction(retriever);
					if(isAccepted) {
						System.out.println("Term paper Accepted (using CB)");
					}
					else {
						System.out.println("Term paper Rejected (using CB)");
					}
				}
				else if(acceptance < 0.4) {
					System.out.println("Term paper Rejected (without using CB)");
				}
				else {
					System.out.println("Term paper Accepted (without using CB)");
				}
			}*/

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static double fuzzyReason(int[] features, boolean display) {

  	FunctionBlock functionBlock = fis.getFunctionBlock(null);
  	
  		functionBlock.setVariable("daily_rate", features[1]);
  		functionBlock.setVariable("monthly_income", features[7]);
  		functionBlock.setVariable("total_working_years", features[9]);
  		functionBlock.setVariable("years_at_company", features[12]);
		functionBlock.setVariable("distance_from_home", features[2]);

		// Evaluate
		functionBlock.evaluate();

		// Get output
		Variable attrition = functionBlock.getVariable("attrition");
		
		// Plot Defuzzification
		JFuzzyChart.get().chart(attrition, attrition.getDefuzzifier(), display);
		
		return functionBlock.getVariable("attrition").getValue();
	}

	private static void print(Retrieval r) {
		for (Map.Entry<Instance, Similarity> entry: r.entrySet()) {
			System.err.println("\nSimilarity: " + entry.getValue().getValue()
					+ " to case: " + entry.getKey().getName());
		}
	}
	
	public static boolean getPrediction(Retrieval r) {
		double acceptance_prob = majorityVote(r);
		if (acceptance_prob >= 0.5) {
			return true;
		} else {
			return false;
		}
	}

	public static double similarityWeighted(Retrieval r) {
		double total = 0.0;
		int num_cases = 0;
		for (Map.Entry<Instance, Similarity> entry: r.entrySet()) {
			if (case_map.get(entry.getKey().getName()).result ) {
				total += entry.getValue().getValue();
			}
			num_cases++;
		}
		double acceptance_prob = total / num_cases;
		return acceptance_prob;
	}


	public static double majorityVote(Retrieval r) {
		int num_true = 0;
		int num_false = 0;
		for (Map.Entry<Instance, Similarity> entry: r.entrySet()) {
			if(case_map.get(entry.getKey().getName()).result) {
				num_true += 1;
			}
			else {
				num_false += 1;
			}
		}
		if(num_true > num_false) {
			return 1.0;
		}
		return 0.0;
	}
	
	public static void getCases(String fileName) throws Exception{
		List<String> lines = Files.readAllLines(Paths.get(fileName));
		cases = new ArrayList<Attrition>();
		case_map = new HashMap<String,Attrition>();
		String[] line_split;
		int[] vals;
		boolean res;
		for(int i=1;i<lines.size();i++) {
			vals = new int[15];
			line_split = lines.get(i).split(",");
			for(int j=0;j<15;j++){
				vals[j] = Integer.parseInt(line_split[j]);
			}
			res = Integer.parseInt(line_split[15]) == 1;
			cases.add(new Attrition(vals,res));
		}
	}
}

class Attrition{
	int[] features;
	boolean result;
	
	Attrition(int[] a, boolean b){
		features = a;
		result = b;
	}
	
	public String toString(){
		String res = "";
		for(int i=0;i<features.length;i++){
			res = res + "Feature "+i+" "+features[i]+"\n";
		}
		res = res + "Result: "+result;
		return res;
	}
}

