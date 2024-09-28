---
title: "Demystify a Java gadget chain to exploit insecure deserialization"
date: 2019-11-24 12:19:45 -0800
categories: [web_security]
tags: [java, insecure_deserialization]     # TAG names should always be lowercase
---

Site migration in process .. Drafting notes..


I was always curious about how the actual remote code execution occurs during the Insecure Deserialization process. So I thought of giving a try to understand the known harmful `gadgets` from `commons-collections-3.2.2.jar` and develop the entire chain from scratch.

We need to use `property oriented programming` to build a `RCE gadget` from the scratch.

## Component / Classes Used to build the gadget
1. Default JRE System Libraries: HashMap
2. `commons-collections-3.2.2.jar`:
   1. ChainedTransformer
   2. LazyMap
   3. TiedMapEntry
   4. HashBag

## 1. Command Execution using `Runtime` object directly
We can use the Java `Runtime` object and its `exec()` method to execute any `system` commands.
- for example, running the `mate-calculator` in linux.

![](image%202.png)

## 2. Command Execution using Reflection API and `Runtime` object
* Java `Reflection API` is used to `examine` or `modify` the `behavior` of methods, classes, interfaces at `runtime`.
- Through reflection API, we can invoke any method at `runtime` via `invoke()` function.
  - Here, we are trying to invoke `getRuntime()` method to get a `Runtime` object.

![](image.png)

## 3. Command Execution using `ConstantTransformer` and `InvokerTransformer` together

Before directly jump into the `Constant Transformer` and `InvokerTransformer`, first understand the `Transformer` class and the `transform()` method.

#### Transformer
- It transforms an input object to an output object through `transform()` method.
- It doesn’t change the input object.
- It is mainly used for: `type conversion`, `extracting` parts of an object.

For example, we can create a class `MyReverse` by implementing the `Transformer`interface and `transform()` method.
- Here, in `transform()` method, we specify how to reverse a `String` type object.

![](image%203.png)

When we call the `transform()` method via passing the argument of a `String` type object, it reverses the string.

![](image%204.png)

> The return type of the `transform()` method is `Object` therefore it can return any type of object.

### ConstantTransformer
In contrast to the `Transformer` class, it always returns the `same object` that specified during `initialization`.

- If we Initialize a `ConstantTransformar` with `Runtime.class` and can call the `transform()` method with `any object`(for example, `HashSet`), we will always get the `Runtime.class` type object.

![](image%205.png)

### InvokerTransformer
* During initialization, it takes a `method name` with optional parameters.
* On `transform`, it calls that method for the object provided with the parameters.

![](image%206.png)
