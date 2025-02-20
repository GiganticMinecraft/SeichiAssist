package com.github.unchama.seichiassist.infrastructure.akka

import org.apache.pekko.actor.ActorSystem

case class ConfiguredActorSystemProvider(configurationPath: String) {
  // we need to explicitly pass the classloader because
  // Akka cannot find the loader used to load this class
  lazy val classLoader: ClassLoader = getClass.getClassLoader

  def provide(): ActorSystem = {
    org.apache.pekko.actor.ActorSystem(name = "default", classLoader = Some(classLoader))
  }

}
