name := "highperfscala"
organization := "highperfscala"

val scalazVersion = "7.2.2"
val specsVersion = "3.8.4"

val slf4s = "org.slf4s" %% "slf4s-api" % "1.7.12"
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.0"
val hdrHistogram = "org.mpierce.metrics.reservoir" % "hdrhistogram-metrics-reservoir" % "1.1.0"
val scalaz = "org.scalaz" %% "scalaz-core" % scalazVersion
val scalaConcurrent = "org.scalaz" %% "scalaz-concurrent" % scalazVersion
val specs2 = "org.specs2" %% "specs2-core" % specsVersion
val specs2ScalaCheck = "org.specs2" %% "specs2-scalacheck" % specsVersion
val joda = "joda-time" % "joda-time" % "2.9.4"
val jodaConvert = "org.joda" % "joda-convert" % "1.8"


val baseOptions = Defaults.coreDefaultSettings ++ Seq(
  scalaVersion := "2.11.8",
  fork := true,
  resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  libraryDependencies ++= List(
    slf4s, scalaCheck, hdrHistogram, scalaz, joda, jodaConvert,
    specs2 % "test", specs2ScalaCheck % "test"
  )
)

lazy val root = Project(
  id = "highperfscala",
  base = file("."),
  settings = baseOptions ++ Seq(
    onLoadMessage ~= (_ + (if (sys.props("java.specification.version") != "1.8") {
      """
        |You seem to not be running Java 1.8.
        |While the provided code may still work, we recommend that you
        |upgrade your version of Java.
      """.stripMargin
    } else "")))
) aggregate(
  chapter2
)
  	
lazy val chapter2 = Project(
  id = "chapter2",
  base = file("chapter2"),
  settings = baseOptions
).enablePlugins(JmhPlugin) 