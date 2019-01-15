package integration

import com.whisk.docker.{DockerContainer, DockerKit, DockerReadyChecker}

trait MongoDbContainer extends DockerKit {

  val MongoDbPort = 27017

  val mongodbContainer = DockerContainer("mongo:3.6.7")
    .withPorts(MongoDbPort -> None)
    .withReadyChecker(DockerReadyChecker.LogLineContains("waiting for connections on port"))
    .withCommand("mongod", "--nojournal", "--smallfiles", "--syncdelay", "0")

  abstract override def dockerContainers: List[DockerContainer] =
    mongodbContainer :: super.dockerContainers
}
