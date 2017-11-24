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


public class TermPaperCBR {

  public static ArrayList<TermPaper> cases;
  public static HashMap<String,TermPaper> case_map;
  public static FIS fis;

  // Params
  public static boolean DISPLAY_DEFUZZIFICATION = true;
  public static boolean INTERACTIVE_INTERFACE = true;
  public static boolean CASE_DELETION = false;
  public static boolean ACCURACY_CHECK = false;

  public static void main (String[] args) throws Exception{

    // read dataset
    getCases(args[0]);
  
    try {
      Project p = new Project();
      
      // Create Concept for term paper
      Concept termpaper = p.createTopConcept("TermPaper");
      
      // Add descriptions for the concepts along with their domains. Look for classes of the type *Desc.
      IntegerDesc daysDesc = new IntegerDesc(termpaper, "days_late", 0, 20);
      BooleanDesc medcertDesc = new BooleanDesc(termpaper, "med_cert");
      
      // Add a similarity function for the attribute using the add*Fct function. The function types can be specified(refer documentation)
      IntegerFct dayFct = daysDesc.addIntegerFct("dayfct", true);
      dayFct.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
      dayFct.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
      
      // For the boolean variable, the similarity is 0-1.
      
      // Create an amalgamation function, to define the global similarity. // Add the amalgamation function for the concept, so that this global similarity is used.
      AmalgamationFct glob_sim = termpaper.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM, "TermPaper", true);
      glob_sim.setActiveFct(daysDesc, dayFct);

      //Change the weights here.
      glob_sim.setWeight(daysDesc,3);
      glob_sim.setWeight(medcertDesc,2);

      // create casebase
      DefaultCaseBase cb = p.createDefaultCB("myCaseBase");
      
      // add instances to the casebase
      Instance i;
      for (int j=0; j<cases.size(); j++) {
        i = termpaper.addInstance("tp"+j);
        i.addAttribute(daysDesc, cases.get(j).days_late);
        i.addAttribute(medcertDesc, cases.get(j).med_cert);
        cb.addCase(i);
        case_map.put("tp"+j, cases.get(j));       
      }
      
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
        for (int j=0; j<cases.size(); j++) {
          TermPaper tp = cases.get(j);
          int days_late = tp.days_late;
          int med_cert = tp.med_cert ? 1 : 0;
          retriever = new Retrieval(termpaper, cb);
          retriever.setRetrievalMethod(Retrieval.RetrievalMethod.RETRIEVE_K_SORTED);
          retriever.setK(3);
          boolean isAccepted = false;

          double acceptance = fuzzyReason(days_late, med_cert, false);

          if (acceptance >= 0.4 && acceptance <= 0.6) {
            Instance q = retriever.getQueryInstance();
            q.addAttribute(daysDesc.getName(), days_late);
            q.addAttribute(medcertDesc.getName(), med_cert);

            retriever.start();
            isAccepted = getPrediction(retriever);

          } else if (acceptance < 0.4) {
            isAccepted = false;
          } else {
            isAccepted = true;
          }
          if (isAccepted == tp.accepted) {
            accuracy += 1;
          }
        }
        accuracy /= cases.size();
        System.out.println("Accuracy on Case Base = "+String.format("%.02f", accuracy*100)+"%");
      }

      // Case Base Reduction
      if (CASE_DELETION) {
        for (int j=0; j<cases.size(); j++) {
          TermPaper tp = cases.get(j);
          int days_late = tp.days_late;
          int med_cert = tp.med_cert ? 1 : 0;

          double acceptance = fuzzyReason(days_late, med_cert, false);

          if (acceptance >= 0.4 && acceptance <= 0.6) {
            System.err.println("Case Needed ("+days_late+" , "+med_cert+" , "+acceptance+" )");
          }
        }
      }

      
      if (INTERACTIVE_INTERFACE) {
        // Get input
        Scanner in = new Scanner(System.in);

        int days;
        boolean med_cert;
        
        System.out.print("Enter the number of days late: ");
        days = in.nextInt();
        
        System.out.print("Is medical certificate submitted? Enter \"true\" or \"false\": ");
        med_cert = in.nextBoolean();  
        retriever = new Retrieval(termpaper, cb);
        retriever.setRetrievalMethod(Retrieval.RetrievalMethod.RETRIEVE_K_SORTED);
        retriever.setK(3);
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
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  
  public static double fuzzyReason(int days, int med_cert, boolean display) {

    FunctionBlock functionBlock = fis.getFunctionBlock(null);
    
    functionBlock.setVariable("days_late", days);
    functionBlock.setVariable("medical_certificate", med_cert);

    // Evaluate
    functionBlock.evaluate();

    if (display)
      JFuzzyChart.get().chart(functionBlock);

    // Get output
    Variable acceptance = functionBlock.getVariable("acceptance");
    
    // Plot Defuzzification
    JFuzzyChart.get().chart(acceptance, acceptance.getDefuzzifier(), display);
    
    return functionBlock.getVariable("acceptance").getValue();
  }

  private static void print(Retrieval r) {
    for (Map.Entry<Instance, Similarity> entry: r.entrySet()) {
      System.err.println("\nSimilarity: " + entry.getValue().getValue()
          + " to case: " + entry.getKey().getName());
    }
  }
  
  public static boolean getPrediction(Retrieval r) {
    double acceptance_prob = similarityWeighted(r);
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
      if (case_map.get(entry.getKey().getName()).accepted ) {
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
      if(case_map.get(entry.getKey().getName()).accepted) {
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
    cases = new ArrayList<TermPaper>();
    case_map = new HashMap<String,TermPaper>();
    String[] line_split;
    int days;
    boolean med, acc;
    for(int i=1;i<lines.size();i++) {
      line_split = lines.get(i).split(",");
      days = Integer.parseInt(line_split[0]);
      if(line_split[1].equals("False")) {
        med = false;
      }
      else {
        med = true;
      }
      if(line_split[2].equals("False")) {
        acc = false;
      }
      else {
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

