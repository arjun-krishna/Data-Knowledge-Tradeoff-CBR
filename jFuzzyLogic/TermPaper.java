import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.Gpr;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import net.sourceforge.jFuzzyLogic.defuzzifier.Defuzzifier;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfArea;


public class TermPaper {
    public static void main(String[] args) throws Exception {
        
        String fileName = "term_paper.fcl";

        FIS fis = FIS.load(fileName,true);
        
        if( fis == null ) { 
            System.err.println("Can't load file => " + fileName);
            return;
        }

        FunctionBlock functionBlock = fis.getFunctionBlock(null);
        // JFuzzyChart.get().chart(functionBlock);

        // Set inputs
        functionBlock.setVariable("days_late", 3);
        functionBlock.setVariable("medical_certificate", 1);

        // Evaluate
        functionBlock.evaluate();

        // Get output
        Variable acceptance = functionBlock.getVariable("acceptance");
        
        Defuzzifier COA = new DefuzzifierCenterOfArea(acceptance);


        System.out.println("A[COA] = "+COA.defuzzify());


        JFuzzyChart.get().chart(acceptance, acceptance.getDefuzzifier(), true);

        Gpr.debug("low[days_late]: "+ functionBlock.getVariable("days_late").getMembership("low"));
        Gpr.debug("true[medical_certificate]: "+ functionBlock.getVariable("medical_certificate").getMembership("true"));

        // System.out.println(functionBlock);
        System.out.println("Acceptance : "+ functionBlock.getVariable("acceptance").getValue());
        System.out.println("A[MAX] = "+acceptance.getUniverseMax());
        System.out.println("A[MIN] = "+acceptance.getUniverseMin());
    }
}

// http://jfuzzylogic.sourceforge.net/html/manual.html#runfcl
