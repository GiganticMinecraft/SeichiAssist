package com.github.unchama.util.nms.v1_12_2.world

import cats.effect.{Concurrent, Sync}


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
        val method = clazz.getDeclaredMethod("getChunkAt", Integer.TYPE, Integer.TYPE)
        receiver => {
          case (x, z) => method.invoke(receiver, x, z)
        }
      }

      // public boolean method(int, int)
      // originally
      // protected boolean method(int, int, boolean)
      lazy val isChunkLoaded: AnyRef => (Int, Int) => Boolean = {
        val method = clazz.getDeclaredMethod("isChunkLoaded", Integer.TYPE, Integer.TYPE, java.lang.Boolean.TYPE)

        method.setAccessible(true)

        receiver => {
          case (x, z) => method.invoke(receiver, x, z, true).asInstanceOf[java.lang.Boolean]
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
   * In a running minecraft server, there is an internal queue which is used in controlling and limiting chunk saves.
   *
   * This action, when running, relaxes the save-queue throttle.
   * The returned action completes when there are no more chunks to be saved.
   */
  def relaxFileIOThreadThrottle[F[_]](implicit F: Concurrent[F]): F[Unit] = Sync[F].delay {
    FileIOThread.relaxThrottle(FileIOThread.instance)()
  }

  def flushEntityRemovalQueue[F[_]](world: org.bukkit.World)(implicit F: Sync[F]): F[Unit] = F.delay {
    val nmsWorldServer = CraftWorld.nmsWorld(world)
    val removalQueueAlias = World.entityRemovalQueue(nmsWorldServer)

    World.entityList(nmsWorldServer).removeAll(removalQueueAlias)

    removalQueueAlias.asScala.foreach { entity =>
      val entityChunkX = Entity.chunkX(entity)
      val entityChunkZ = Entity.chunkZ(entity)

      if (Entity.loadedToAChunk(entity) && World.isChunkLoaded(nmsWorldServer)(entityChunkX, entityChunkZ)) {
        val chunk = World.getChunkAtCoordinate(world)(entityChunkX, entityChunkZ)

        Chunk.untrackEntity(chunk)(entity)
      }

      World.untrackEntity(nmsWorldServer)(entity)
    }

    removalQueueAlias.clear()
  }
}
