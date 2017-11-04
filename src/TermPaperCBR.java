import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
//import de.dfki.mycbr.core.casebase.Case;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.model.*;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.*;
import de.dfki.mycbr.core.similarity.config.*;

public class TermPaperCBR{

	public static ArrayList<TermPaper> cases;
	public static HashMap<String,TermPaper> case_map;

	public static void main (String[] args) throws Exception{
		getCases(args[0]);
		//System.out.println(cases);
		try {
			Project p = new Project();
			// Create Concept for term paper
			Concept termpaper = p.createTopConcept("TermPaper");
			
			// Add descriptions for the concepts along with their domains. Look for classes of the type *Desc.
			IntegerDesc daysDesc = new IntegerDesc(termpaper,"days_late",0,20);
			BooleanDesc medcertDesc = new BooleanDesc(termpaper,"med_cert");
			
			// Add a similarity function for the attribute using the add*Fct function. The function types can be specified(refer documentation)
			IntegerFct dayFct = daysDesc.addIntegerFct("dayfct",true);
			dayFct.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			dayFct.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			
			// For the boolean variable, the similarity is 0-1.
			
			//System.out.println("Param: "+dayFct.getFunctionParameterL());	
			
			// Create an amalgamation function, to define the global similarity. // Add the amalgamation function for the concept, so that this global similarity is used.
			AmalgamationFct glob_sim = termpaper.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM,"TermPaper",true);
			glob_sim.setActiveFct(daysDesc,dayFct);
			//Change the weights here.
			glob_sim.setWeight(daysDesc,3);
			glob_sim.setWeight(medcertDesc,2);
			//System.out.println(glob_sim.getWeight(daysDesc));
			//System.out.println(glob_sim.getWeight(medcertDesc));
			

			// add casebase
			
			DefaultCaseBase cb = p.createDefaultCB("myCaseBase");
			
			// add instances to the casebase
			Instance i;
			for(int j=0;j<cases.size();j++){
				i = termpaper.addInstance("tp"+j);
				i.addAttribute(daysDesc,cases.get(j).days_late);
				i.addAttribute(medcertDesc,cases.get(j).med_cert);
				cb.addCase(i);
				case_map.put("tp"+j,cases.get(j));				
			}
			
			// set up query and retrieval. the retrieval method, number of cases to retrieve can be set
			
			Retrieval r = new Retrieval(termpaper,cb);
			r.setRetrievalMethod(Retrieval.RetrievalMethod.RETRIEVE_K_SORTED);
			r.setK(3);
			Instance q = r.getQueryInstance();
			
			// Get input
			Scanner in = new Scanner(System.in);
			int days;
			boolean med_cert;
			System.out.print("Enter the number of days late: ");
			days = in.nextInt();
			System.out.print("Is medical certificate submitted? Enter \"true\" or \"false\": ");
			med_cert = in.nextBoolean();	
			q.addAttribute(daysDesc.getName(),days);
			q.addAttribute(medcertDesc.getName(),med_cert);
			
			// Use knowledge from adaptation container
			float output;
			output = adaptSolution(days, med_cert);
			
			// If knowledge from adaptation container is not sufficient, i.e. the output is in the fuzzy region, use the knowledge from the case base container.
			if(output>=0.4&&output<=0.6){
				r.start();

				//print(r);
				boolean isAccepted = getPrediction(r);
				if(isAccepted){
					System.out.println("Term paper Accepted(using CB)");
				}
				else{
					System.out.println("Term paper Rejected(using CB)");
				}
			
			}
			else if(output < 0.4){
				System.out.println("Term paper Rejected(without using CB)");
			}
			else{
				System.out.println("Term paper Accepted(without using CB)");
			}	

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static void print(Retrieval r) {
		for (Map.Entry<Instance, Similarity> entry: r.entrySet()) {
			System.out.println("\nSimilarity: " + entry.getValue().getValue()
					+ " to case: " + entry.getKey().getName());
		}
	}
	
	public static boolean getPrediction(Retrieval r){
		int num_true = 0;
		int num_false = 0;
		for (Map.Entry<Instance, Similarity> entry: r.entrySet()) {
			if(case_map.get(entry.getKey().getName()).accepted){
				num_true += 1;
			}
			else{
				num_false += 1;
			}
		}
		if(num_true > num_false){
			return true;
		}
		return false;
	}
	
	public static void getCases(String fileName) throws Exception{
		List<String> lines = Files.readAllLines(Paths.get(fileName));
		cases = new ArrayList<TermPaper>();
		case_map = new HashMap<String,TermPaper>();
		String[] line_split;
		int days;
		boolean med, acc;
		for(int i=1;i<lines.size();i++){
			line_split = lines.get(i).split(",");
			days = Integer.parseInt(line_split[0]);
			if(line_split[1].equals("False")){
				med = false;
			}
			else{
				med = true;
			}
			if(line_split[2].equals("False")){
				acc = false;
			}
			else{
				acc = true;
			}
			cases.add(new TermPaper(days,med,acc));
		}
	}
	
	public static float adaptSolution(int days_late, boolean med_cert){
		float[] fuzzy_in = fuzzifyQuery(days_late,med_cert);
		for(int i=0;i<fuzzy_in.length;i++){
			System.out.print(fuzzy_in[i]+" ");
		} 
		System.out.println();
		float fuzzy_out = fuzzy_reason(fuzzy_in);
		return fuzzy_out;
	}
	
	public static float[] fuzzifyQuery(int days_late, boolean med_cert){
		float[] arr = new float[5];
		float[] fuzzify_params = {0,3,0,3,4,10,1,3,12,15,2,2}; // Params are left boundary, right boundary, left width, right width
		for(int i=0;i<3;i++){
			if(days_late<fuzzify_params[i*4]-fuzzify_params[i*4+2]){
				arr[i] = 0;
			}
			else if(days_late<fuzzify_params[i*4]){
				arr[i] = ((float)(days_late-(fuzzify_params[i*4]-fuzzify_params[i*4+2])))/(fuzzify_params[i*4+2]);
			}
			else if(days_late<fuzzify_params[i*4+1]){
				arr[i] = 1;
			}
			else if(days_late<fuzzify_params[i*4+1]+fuzzify_params[i*4+3]){
				arr[i] = ((float)((fuzzify_params[i*4+1]+fuzzify_params[i*4+3]))-days_late)/(fuzzify_params[i*4+3]);
			}	
			else{
				arr[i] = 0;
			}	
		}
		if(med_cert){
			arr[3] = 1;
			arr[4] = 0;
		}
		else{
			arr[3] = 0;
			arr[4] = 1;
		}
		return arr;
	}
	
	public static float fuzzy_reason(float[] input){
		float[] params = {1.0f,0.45f,0.03f,0.76f,0.3f};
		float[] weights = {0.5f,0.5f};
		float[] vals ={0.0f,0.0f};
		float val=0.0f;
		for(int i=0;i<3;i++){
			//vals[0] = Math.max(vals[0],Math.min(params[i],input[i]));
			vals[0] += params[i]*input[i];
		}
		for(int i=3;i<5;i++){
			//vals[1] = Math.max(vals[1],Math.min(params[i],input[i]));
			vals[1] += params[i]*input[i];
		}
		
		for(int i=0;i<vals.length;i++){
			val += weights[i]*vals[i];
		}
		System.out.println(val);
		return val;
	}
}

class TermPaper{
	int days_late;
	boolean med_cert, accepted;
	
	TermPaper(int a, boolean b, boolean c){
		days_late = a;
		med_cert = b;
		accepted = c;
	}
	
	public String toString(){
		return "Late by "+days_late+" days, medical certificate: "+med_cert+" acceptance: "+accepted;
	}
}

