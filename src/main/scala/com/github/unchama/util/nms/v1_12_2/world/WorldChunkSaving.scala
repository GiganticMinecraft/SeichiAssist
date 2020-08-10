package com.github.unchama.util.nms.v1_12_2.world

import cats.effect.{Concurrent, Sync}
import com.github.unchama.util.nms.v1_12_2.world.WorldChunkSaving.Reflection.World.clazz
import org.slf4j.Logger


object WorldChunkSaving {

  import scala.jdk.CollectionConverters._

  private object Reflection {
    private val nmsPackage_1_12_R1 = "net.minecraft.server.v1_12_R1"
    private val craftBukkitPackage_1_12_R1 = "org.bukkit.craftbukkit.v1_12_R1"

    object FileIOThread {
      private[Reflection] lazy val clazz: Class[_] = Class.forName(s"$nmsPackage_1_12_R1.FileIOThread")

      // public static FileIOThread method()
      lazy val getInstance: () => AnyRef = {
        val method = clazz.getDeclaredMethod("a")

        () => method.invoke(null)
      }

      lazy val instance: AnyRef = getInstance()

      // public void method()
      lazy val relaxThrottle: AnyRef => () => Unit = {
        val method = clazz.getDeclaredMethod("b")
        receiver => () => method.invoke(receiver)
      }

      // public void method()
      // originally
      // private void method()
      lazy val forceLoopThroughSavers: AnyRef => () => Unit = {
        val method = instance.getClass.getDeclaredMethod("c")

        method.setAccessible(true)

        receiver => () => method.invoke(receiver)
      }
    }

    object Entity {
      private[Reflection] lazy val clazz: Class[_] = Class.forName(s"$nmsPackage_1_12_R1.Entity")

      // public int field
      lazy val chunkX: AnyRef => Int = {
        val field = clazz.getDeclaredField("ab")
        field.getInt(_)
      }

      // public int field
      lazy val chunkZ: AnyRef => Int = {
        val field = clazz.getDeclaredField("ad")
        field.getInt(_)
      }

      // public boolean field
      lazy val loadedToAChunk: AnyRef => Boolean = {
        // NOTE: it is not clear what this field is doing, but given that this field is used in
        // switching chunk processing, it seems to indicate if the entity is loaded into a chunk
        val field = clazz.getDeclaredField("aa")
        field.getBoolean(_)
      }
    }

    object Chunk {
      private[Reflection] lazy val clazz: Class[_] = Class.forName(s"$nmsPackage_1_12_R1.Chunk")

      // public void method(Entity)
      lazy val untrackEntity: AnyRef => AnyRef => Unit = {
        val method = clazz.getDeclaredMethod("b", Entity.clazz)
        receiver => entity => method.invoke(receiver, entity)
      }
    }

    object World {
      private[Reflection] lazy val clazz: Class[_] = Class.forName(s"$nmsPackage_1_12_R1.World")

      // public final List<Entity> field
      lazy val entityList: AnyRef => java.util.List[_ <: AnyRef] = {
        val field = clazz.getDeclaredField("entityList")
        receiver => field.get(receiver).asInstanceOf[java.util.List[_ <: AnyRef]]
      }

      // public final List<Entity> field
      // originally
      // protected final List<Entity> field
      lazy val entityRemovalQueue: AnyRef => java.util.List[_ <: AnyRef] = {
        val field = clazz.getDeclaredField("f")
        field.setAccessible(true)
        receiver => field.get(receiver).asInstanceOf[java.util.List[_ <: AnyRef]]
      }

      // public void method(Entity)
      // originally
      // protected void method(Entity)
      lazy val untrackEntity: AnyRef => AnyRef => Unit = {
        val method = clazz.getDeclaredMethod("c", Entity.clazz)

        method.setAccessible(true)

        receiver => entity => method.invoke(receiver, entity)
      }

      // public Chunk method(int, int)
      lazy val getChunkAtCoordinate: AnyRef => (Int, Int) => AnyRef = {
        val method = clazz.getDeclaredMethod("getChunkAt", classOf[Integer], classOf[Integer])
        receiver => {
          case (x, z) => method.invoke(receiver, x, z)
        }
      }
    }

    object WorldServer {
      private[Reflection] lazy val clazz: Class[_] = Class.forName(s"$nmsPackage_1_12_R1.WorldServer")

      // public boolean method(int, int)
      // originally
      // protected boolean method(int, int, boolean)
      lazy val isChunkLoaded: AnyRef => (Int, Int) => Boolean = {
        val method = clazz.getDeclaredMethod("isChunkLoaded", classOf[Integer], classOf[Integer], classOf[java.lang.Boolean])
        method.setAccessible(true)
        receiver => {
          case (x, z) => method.invoke(receiver, x, z, true).asInstanceOf[Boolean]
        }
      }

    }

    object CraftWorld {
      private[Reflection] lazy val clazz: Class[_] = Class.forName(s"$craftBukkitPackage_1_12_R1.CraftWorld")

      // public final nms.WorldServer (<: nms.World)
      // originally
      // private final nms.WorldServer
      lazy val nmsWorld: org.bukkit.World => AnyRef = {
        val field = clazz.getDeclaredField("world")

        field.setAccessible(true)

        receiver => field.get(receiver)
      }
    }

  }

  import Reflection._

  /**
   * FileIOThread is a gateway object to handle chunk saves,
   * but it has an internal flag to not let chunks save in a mass.
   *
   * This action, when running, relaxes the throttle.
   * This action completes when there are no more chunks to be saved.
   */
  private def relaxFileIOThreadThrottle[F[_] : Sync]: F[Unit] = Sync[F].delay {
    FileIOThread.relaxThrottle(FileIOThread.instance)()
  }

  /**
   * FileIOThread has an internal list of `IAsyncChunkSaver`s,
   * each of which has a single method to pop its internal queue and proceed to saving a chunk.
   *
   * This action loops through the current list of `IAsyncChunkSaver`s and
   * invokes `IAsyncChunkSaver`s' pop-and-process method.
   */
  private def forceFileIOThreadLoopThroughSavers[F[_] : Sync]: F[Unit] = Sync[F].delay {
    FileIOThread.instance.synchronized {
      FileIOThread.forceLoopThroughSavers(FileIOThread.instance)()
    }
  }

  import cats.implicits._

  /**
   * In a running minecraft server, there is an internal queue which is used in controlling and limiting chunk saves.
   *
   * When chunk load happens a lot in a very short period of time,
   * default chunk unloading may be too slow, throttled by the internal queue,
   * hence there is a danger of OutOfMemoryError being thrown.
   *
   * This action is helpful in such a situation; it starts a fiber,
   * within which any unloaded unsaved chunks will be forced to be saved.
   */
  def flushChunkSaverQueue[F[_]](implicit F: Concurrent[F], logger: Logger): F[Unit] = {
    F.delay(println("Save queue flushing started..."))
  } >> {
    F.race(
      relaxFileIOThreadThrottle[F],
      F.foreverM(forceFileIOThreadLoopThroughSavers)
    ).as(())
  } >> {
    F.delay(println("Save queue flushing done!"))
  }

  def flushEntityRemovalQueue[F[_]](world: org.bukkit.World)(implicit F: Sync[F]): F[Unit] = F.delay {
    val nmsWorldServer = CraftWorld.nmsWorld(world)
    val removalQueueAlias = World.entityRemovalQueue(nmsWorldServer)

    World.entityList(nmsWorldServer).removeAll(removalQueueAlias)

    removalQueueAlias.asScala.foreach { entity =>
      val entityChunkX = Entity.chunkX(entity)
      val entityChunkZ = Entity.chunkZ(entity)

      if (Entity.loadedToAChunk(entity) && WorldServer.isChunkLoaded(nmsWorldServer)(entityChunkX, entityChunkZ)) {
        val chunk = World.getChunkAtCoordinate(world)(entityChunkX, entityChunkZ)

        Chunk.untrackEntity(chunk)(entity)
      }

      World.untrackEntity(nmsWorldServer)
    }

    removalQueueAlias.clear()
  }
}
