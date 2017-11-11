/* @desc : Class to hold the Fuzzy Set of an Attribute
 *
 */

import java.io.*;
import java.util.*;
import java.lang.*;

public class FuzzySet {
  public String name;
  public Float value;
  public Boolean is_fuzzy;
  public HashMap<String, Float> membership_function;

  // Constructor for a CRISP Set
  FuzzySet(String _name, Float _value) {                       
    name = _name;
    value = _value;
    is_fuzzy = false;
  }

  // Constructor for a Fuzzy Set [Specifying the memership function parameters]
  FuzzySet(String _name, Float _value, Float l, Float u, Float dl, Float du) {
    name = _name;
    value = _value;
    is_fuzzy = true;

    membership_function = new HashMap<String, Float>();
    membership_function.put("l", l);
    membership_function.put("dl", dl);
    membership_function.put("u", u);
    membership_function.put("du", du);
  }

  public double get_memership(Float val) {
    if (is_fuzzy) {                        // Need to evaluate Memership to the Fuzzy Set

      int compare_dl = membership_function.get("dl").compareTo(val);    // ?(dl > val)
      int compare_l  = membership_function.get("l").compareTo(val);     // ?( l > val)
      int compare_u  = membership_function.get("u").compareTo(val);     // ?( u > val)
      int compare_du = membership_function.get("du").compareTo(val);    // ?(du > val)

      // Middle Region
      if (compare_l <= 0 && compare_u >= 0) {
        return 1.0;
      }

      // Out of the region of the memership function
      if (compare_dl >= 0 || compare_du <= 0) {
        return 0.0;
      }

      // Left Triangle
      if (compare_dl < 0 && compare_l > 0) {
        Float dl = membership_function.get("dl");
        Float l  = membership_function.get("l");

        return (val - dl) / (l - dl);
      }

      // Right Triangle
      if (compare_u < 0 && compare_du > 0) {
        Float du = membership_function.get("du");
        Float u  = membership_function.get("u");

        return (val - du) / (u - du);
      }


    } else {                               // This is CRISP Set
      if (value.compareTo(val) == 0) {
        return 1.0;
      } else {
        return 0.0;
      }
    }
    return 0.0;  // Exec will never come here! [Java needed me to put this return!]
  }
}


/*

Assumption
===========
@ Memership Function is modeled as a Trapezoid 
           __________
          /|        |\
         / |        | \
        /  |        |  \
_______/   |        |   \__________
      dl   l        u   du 

*/