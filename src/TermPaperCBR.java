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
			r.setK(10);
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
			
			r.start();

			//print(r);
			boolean isAccepted = getPrediction(r);
			if(isAccepted){
				System.out.println("Term paper Accepted");
			}
			else{
				System.out.println("Term paper Rejected");
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

