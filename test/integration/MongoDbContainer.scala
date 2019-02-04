package integration

import com.whisk.docker.{DockerContainer, DockerKit, DockerReadyChecker}

trait MongoDbContainer extends DockerKit {

  val MongoDbPort = 27017
  val MongoDbHostName = "localhost"

  val MongodbContainer = DockerContainer("mongo:3.6.7")
    .withHostname(MongoDbHostName)
    .withPorts(MongoDbPort -> Some(MongoDbPort))
    .withReadyChecker(DockerReadyChecker.LogLineContains("waiting for connections on port"))
    .withCommand("mongod", "--smallfiles", "--syncdelay", "0")

  abstract override def dockerContainers: List[DockerContainer] =
    MongodbContainer :: super.dockerContainers
}
