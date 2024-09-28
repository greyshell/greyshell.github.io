---
title: "Insecure Deserialization in Java"
date: 2019-11-22 22:17:55 -0800
categories: [web_security]
tags: [java, insecure_deserialization]     # TAG names should always be lowercase
---

## Concept of Serialization

The process of converting the `state` of object into stream of bytes is called serialization.

The purpose of serialization is to save the object’s state to the file system or transmit it over the network for future use.

### In the context of Java

> - `Serializable` is a `marker interface`.
> - It has no `data member` and `method`.
> - It is only used to `mark` java classes so that objects of these type of classes may get a certain `capability`.
{: .prompt-info }


Create a `User` class and make it `serializable`.

Create an object from the `User` class and save it into the file system in `.ser` format.

![serialized_object](assets/2019-11-22-insecure_deserialization_java.assets/serialized_object.png)

## Concept of Deserialization

The process of `re-creating` the actual object in memory from byte stream is called de-serialization.

> The `User` class must be available in the Java classpath for deserialization to succeed.
{: .prompt-warning }


![deserialized_object](assets/2019-11-22-insecure_deserialization_java.assets/deserialized_object.png)

### Observations

- [x] For instance, if a serialized object is created using the `User` class but type checking during deserialization is performed with the `SuperUser` class, then application will throw a `ClassCastException`.

![consume_superclass](assets/2019-11-22-insecure_deserialization_java.assets/consume_superclass.png)

- [x] However, if a serialized object is created using the `SuperUser` class but type checking during deserialization is performed with the `User` class, the application will not throw any exception because `SuperUser` class is derived from the base class `User`.

![consume_baseclass](assets/2019-11-22-insecure_deserialization_java.assets/consume_baseclass.png)

- [x] Some objects may be required to implement `Serializable` due to inheritance for example `SuperUser`. It inherites the base class `User` that implements `Serializable`.

To ensure that such objects (e.g., `SuperUser`) cannot be deserialized, we can override the `readObject()` method and mark it as final to throw an exception during the deserialization process.

![stop_deserialization_using_final](assets/2019-11-22-insecure_deserialization_java.assets/stop_deserialization_using_final.png)

## The Bug

1. The readObject method of `java.io.ObjectInputStream` is vulnerable.

2. During the Deserialization process, the `readObject()` method is always being called, and it can construct any sort of Serializable object that can be found on the Java classpath before passing it back to the caller for the type_check.

3. An Exception occurs only when there’s a type mismatch between the returned object and the expected object. If the constructed object performs any harmful actions during its construction, it’s already too late to prevent them by the time type checking.


##  How to Identify

From a Blackbox perspective
1. Look for magic numbers like `AC ED 00 05` or `rO0A` (base64-encoded) in the request/response to identify if the application is handling a serialized object.

2. The `Content-Type` header in the HTTP response is set to `application/x-java-serialized-object`.

⠀
From a Whitebox perspective
1. Search the codebase for Java Serialization APIs such as `ObjectInputStream`, particularly instances of `readObject()` method, and analyze how `ObjectInputStream` is utilized.

2. Before calling `readObject()`, ensure the code checks for all expected classes from the serialized object using a `whitelist`.


## What is the Impact

1. Remote code execution through `property-oriented programming` or gadget chaining.

2. Bypass authorization or escalate privileges via Insecure Direct Object Reference (IDOR) if the object’s signature / authenticity is not verified.

3. Denial of Service (DoS) attacks, such as exhausting heap memory, CPU cycle.


## How to Exploit

### Denial of Service

1. Generate a malicious serialized object.

2. During deserialization, when the application attempts to reconstruct the object in memory, it consumes 100% of the CPU resources.

![dos_deserialization](assets/2019-11-22-insecure_deserialization_java.assets/dos_deserialization.png)

### Remote Code Execution

TBD

## How to Mitigate

1. Do not blindly accept serialized objects from untrusted sources. Implement integrity checks or sign the serialized objects to prevent tampering or the creation of malicious objects.

2. Use a whitelist approach to secure `java.io.ObjectInputStream`
    - Create a `HashSet` containing all expected classes wrapped in the object.
    - Extend `ObjectInputStream` to create a custom `SafeObjectInputStream` class.
    - Override the `resolveClass()` method to verify if `cls.getName()` exists in the `HashSet`, otherwise, throw an `InvalidClassException`.

When we provide any object other than `User` type, it throws exception.

![bad_object](assets/2019-11-22-insecure_deserialization_java.assets/bad_object.png)


When we provide `User` type object, it does not throw any exception.

![good_object](assets/2019-11-22-insecure_deserialization_java.assets/good_object.png)

> A Denial of Service (DoS) is inevitable if the `expected` object type is a `HashSet`, `HashMap`, or `ArrayList`.
{: .prompt-danger }



Defense in depth
1. Use the `transient` keyword for sensitive fields that you do not want to be serialized. The `transient` keyword prevents a variable, like a password field, from being serialized. When the JVM encounters a variable marked as transient or `static`, it disregards its original value and instead saves the default value corresponding to that variable’s data type.

2. For detective controls, log any exceptions or failures that occur during the deserialization process.


3. Use Java Security Manager to block specific classes such as `InvokerTransformer`.

```java
// in current Java, by default enableUnsafeSerialization is set to 'false'
System.setProperty(
        "org.apache.commons.collections.enableUnsafeSerialization",
        "false");
```


## Code Repo

https://github.com/greyshell/java_insecure_deserialization
