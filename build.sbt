name := "premier-league-api"

version := "1.0"

lazy val `premier-league-api` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  ws,
  guice,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.16.0-play26",
  specs2 % Test
)

routesImport += "binders._"

unmanagedResourceDirectories in Test += baseDirectory(_ / "target/web/public/test").value
