# Spark Hadoop dnsjava JAR shading

## Problem statement

Duplicate classes on the class path.

Let's suppose your project uses _Library A_ and _Library B_. _Library B_ just happens to pull in _Library A_ but a
different version of it. No matter if the difference is in the _MAJOR_, _MINOR_, or _PATCH_ version, problems may arise.

Reference (for versioning): https://semver.org

```
Your Project -> Library B v1.1.0 -> Library A v2.1.0
    |
    v
Library A v1.2.0
```

Without shading (to be explained below), the version of Library A used by both _Your Project_ and _Library B_ is
now ambiguous.

Specifically, let's suppose your _Apache Spark_ based project needs to use _dnsjava_.

At the time of writing (2021-02-19), the following Apache Spark 3.0.2 options are available:
- Pre-built for Apache Hadoop 2.7
- Pre-built for Apache Hadoop 3.2 and later
- Pre-built with user-provided Apache Hadoop

Apache Hadoop versions 2.7 through 3.3.0-RC0 inclusive, use dnsjava 2.1.7 without shading it.

Reference: https://spark.apache.org/

This means unless your project also uses this specific version, you may run into issues. This is where _shading_ comes
in.

## Solution

Shading JARs allows your project to run its own copy of dependencies. Here's how it works:
- Renames the namespace (package name)
- Rewrites your project's references to the renamed namespace

### Renaming

`build.sbt` contains a complete working example (that builds!). Here are the relevant lines specific to _dnsjava_.

```
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("org.xbill.**" -> "some.namespace.that.should.never.collide.@0").inAll
)
```

### Writing your code

You write your code as normal.

e.g.
```
val resolver = new org.xbill.DNS.SimpleResolver()
```

When you run `sbt assembly`, it will rewrite the references directly in the bytecode to:

```
val resolver = new some.namespace.that.should.never.collide.org.xbill.DNS.SimpleResolver()
```

### Verification

If shading worked properly, you should see something like this:

```
shelL$ sbt assembly

shell$ cd target/scala-2.12/

shell$ mkdir tmp && cd tmp && unzip ../spark-hadoop-dnsjava-jar-shading-assembly-1.0.0.jar

shell$ find . -type d -name "xbill*"
./some/namespace/that/should/never/collide/org/xbill
```

If not, it would look like this:

```
shell$ find . -type d -name "xbill*"
./org/xbill
```
