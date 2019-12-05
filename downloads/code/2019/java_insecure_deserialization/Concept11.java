import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.util.HashMap;
import java.util.Map;

public class Concept11 {
    public static void main(String[] args) {
        try {
            // understanding tiedmapentry
            // HashMap implements the Map interface so we can create Map type object also
            Map<String, Integer> dict = new HashMap<String, Integer>();
            dict.put("asinha", 9);  // add an entry
            dict.put("bob", 2);  // add another entry

            // method definition => decorate(Map map, Transformer factory)
            Map lazymap = LazyMap.decorate(dict, new MyReverse());  // MyReverse implements transformer
            System.out.println(dict);  // inspect the underlying HashMap status: result => both are same
            // lazymap just acts as a wrapper around HashMap,

            String valid_key = "asinha";
            TiedMapEntry tiedmapentry = new TiedMapEntry(lazymap, valid_key);
            System.out.println(tiedmapentry);  // inspect the tiedmapentry object status
            System.out.println(dict);  // inspect the underlying HashMap status

            String invalid_key = "invalid_key";
            TiedMapEntry tme = new TiedMapEntry(lazymap, invalid_key);  // any invalid key invokes transform() method

            int res = tme.hashCode();  // it intern calls tme.getValue()
            System.out.println(res);  // hashcode
            System.out.println(dict);  // a new entry is added to the underlying HashMap

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
