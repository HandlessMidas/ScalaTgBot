name := "ScalaTgBot"

version := "0.1"

scalaVersion := "2.12.9"

libraryDependencies ++= Seq(
  "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
  "com.softwaremill.sttp" %% "json4s" % "1.7.2",
  "org.json4s" %% "json4s-native" % "3.6.0",
  "org.scalamock" %% "scalamock" % "4.4.0" % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)
