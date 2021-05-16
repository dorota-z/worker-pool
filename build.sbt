scalaVersion := "2.13.5"

organization := "com.pirum"
organizationName := "Pirum Systems"
organizationHomepage := Some(url("https://www.pirum.com"))

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.junit.jupiter" % "junit-jupiter-api" % "5.7.0" % Test,
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.7.0" % Test
)