/* @desc : Class to hold attribute
 *
 */

import java.io.*;
import java.util.*;
import java.lang.*;

public class Attribute {
  public String name;
  public List<FuzzySet> sets;

  Attribute(String _name, List<FuzzySet> _sets) {
    name = _name;
    sets = _sets;
  } 

  public void fuzzify(Float val) {
    List<double> fuzzyfied_vector = new ArrayList<double>();
    for (int i = 0; i < sets.size(); i++) {
      fuzzyfied_vector.add(sets.get(i).get_memership(val));
    }
  }

  public void defuzzyify(List<double> activations) {
    
  }
}
