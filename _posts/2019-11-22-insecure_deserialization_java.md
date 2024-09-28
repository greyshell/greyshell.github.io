---
title: "Insecure Deserialization in Java"
date: 2019-11-22 22:17:55 -0800
categories: [web_security]
tags: [java, insecure_deserialization]     # TAG names should always be lowercase
---

I was always curious about how the actual remote code execution occurs during the Insecure Deserialization process. So I thought of giving a try to understand the known harmful `gadgets` from `commons-collections-3.2.2.jar` and develop the entire chain from scratch.

<!-- more -->

## Serialization

The process of converting the `state` of object into stream of bytes is called `serialization`.

The purpose of serialization is to save the object’s state to the file system or transmit it over the network for future use.

### Serialization in Java

> - `Serializable` is a `marker interface`.
> - It has no `data member` and `method`.
> - It is only used to `mark` java classes so that objects of these type of classes may get a certain `capability`.


Create a `User` class and make it `serializable`.
