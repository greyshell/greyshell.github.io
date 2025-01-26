---
title: "Demystify a Java gadget chain to exploit insecure deserialization"
date: 2019-11-24 12:19:45 -0800
categories: [web_security]
tags: [java, insecure_deserialization]     # TAG names should always be lowercase
---

I was always curious about how the actual remote code execution occurs during the Insecure Deserialization process. So I thought of giving a try to understand the known harmful `gadgets` from `commons-collections-3.2.2.jar` and develop the entire chain from scratch.

We need to use `property oriented programming` to build a `RCE gadget` from the scratch.

## Component / Classes used to build the gadget

1. Default JRE System Libraries: HashMap
2. `commons-collections-3.2.2.jar`:
   1. ChainedTransformer
   2. LazyMap
   3. TiedMapEntry
   4. HashBag
3. `commons-lang3-3.7.jar`
4. `mockito-all-1.9.5.jar`

> Make sure to add all those external libraries into Java class PATH (i.e Pycharm File -> Project Structure -> Libraries)
{: .prompt-info }

## Pieces of the Puzzle

![gadget_flow](assets/2019-11-24-demystify_java_gadget_chain.assets/gadget_flow.png)


## Command Execution using `Runtime` object directly

We can use the Java `Runtime` object and its `exec()` method to execute any `system` commands.
- for example, running the `gnome-calculator` in linux.

![rce01](assets/2019-11-24-demystify_java_gadget_chain.assets/rce01.png)

## Command Execution using Reflection API and `Runtime` object

* Java `Reflection API` is used to `examine` or `modify` the `behavior` of methods, classes, interfaces at `runtime`.
- Through reflection API, we can invoke any method at `runtime` via `invoke()` function.
  - Here, we are trying to invoke `getRuntime()` method to get a `Runtime` object.

![rce02](assets/2019-11-24-demystify_java_gadget_chain.assets/rce02.png)


Before directly jump into the `Constant Transformer` and `InvokerTransformer`, first understand the `Transformer` class and the `transform()` method.

## Concept of Transformer

- It transforms an input object to an output object through `transform()` method.
- It doesn’t change the input object.
- It is mainly used for: `type conversion`, `extracting` parts of an object.

For example, we can create a class `MyReverse` by implementing the `Transformer`interface and `transform()` method.
- Here, in `transform()` method, we specify how to reverse a `String` type object.

When we call the `transform()` method via passing the argument of a `String` type object, it reverses the string.

![concept_transformer](assets/2019-11-24-demystify_java_gadget_chain.assets/concept_transformer.png)


The return type of the `transform()` method is `Object` therefore it can return any type of object.

## Command Execution using ConstantTransformer

In contrast to the `Transformer` class, it always returns the `same object` that specified during `initialization`.

- If we Initialize a `ConstantTransformar` with `Runtime.class` and can call the `transform()` method with `any object`(for example, `HashSet`), we will always get the `Runtime.class` type object.

![rce03](assets/2019-11-24-demystify_java_gadget_chain.assets/rce03.png)

## Concept of InvokerTransformer

* During initialization, it takes a `method name` with optional parameters.
* On `transform`, it calls that method for the object provided with the parameters.

![concept_invoker_transformer](assets/2019-11-24-demystify_java_gadget_chain.assets/concept_invoker_transformer.png)


## Command Execution combining Transformer, ConstantTransformer and InvokerTransformer

We can chain all three types of transformers and perform RCE.

![rce04](assets/2019-11-24-demystify_java_gadget_chain.assets/rce04.png)


## Command Execution using ChainedTransformer

`ChainedTransformer` is an array of transformers. By carefully maintaining their execution order we can achieve the same result but with minimum amount of code.

![rce05](assets/2019-11-24-demystify_java_gadget_chain.assets/rce05.png)

## Concept HashMap

> **public** **class** HashMap<K,V> **extends** AbstractMap<K,V> **implements** Map<K,V>, Cloneable, Serializable

1. HashMap class contains `values` based on the `key`.
2. It contains only unique `keys`.
3. It maintains no order.
4. It returns `null` if there’s `no` value is present for the requested `key`.

![concept_hashmap](assets/2019-11-24-demystify_java_gadget_chain.assets/concept_hashmap.png)

## Concept LazyMap

1. A type of `Map` which `creates` a `value` if there’s `no` value is present for the `requested` key.
2. This `generation` is done through a `transformation` (i.e transformer.`transform()` method) on the requested `Key`.
3. When the request key is not found then `lazyMap.get("invalid_key")` calls `transformer.transform("invalid_key")` to generate the key on the fly.

![concept_lazymap1](assets/2019-11-24-demystify_java_gadget_chain.assets/concept_lazymap1.png)

![concept_lazymap2](assets/2019-11-24-demystify_java_gadget_chain.assets/concept_lazymap2.png)

## Command Execution by combining ChainedTransformer and LazyMap

![rce06](assets/2019-11-24-demystify_java_gadget_chain.assets/rce06.png)


## Concept TiedMapEntry

1. This can be used to `enable` a `Map` entry to `make changes` on the `underlying` map.
2. Key point to remember `tiedmapentry.hashcode()` method calls `tiedmapentry.getValue()` method then intern it finally calls `lazymap.get(this.key)` method.

![concept_tidemapentry1](assets/2019-11-24-demystify_java_gadget_chain.assets/concept_tidemapentry1.png)

![concept_tidemapentry2](assets/2019-11-24-demystify_java_gadget_chain.assets/concept_tidemapentry2.png)

## Concept HashBag

1. A Collection that `counts` the number of times an object `appears` in the collection.
2. Backed by an internal `HashMap` object.
3. While adding any Object, first it's `hashcode()` is calculated. Based on that hashcode/index, it updates the underlying `HashMap` table entry.
4. Key Point to remember `hashbag.add(tiedmapentry)` method calls `tiedmapentry.hashcode()` method.

![concept_hashbag](assets/2019-11-24-demystify_java_gadget_chain.assets/concept_hashbag.png)

## Assembling the pieces

1. Create a `TiedMapEntry` with a underlying `lazyMap` and key is `String` -> 'invalid_key'.
2. Then `add` the `tiedmapentry` Object into a `HashBag` instance.
3. `hashbag.add(tiedmapentry)` -> `tiedmapentry.hashcode()` -> `lazymap.get(this.key)` -> `chainedtransformer.transform(key)` -> `Runtime.getRuntime().exec("/usr/bin/gnome-calculator");`


### Challenges

- Our primary objective for during the deserialization process is to ensure that `TiedMapEntry.hashcode()` gets invoked first time only.
- However, when we attempt to serialize the HashBag object to generate the payload or .ser file, the method lazymap.get("invalid_key") is invoked once. This causes the underlying HashMap to be updated with the entry `invalid_key:derived_value`.
- As a result, during the deserialization process, when `TiedMapEntry.hashcode()` triggers the call `lazymap.get(this.key)`, the `ChainedTransformer.transform(key)` method will not be executed. This is because the LazyMap no longer needs to derive the value for the key using the `transform()` method—the underlying HashMap already contains the precomputed entry.

> Therefore, we need to find a way to prevent the `TiedMapEntry.hashcode()` method from being invoked while creating the exploit payload.
{: .prompt-warning }

### Resolve using mockito

1. Create a `HashBag` instance and add any `Object` into it.
2. This will invoke Object’s `hashcode()` method and based on the hashcode / index, the underlying `HashBag's` => `HashMap` table entry will be updated with `key = Object` and `value / count = 1`.
3. Now using `mokito` library, modify that `HashBag's` => `HashMap’s` `first` entry in `memory`.
   - Replace that `Object` with `TiedMapEntry` Object.
   - We have added only one entry due to that we are modifying the `first` entry.
4. As you can observe, till this point, `TiedMapEntry.hashcode()` is not called anywhere.
5. Serialize this `HashBag` Object.

An exception might still occur while creating the serialized object, as serialization support is now disabled by default for `org.apache.commons.collections.functors.InvokerTransformer`.

Therefore enable the support in both producer and consumer programs to serialize / deserialize the object without any exception

```java

System.setProperty(
                    "org.apache.commons.collections.enableUnsafeSerialization",
                    "true");
```

## Prepare Final Exploit

Before finalizing the exploit payload, we need to include support for the Mockito library.

> Java’s **strong encapsulation** introduced in **Java 9+**, which restricts reflective access to certain internal Java classes and fields by default. This is especially relevant when using libraries or tools that attempt to access private or internal fields of classes like `HashMap`.
{: .prompt-danger }

- [x] By adding the `--add-opens` option, we can explicitly open the necessary package (`java.util`) for reflection. In IntelliJ IDEA -> Run -> Edit Configurations -> In **VM options** field, add the following

```text
--add-opens java.base/java.util=ALL-UNNAMED
```

![add_vm_options](assets/2019-11-24-demystify_java_gadget_chain.assets/add_vm_options.png)

![rce07_1](assets/2019-11-24-demystify_java_gadget_chain.assets/rce07_1.png)

![rce07_2](assets/2019-11-24-demystify_java_gadget_chain.assets/rce07_2.png)

It was observed that the calculator did not open during object creation because `TiedMapEntry.hashcode()` was not invoked at any point.

### Test Exploit

When the client deserialization code attempts to process the .ser file, the calculator opens.

![rce07_3](assets/2019-11-24-demystify_java_gadget_chain.assets/rce07_3.png)

## References
- Code Repo: <https://github.com/greyshell/rce_gadget/tree/main/src>
- <https://speakerdeck.com/dhavalkapil/magichat-insomnihack-teaser-2018-writeup>
