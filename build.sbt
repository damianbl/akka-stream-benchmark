name := "akka-quickstart-scala"

version := "1.1"

scalaVersion := "2.11.11"

lazy val akkaVersion = "2.5.3"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "local-central" at "http://192.168.1.100:8081/nexus/content/repositories/releases/"

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

externalResolvers <<= resolvers map { rs =>
  Resolver.withDefaultResolvers(rs, mavenCentral = true)
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.dblazejewski" %% "common-model" % "1.0.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.storm-enroute" % "scalameter-core_2.11" % "0.8.2"
)

publishArtifact in Test := false
publishMavenStyle := true
pomIncludeRepository := { _ => false }
pomExtra := <url>http://blog.dblazejewski.com</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/MIT</url>
    </license>
  </licenses>
  <scm>
    <connection></connection>
    <developerConnection></developerConnection>
    <url></url>
  </scm>
  <developers>
    <developer>
      <id></id>
      <name></name>
      <url></url>
    </developer>
  </developers>

publishTo := Some("nexus" at "http://192.168.1.100:8081/nexus/content/repositories/releases/")
