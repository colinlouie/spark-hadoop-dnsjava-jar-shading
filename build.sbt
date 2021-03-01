name := "spark-hadoop-dnsjava-jar-shading"

version := "1.0.0"

scalaVersion := "2.12.10"
lazy val sparkVersion = "3.0.0"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("org.xbill.**" -> "some.namespace.that.should.never.collide.@0").inAll
)

/** --------------------------------------- */
/** Required for operation of Apache Spark. */
/** --------------------------------------- */

// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % Provided

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % Provided

/** ------------------------- */
/** Required for application. */
/** ------------------------- */

// https://mvnrepository.com/artifact/dnsjava/dnsjava
libraryDependencies += "dnsjava" % "dnsjava" % "3.3.1"
