name := """Notificator"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"


routesGenerator := InjectedRoutesGenerator

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
	"com.typesafe.play" %% "play-slick" % "3.0.0",
	"com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
	"mysql" % "mysql-connector-java" % "6.0.6",
	"com.h2database" % "h2" % "1.4.196",
	"org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % "test",
	"com.typesafe.play" %% "play-json" % "2.6.3",
	"be.objectify" %% "deadbolt-scala" % "2.6.0",
	specs2 % Test
)


libraryDependencies += guice
libraryDependencies += ehcache
libraryDependencies += jcache

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

fork in run := true