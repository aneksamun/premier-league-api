import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerExposedPorts
import com.typesafe.sbt.packager.docker.ExecCmd

name := "premier-league-api"

version := "1.0"

lazy val `premier-league-api` = (project in file("."))
  .enablePlugins(PlayScala, DockerPlugin, AshScriptPlugin)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  ws,
  guice,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.16.0-play26",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test, 
  "org.mockito" % "mockito-core" % "2.23.0" % Test,
  "com.whisk" %% "docker-testkit-scalatest" % "0.9.8" % Test,
  "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.8" % Test
)

routesImport += "binders._"

unmanagedResourceDirectories in Test += baseDirectory(_ / "target/web/public/test").value

javaOptions in Universal ++= Seq(
  "-J-Xmx1024m",
  "-J-Xms128m",
  s"-Dpidfile.path=/opt/docker/${packageName.value}/run/play.pid"
)

dockerCommands ++= Seq(
  ExecCmd("RUN", "mkdir", s"/opt/docker/${packageName.value}"),
  ExecCmd("RUN", "mkdir", s"/opt/docker/${packageName.value}/run"),
  ExecCmd("RUN", "chown", "-R", "daemon:daemon", s"/opt/docker/${packageName.value}/")
)

bashScriptExtraDefines := List(
  """addJava "-Duser.dir=$(realpath "$(cd "${app_home}/.."; pwd -P)")""""
)

dockerBaseImage := "openjdk:8u171-alpine3.8"
dockerExposedPorts in Docker := Seq(9000, 9000)
