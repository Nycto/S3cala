name := "S3cala"

organization := "com.roundeights"

version := "0.1"

scalaVersion := "2.10.2"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature")

// Application dependencies
libraryDependencies ++= Seq(
    "commons-io" % "commons-io" % "2.4" % "optional",
    "com.amazonaws" % "aws-java-sdk" % "1.4.1"
)

