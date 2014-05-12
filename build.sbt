name := "S3cala"

organization := "com.roundeights"

version := "0.1"

scalaVersion := "2.11.0"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature")

publishTo := Some("Spikemark" at "https://spikemark.herokuapp.com/maven/roundeights")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// Application dependencies
libraryDependencies ++= Seq(
    "commons-io" % "commons-io" % "2.4" % "optional",
    "com.amazonaws" % "aws-java-sdk" % "1.7.8.1"
)

