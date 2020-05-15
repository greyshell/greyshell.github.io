import org.apache.commons.collections.map.LazyMap;

import java.util.HashMap;
import java.util.Map;

public class Concept09 {
    public static void main(String[] args) {
        try {
            // understanding LazyMap
            // HashMap implements the Map interface so we can create Map type object also
            Map<String, Integer> dict = new HashMap<String, Integer>();
            dict.put("asinha", 9);  // add an entry
            dict.put("bob", 2);  // add another entry

            // method definition => decorate(Map map, Transformer factory)
            Map lazymap = LazyMap.decorate(dict, new MyReverse());  // MyReverse implements transformer

            System.out.println(lazymap);  // inspect  the lazymap object status
            System.out.println(dict);  // inspect the underlying HashMap status: result => both are same
            // lazymap just acts as a wrapper around HashMap,
            // when no key is found then it generates the value and update HashMap with that new entry

            System.out.println(lazymap.get("asinha"));  // search with a valid key
            System.out.println(dict); // check the underlying Map status, no entry is added

            // invalid key => invokes the transform() method to generate a value through transform() method
            String out = (String) lazymap.get("alice");  // transform() method will be called
            System.out.println(out);
            System.out.println(dict);  // new entry has been added into the underlying HashMap

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
