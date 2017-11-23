import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.Gpr;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class Attrition {
    public static void main(String[] args) throws Exception {
        
        String fileName = "attrition.fcl";

        FIS fis = FIS.load(fileName,true);
        
        if( fis == null ) { 
            System.err.println("Can't load file => " + fileName);
            return;
        }

        FunctionBlock functionBlock = fis.getFunctionBlock(null);
        // JFuzzyChart.get().chart(functionBlock);

        // Set inputs
        functionBlock.setVariable("daily_rate", 200);
        functionBlock.setVariable("monthly_income", 2000);
        functionBlock.setVariable("total_working_years", 5);
        functionBlock.setVariable("years_at_company", 2);
        functionBlock.setVariable("distance_from_home", 18);

        // Evaluate
        functionBlock.evaluate();

        // Get output
        Variable attrition = functionBlock.getVariable("attrition");
        JFuzzyChart.get().chart(attrition, attrition.getDefuzzifier(), true);

        //Gpr.debug("low[days_late]: "+ functionBlock.getVariable("days_late").getMembership("low"));
        //Gpr.debug("true[medical_certificate]: "+ functionBlock.getVariable("medical_certificate").getMembership("true"));

        // System.out.println(functionBlock);
        System.out.println("Attrition : "+ functionBlock.getVariable("attrition").getValue());
    }
}
