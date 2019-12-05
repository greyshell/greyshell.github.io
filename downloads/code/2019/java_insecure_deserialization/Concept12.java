import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.util.HashMap;
import java.util.Map;

public class Concept12 {
    public static void main(String[] args) {
        try {
            // understanding HashBag
            Map dict = new HashMap();
            dict.put("asinha", 9);
            Map lazymap = LazyMap.decorate(dict, new MyReverse());  // chainedTransformer.transform() will be called

            String invalid_key = "invalid_key";

            // Create a TiedMapEntry with the underlying map as our `lazyMap` and an invalid key
            TiedMapEntry tiedmapentry = new TiedMapEntry(lazymap, invalid_key);

            HashBag b = new HashBag();
            b.add(tiedmapentry);

            System.out.println(dict);  // a new entry is added to the underlying HashMap of tiedmapentry object
            // format => count: object
            System.out.println(b);  // key = tiedmapentry(it's HashMap is updated), value = count

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
