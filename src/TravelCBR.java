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

public class TravelCBR{

	public static ArrayList<Travel> cases;
	public static HashMap<String,Travel> case_map;

	public static void main (String[] args) throws Exception{
		getCases(args[0]);
		/*for(int i=0;i<cases.size();i++){
			System.out.println(cases.get(i));
		}
		System.out.println(cases.size());*/
		//System.out.println(cases);
		try {
			Project p = new Project();
			// Create Concept for term paper
			Concept travel = p.createTopConcept("Travel");
			
			// Add descriptions for the concepts along with their domains. Look for classes of the type *Desc.
			HashSet<String> holiday_types = new HashSet<String>();
			String[] holidayArray = { "City", "Bathing", "Language", "Recreation", "Skiing", "Active", "Education", "Wandering" };
			holiday_types.addAll(Arrays.asList(holidayArray));
			SymbolDesc holidayTypeDesc = new SymbolDesc(travel,"holidayType",holiday_types);
			// FloatDesc priceDesc = new FloatDesc(travel,"price",0,8000); //Price is an output and hence not a descriptor
			IntegerDesc numPeopleDesc = new IntegerDesc(travel,"num_people",1,12);
			BooleanDesc carDesc = new BooleanDesc(travel,"isCar");
			BooleanDesc trainDesc = new BooleanDesc(travel,"isTrain");
			BooleanDesc coachDesc = new BooleanDesc(travel,"isCoach");
			BooleanDesc planeDesc = new BooleanDesc(travel,"isPlane");
			IntegerDesc durationDesc = new IntegerDesc(travel,"duration",1,25);
			IntegerDesc seasonDesc = new IntegerDesc(travel,"season",0,12);
			IntegerDesc accommodationDesc = new IntegerDesc(travel,"accomodation",1,6);
			
			// Add a similarity function for the attribute using the add*Fct function. The function types can be specified(refer documentation)
			
			// Similarity for number of people
			IntegerFct numPeopleFct = numPeopleDesc.addIntegerFct("numPeoplefct",true);
			numPeopleFct.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			numPeopleFct.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			System.out.println(numPeopleFct.getFunctionParameterR());
			//numPeopleFct.setFunctionParameterR(0.0);
			//numPeopleFct.setFunctionParameterL(0.0);
			
			// Similarity for duration
			IntegerFct durationFct = durationDesc.addIntegerFct("durationfct",true);
			durationFct.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			durationFct.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			
			// Similarity for season
			IntegerFct seasonFct = seasonDesc.addIntegerFct("seasonfct",true);
			seasonFct.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			seasonFct.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			
			// Similarity for accommodation
			IntegerFct accommodationFct = accommodationDesc.addIntegerFct("accommodationfct",true);
			accommodationFct.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
			accommodationFct.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
			
			// For the boolean variable, the similarity is 0-1.
			
			//System.out.println("Param: "+dayFct.getFunctionParameterL());	
			
			// Create an amalgamation function, to define the global similarity. // Add the amalgamation function for the concept, so that this global similarity is used.
			AmalgamationFct glob_sim = travel.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM,"TermPaper",true);
			glob_sim.setActiveFct(numPeopleDesc, numPeopleFct);
			glob_sim.setActiveFct(durationDesc, durationFct);
			glob_sim.setActiveFct(seasonDesc, seasonFct);
			glob_sim.setActiveFct(accommodationDesc, accommodationFct);
			//Change the weights here.
			glob_sim.setWeight(holidayTypeDesc,1);
			glob_sim.setWeight(numPeopleDesc,4);
			glob_sim.setWeight(carDesc,1);
			glob_sim.setWeight(trainDesc,1);
			glob_sim.setWeight(coachDesc,1);
			glob_sim.setWeight(planeDesc,1);
			glob_sim.setWeight(durationDesc,2);
			glob_sim.setWeight(seasonDesc,1);
			glob_sim.setWeight(accommodationDesc,2);
			//System.out.println(glob_sim.getWeight(daysDesc));
			//System.out.println(glob_sim.getWeight(medcertDesc));
			

			// add casebase
			
			DefaultCaseBase cb = p.createDefaultCB("travelCaseBase");
			
			// add instances to the casebase
			Instance i;
			for(int j=0;j<cases.size();j++){
				i = travel.addInstance("tp"+j);
				i.addAttribute(holidayTypeDesc,cases.get(j).holidayType);
				i.addAttribute(numPeopleDesc,cases.get(j).num_people);
				i.addAttribute(carDesc,cases.get(j).isCar);
				i.addAttribute(trainDesc,cases.get(j).isTrain);
				i.addAttribute(coachDesc,cases.get(j).isCoach);
				i.addAttribute(planeDesc,cases.get(j).isPlane);
				i.addAttribute(durationDesc,cases.get(j).duration);
				i.addAttribute(seasonDesc,cases.get(j).season);
				i.addAttribute(accommodationDesc,cases.get(j).accommodation);
				cb.addCase(i);
				case_map.put("tp"+j,cases.get(j));				
			}
			
			// set up query and retrieval. the retrieval method, number of cases to retrieve can be set
			
			// TODO from here
			Retrieval r = new Retrieval(travel,cb);
			r.setRetrievalMethod(Retrieval.RetrievalMethod.RETRIEVE_K_SORTED);
			r.setK(10);
			Instance q = r.getQueryInstance();
			
			// Get input
			Scanner in = new Scanner(System.in);
			/*int days;
			boolean med_cert;
			System.out.print("Enter the number of days late: ");
			days = in.nextInt();
			System.out.print("Is medical certificate submitted? Enter \"true\" or \"false\": ");
			med_cert = in.nextBoolean();*/	
			q.addAttribute(holidayTypeDesc.getName(),holidayTypeDesc.getAttribute("Recreation"));
			q.addAttribute(numPeopleDesc.getName(),10);
			q.addAttribute(carDesc.getName(),true);
			q.addAttribute(trainDesc.getName(),false);
			q.addAttribute(coachDesc.getName(),false);
			q.addAttribute(planeDesc.getName(),false);
			q.addAttribute(durationDesc.getName(),14);
			q.addAttribute(seasonDesc.getName(),3);
			q.addAttribute(accommodationDesc.getName(),2);
		
			// Use knowledge from adaptation container, fuzzy reasoning is actually some sort of vocabulary
			/*double output;
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
			}*/	
			r.start();

			print(r);
			float price = getPrediction(r);
			/*boolean isAccepted = getPrediction(r);
			if(isAccepted){
				System.out.println("\nTerm paper Accepted(using CB)");
			}
			else{
				System.out.println("\nTerm paper Rejected(using CB)");
			}*/
			System.out.println("The expected price is: "+price);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static void print(Retrieval r) {
		for (Map.Entry<Instance, Similarity> entry: r.entrySet()) {
			System.out.println("Similarity: " + entry.getValue().getValue()
					+ " to case: " + entry.getKey().getName()+" Price: "+case_map.get(entry.getKey().getName()).price+"\n"+case_map.get(entry.getKey().getName()));
		}
	}
	
	public static float getPrediction(Retrieval r){
		float total_sim = 0.0f, total_price = 0.0f, sim;
		for (Map.Entry<Instance, Similarity> entry: r.entrySet()) {
			sim = (float)entry.getValue().getValue();
			total_sim += sim;
			total_price += sim * case_map.get(entry.getKey().getName()).price;
		}
		return total_price/total_sim;
	}
	
	public static void getCases(String fileName) throws Exception{
		List<String> lines = Files.readAllLines(Paths.get(fileName));
		cases = new ArrayList<Travel>();
		case_map = new HashMap<String,Travel>();
		String[] line_split;
		
		String holidayType;
		float price; 
		int num_people, duration, accommodation, season;
		boolean isCar, isPlane, isCoach, isTrain;
		
		for(int i=1;i<lines.size();i++){
			line_split = lines.get(i).split(",");
			holidayType = line_split[0];
			price = Float.parseFloat(line_split[1]);
			num_people = (int)Float.parseFloat(line_split[2]);
			isCar = Integer.parseInt(line_split[4]) == 1;
			isTrain = Integer.parseInt(line_split[5]) == 1;
			isCoach = Integer.parseInt(line_split[6]) == 1;
			isPlane = Integer.parseInt(line_split[7]) == 1;
			duration = (int)Float.parseFloat(line_split[8]);
			season = Integer.parseInt(line_split[9]);
			accommodation = Integer.parseInt(line_split[10]);
			
			cases.add(new Travel(holidayType,price,num_people,isCar,isTrain,isCoach,isPlane,duration,season,accommodation));
		}
	}
}

class Travel{
	String holidayType;
	float price; // Required to be output
	int num_people, duration, accommodation, season;
	boolean isCar, isPlane, isCoach, isTrain;
	
	Travel(String a, float b, int c, boolean d, boolean e, boolean f, boolean g, int h, int i, int j){
		holidayType = a;
		price = b;
		num_people = c;
		isCar = d;
		isTrain = e;
		isCoach = f;
		isPlane = g;
		duration = h;
		season = i;
		accommodation = j;		
	}
	
	public String toString(){
		return "Holiday Type: "+holidayType+"\nNumber of People: "+num_people+"\nisCar: "+isCar+"\nisPlane: "+isPlane+"\nisCoach: "+isCoach+"\nisTrain: "+isTrain+"\nDuration: "+duration+"\nSeason: "+season+"\nAccommodation: "+accommodation;
	}
}

