package com.github.unchama.seichiassist.listener

import cats.effect.IO
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.seichiskill.SeichiSkillUsageMode.Disabled
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.subsystems.mebius.domain.property.{
  MebiusProperty,
  NormalMebius
}
import com.github.unchama.seichiassist.util.{SendMessageEffect, SendSoundEffect}
import com.github.unchama.targetedeffect.player.{FocusedSoundEffect, PlayerEffects}
import net.coreprotect.model.Config
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.{
  AsyncPlayerPreLoginEvent,
  PlayerChangedWorldEvent,
  PlayerJoinEvent,
  PlayerTeleportEvent
}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.{Bukkit, Location, Material, Sound}

import java.util.UUID
import scala.collection.mutable
import scala.jdk.CollectionConverters._

class PlayerJoinListener extends Listener {
  private val playerMap: mutable.HashMap[UUID, PlayerData] = SeichiAssist.playermap
  private val databaseGateway = SeichiAssist.databaseGateway
  private val failedToLoadDataError =
    "プレーヤーデータの読み込みに失敗しました。再接続しても読み込まれない場合管理者に連絡してください。"

  @EventHandler
  def onPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent): Unit = {
    val maxTryCount = 10

    (1 until maxTryCount + 1).foreach { tryCount =>
      val isLastTry = tryCount == maxTryCount

      try {
        loadPlayerData(event.getUniqueId, event.getName)
        return
      } catch {
        case e: Exception =>
          if (isLastTry) {
            println("Caught exception while loading PlayerData.")
            e.printStackTrace()

            event.setKickMessage(failedToLoadDataError)
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
            return
          }
      }

      // intentional blocking
      Thread.sleep(600)
    }
  }

  private def loadPlayerData(playerUuid: UUID, playerName: String): Unit = {
    SeichiAssist.playermap(playerUuid) =
      databaseGateway.playerDataManipulator.loadPlayerData(playerUuid, playerName)
  }

  // プレイヤーがjoinした時に実行
  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    val player: Player = event.getPlayer

    /*
      サーバー起動してからワールドが読み込まれる前に接続試行をするとAsyncPlayerPreLoginEventが発火されないことがあり、
      そういった場合ではPlayerDataが読み込まれないままここに到達するため、読み込み試行をしてだめだったらキックする。
     */
    if (!playerMap.isDefinedAt(player.getUniqueId)) {
      try {
        loadPlayerData(player.getUniqueId, player.getName)
      } catch {
        case e: Exception =>
          println("Caught exception while loading PlayerData.")
          e.printStackTrace()

          player.kickPlayer(failedToLoadDataError)
          return
      }
    }

    // join時とonenable時、プレイヤーデータを最新の状態に更新
    playerMap(player.getUniqueId).updateOnJoin()

    // 初見さんへの処理
    if (!player.hasPlayedBefore) {
      // 初見さんであることを全体告知
      SendMessageEffect.sendMessageToEveryoneIgnoringPreference(
        s"$LIGHT_PURPLE$BOLD${player.getName}さんはこのサーバーに初めてログインしました！"
      )
      SendMessageEffect.sendMessageToEveryoneIgnoringPreference(
        s"${WHITE}webサイトはもう読みましたか？→$YELLOW${UNDERLINE}https://www.seichi.network/gigantic"
      )
      SendSoundEffect.sendEverySound(Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)

      // 同時に【はじめての方へ】ページに誘導したほうがただWebサイトに誘導するよりまだ可能性がありそう
      // https://github.com/GiganticMinecraft/SeichiAssist/issues/1939
      player.sendTitle(
        s"${YELLOW}ようこそ! ギガンティック☆整地鯖へ!",
        s"${LIGHT_PURPLE}まず初めに公式サイト【はじめての方へ】ページを確認してください",
        10,
        20 * 10, // タイトルの表示時間は10秒
        10
      )
      player.sendMessage(
        s"$YELLOW【はじめての方へ】ページ→ $YELLOW${UNDERLINE}https://www.seichi.network/helloworld"
      )

      import scala.util.chaining._

      // 初見プレイヤーに木の棒、エリトラ、ピッケルを配布
      val inv = player.getInventory
      // 初見プレイヤー向けの Lore (説明文) を設定
      // /stick で入手できる木の棒は、簡略化された説明文にしておく。
      val stickLore = List(
        "この棒を持って右クリックもしくは左クリックするとメニューが開きます。",
        "メニューからはいろんな機能が使えます。試してみよう。",
        "この棒をなくしても /stick コマンドを実行すると再入手できます。",
        "ヒント: もしサーバー内で迷子になったら /spawn コマンドを実行することでいつでも戻れます。"
      )
      val stick = new ItemStack(Material.STICK, 1).tap { itemStack =>
        import itemStack._
        val meta = getItemMeta
        meta.setDisplayName("木の棒メニュー")
        meta.setLore(stickLore.asJava)
        setItemMeta(meta)
      }
      inv.addItem(stick)
      inv.addItem(new ItemStack(Material.ELYTRA))

      val pickaxe = new ItemStack(Material.DIAMOND_PICKAXE)
        // 耐久Ⅲ
        .tap(_.addEnchantment(Enchantment.DURABILITY, 3))
      inv.addItem(pickaxe)
      inv.addItem(new ItemStack(Material.DIAMOND_SPADE))

      inv.addItem(
        new ItemStack(Material.LOG, 64, 0.toShort),
        new ItemStack(Material.LOG, 64, 0.toShort),
        new ItemStack(Material.LOG, 64, 2.toShort),
        new ItemStack(Material.LOG_2, 64, 1.toShort)
      )

      inv.addItem(new ItemStack(Material.BAKED_POTATO, 64))

      inv.addItem(new ItemStack(Material.WRITTEN_BOOK).tap { is =>
        val meta = is.getItemMeta.asInstanceOf[BookMeta]
        // per https://github.com/GiganticMinecraft/SeichiAssist/issues/914#issuecomment-792534164
        // 改行コードを明確にするためにLFで再結合する
        val contents = List(
          """基本的にはこの４つを守ってください。ルール違反をした場合、BANなどの処罰が与えられます。
            |・正規のアカウントを使用する
            |・他人に迷惑をかけない
            |・掘るときは上から綺麗に
            |・サーバーに負荷をかけない
            |""".stripMargin.linesIterator.mkString("\n"),
          """整地の心得
            |
            |整地ワールドでは以下のことを守って整地してください。
            |
            |・下から掘らず、上から掘るべし！
            |・空中にブロックが残らないようにすべし！
            |・水やマグマは除去すべし！
            |・掘りぬいた後は綺麗に整えるべし！
            |""".stripMargin.linesIterator.mkString("\n"),
          """上記、整地の心得に抵触するような掘り方をした場合は、72時間(3日)以内に状態の復旧をお願いします。
            |
            |なお、第1整地ワールドのみ、整地の心得違反による処罰は実施致しません。
            |""".stripMargin.linesIterator.mkString("\n"),
          """この他細かなルールや処罰の具体的な内容はHPをご確認ください。
            |ルールはサーバー内の情勢に応じて予告なく更新されることがあります。
            |その際プレイヤーに個別通知することは致しませんので、ご利用の際にはお手数ですが随時最新のルールをご確認ください。
            |""".stripMargin.linesIterator.mkString("\n")
        )
        contents.foreach(meta.addPage(_))
        meta.setTitle("サーバーに初参加された方にお読みいただきたい本(v1)")
        meta.setAuthor("ギガンティック☆整地鯖")

        is.setItemMeta(meta)
      })

      // メビウスおひとつどうぞ
      inv.setHelmet(
        BukkitMebiusItemStackCodec.materialize(
          // **getDisplayNameは二つ名も含むのでMCIDにはgetNameが適切**
          MebiusProperty
            .initialProperty(NormalMebius, player.getName, player.getUniqueId.toString),
          damageValue = 0.toShort
        )
      )

      /* 期間限定ダイヤ配布.期間終了したので64→32に変更して恒久継続 */
      inv.addItem(new ItemStack(Material.DIAMOND, 32))

      player.sendMessage("初期装備を配布しました。Eキーで確認してネ")

      // 初見さんにメッセージを送信
      player.sendMessage {
        "チュートリアルを受けた後:\n整地鯖では整地をするとレベルが上がり、様々な恩恵が受けられます。\n初めての方はチュートリアルを整地ワールドで掘ってレベルを上げてみましょう！\n木の棒を右クリックしてメニューを開き右上のビーコンボタンをクリック！"
      }

      // 初見プレイヤーをチュートリアル地点へテレポートする
      // 元コードは #992 で、それを一部修正したものに当たる
      // ハードコーディング
      if (SeichiAssist.seichiAssistConfig.getServerNum == 7) {
        val tutorialLocation: Option[Location] = {
          val world = Bukkit.getWorld("world_spawn")
          // ハードコーディング
          Option.when(world ne null)(new Location(world, 582.5, 216.0, 817.5))
        }
        val tpTutorialSuccess = tutorialLocation.fold(false) { loc =>
          player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN)
        }

        if (tpTutorialSuccess) {
          player.sendMessage(s"${YELLOW}チュートリアル地点にテレポートしました。")
          IO {
            import scalikejdbc._
            DB.localTx { _ =>
              sql"""
                     update seichiassist.tutorial set taken = true where uuid = ${player.getUniqueId}
                   """
            }
          }.unsafeRunAsyncAndForget()
        } else {
          player.sendMessage(s"${RED}チュートリアル地点へのテレポートに失敗しました。")
          if (tutorialLocation.isEmpty) {
            SeichiAssist.instance.loggerF.info(s"指定されたワールドはサーバーに存在しません。").unsafeRunSync()
          }
        }
      } else {
        val compute = IO {
          import scalikejdbc._
          DB.readOnly { implicit session =>
            sql"""
                   select taken from seichiassist.tutorial where uuid = ${player.getUniqueId}
                 """.map { rs => rs.boolean("taken_tutorial") }.first().apply().getOrElse(false)
          }
        }

        val needed: Boolean = compute.unsafeRunSync()
        if (needed) {
          player.sendMessage("チュートリアル地点にテレポートします...")
          // ハードコーディング
          PlayerEffects.connectToServerEffect("s7").run(player).unsafeRunSync();
        }
      }
    }

    // 整地専用サーバーの場合は上級者向けのサーバーである旨を通知
    if (SeichiAssist.seichiAssistConfig.getServerNum == 5)
      player.sendTitle(
        s"${WHITE}ここは$BLUE${UNDERLINE}上級者向けのサーバー${WHITE}",
        s"${WHITE}始めたては他がおすすめ",
        10,
        70,
        20
      )
  }

  // プレイヤーがワールドを移動したとき
  @EventHandler
  def onPlayerChangedWorld(event: PlayerChangedWorldEvent): Unit = {
    // 整地ワールドから他のワールドに移動したときに限る
    if (!event.getFrom.isSeichi) return

    val p = event.getPlayer
    val pd = playerMap(p.getUniqueId)

    // coreprotectを切る
    // inspectマップにtrueで登録されている場合
    if (Config.inspecting.getOrDefault(p.getName, false)) {
      // falseに変更する
      p.sendMessage("§3CoreProtect §f- Inspector now disabled.")
      Config.inspecting.put(p.getName, false)
    }

    // アサルトスキルを切る
    val skillState = pd.skillState.get.unsafeRunSync()
    if (skillState.usageMode != Disabled) {
      SeichiAssist
        .instance
        .assaultSkillRoutines(p)
        .stopAnyFiber
        .flatMap(stopped =>
          if (stopped)
            FocusedSoundEffect(Sound.BLOCK_LEVER_CLICK, 1f, 1f).run(p)
          else
            IO.unit
        )
        .unsafeRunSync()
    }
  }
}
