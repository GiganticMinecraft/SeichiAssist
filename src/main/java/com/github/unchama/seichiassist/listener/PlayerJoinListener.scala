package com.github.unchama.seichiassist.listener

class PlayerJoinListener  extends  Listener {
  private val playerMap: HashMap[UUID, PlayerData] = SeichiAssist.playermap
  private val databaseGateway = SeichiAssist.databaseGateway

  private def loadPlayerData(playerUuid: UUID, playerName: String) {
    SeichiAssist.playermap[playerUuid] =
        databaseGateway.playerDataManipulator.loadPlayerData(playerUuid, playerName)
  }

  private val failedToLoadDataError =
      "プレーヤーデータの読み込みに失敗しました。再接続しても読み込まれない場合管理者に連絡してください。"

  @EventHandler
  def onPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent) {
    val maxTryCount = 10
    runBlocking {
      (1 until maxTryCount + 1).forEach { tryCount =>
        val isLastTry = tryCount == maxTryCount

        try {
          loadPlayerData(event.uniqueId, event.name)
          return@runBlocking
        } catch (e: Exception) {
          if (isLastTry) {
            println("Caught exception while loading PlayerData.")
            e.printStackTrace()

            event.kickMessage = failedToLoadDataError
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
            return@runBlocking
          }
        }

        delay(600)
      }
    }
  }

  // プレイヤーがjoinした時に実行
  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent) {
    val player: Player = event.player

    /*
      サーバー起動してからワールドが読み込まれる前に接続試行をするとAsyncPlayerPreLoginEventが発火されないことがあり、
      そういった場合ではPlayerDataが読み込まれないままここに到達するため、読み込み試行をしてだめだったらキックする。
     */
    if (!playerMap.containsKey(player.uniqueId)) {
      try {
        loadPlayerData(player.uniqueId, player.name)
      } catch (e: Exception) {
        println("Caught exception while loading PlayerData.")
        e.printStackTrace()

        player.kickPlayer(failedToLoadDataError)
        return
      }
    }

    run {
      val limitedLoginEvent = LimitedLoginEvent()
      val playerData = playerMap[player.uniqueId]

      //期間限定ログインイベント判別処理
      limitedLoginEvent.getLastcheck(playerData.lastcheckdate)
      limitedLoginEvent.TryGetItem(player)

      // 1周年記念
      if (playerData.anniversary) {
        player.sendMessage("整地サーバー1周年を記念してアイテムを入手出来ます。詳細はwikiをご確認ください。http://seichi.click/wiki/anniversary")
        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      }

      //join時とonenable時、プレイヤーデータを最新の状態に更新
      playerData.updateOnJoin()
    }

    // 初見さんへの処理
    if (!player.hasPlayedBefore()) {
      //初見さんであることを全体告知
      Util.sendEveryMessage(ChatColor.LIGHT_PURPLE.toString() + "" + ChatColor.BOLD + player.name + "さんはこのサーバーに初めてログインしました！")
      Util.sendEveryMessage(ChatColor.WHITE.toString() + "webサイトはもう読みましたか？→" + ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "https://www.seichi.network/gigantic")
      Util.sendEverySound(Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
      //初見プレイヤーに木の棒、エリトラ、ピッケルを配布
      player.inventory.addItem(ItemStack(Material.STICK))
      player.inventory.addItem(ItemStack(Material.ELYTRA))
      player.inventory.addItem(ItemStack(Material.DIAMOND_PICKAXE))
      player.inventory.addItem(ItemStack(Material.DIAMOND_SPADE))

      player.inventory.addItem(ItemStack(Material.LOG, 64, 0.toShort()),
          ItemStack(Material.LOG, 64, 0.toShort()),
          ItemStack(Material.LOG, 64, 2.toShort()),
          ItemStack(Material.LOG_2, 64, 1.toShort()))

      /* 期間限定ダイヤ配布.期間終了したので64→32に変更して恒久継続 */
      player.inventory.addItem(ItemStack(Material.DIAMOND, 32))

      player.sendMessage("初期装備を配布しました。Eキーで確認してネ")
      //メビウスおひとつどうぞ
      MebiusListener.give(player)
      //初見さんにLv1メッセージを送信
      player.sendMessage(SeichiAssist.seichiAssistConfig.getLvMessage(1))
    }

  }

  // プレイヤーがワールドを移動したとき
  @EventHandler
  def onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
    // 整地ワールドから他のワールドに移動したとき
    if (ManagedWorld.fromBukkitWorld(event.from)?.isSeichi == true) {
      val p = event.player
      val pd = playerMap[p.uniqueId]

      // coreprotectを切る
      // inspectマップにtrueで登録されている場合
      if (Config.inspecting[p.name] != null && (Config.inspecting[p.name] == true)) {
        // falseに変更する
        p.sendMessage("§3CoreProtect §f- Inspector now disabled.")
        Config.inspecting[p.name] = false
      }

      // アサルトスキルを切る
      // 現在アサルトスキルorアサルトアーマーを選択中
      if (pd.activeskilldata.assaultnum >= 4 && pd.activeskilldata.assaulttype >= 4) {
        // アクティブスキルがONになっている
        if (pd.activeskilldata.mineflagnum != 0) {
          // メッセージを表示
          p.sendMessage(ChatColor.GOLD.toString() + ActiveSkill.getActiveSkillName(pd.activeskilldata.assaulttype, pd.activeskilldata.assaultnum) + "：OFF")
          // 内部状態をアサルトOFFに変更
          pd.activeskilldata.updateAssaultSkill(p, pd.activeskilldata.assaulttype, pd.activeskilldata.assaultnum, 0)
          // トグル音を鳴らす
          p.playSound(p.location, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
        }
      }
    }
  }
}
