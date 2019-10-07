package com.github.unchama.seichiassist.task;

import com.github.unchama.seichiassist.SeichiAssist;
import com.github.unchama.seichiassist.listener.MebiusListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * 2分に1回呼び出される
 * 定型Tipsを喋ってsilence true or 喋らずsilence false
 * PlayerDataに実体
 *
 * @author CrossHearts
 */
public class MebiusTask extends BukkitRunnable {
    private Player p;
    private boolean silence = false;

    // プレイヤー接続時に呼び出される
    public MebiusTask(UUID uuid) {
        p = Bukkit.getPlayer(uuid);
        if (MebiusListener.isEquip(p)) {
            speak("おかえり" + Objects.requireNonNull(MebiusListener.getNickname(p)) + "！待ってたよ！");
        }
        runTaskTimerAsynchronously(SeichiAssist.instance(), 2400, 2400);
    }

    // 2分周期で呼び出される
    @Override
    public void run() {
        // 前回喋って2分経過によりお喋り解禁
        silence = false;
        // Tipsを呼び出し
        MebiusListener.callTips(p);
    }

    // silence OFFかつ50%でmessageを喋って、silence trueにする
    public void speak(String message) {
        // 50%乱数
        boolean isSpeak = new Random().nextBoolean();
        if (!silence && isSpeak) {
            // 引数のメッセージを表示
            String name = MebiusListener.getName(p.getInventory().getHelmet());
            playSe();
            p.sendMessage(ChatColor.RESET + "<" + name + ChatColor.RESET + "> " + message);
            // 次タスクまでお喋り禁止
            silence = true;
        }
    }

    // 無条件で喋らせる
    public void speakForce(String message) {
        // 引数のメッセージを表示
        String name = MebiusListener.getName(p.getInventory().getHelmet());
        playSeForce();
        p.sendMessage(ChatColor.RESET + "<" + name + ChatColor.RESET + "> " + message);
    }

    // 喋る時の効果音
    // HARPちゃんは聞こえんのだよ…
    private void playSe() {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.0f);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.0f);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.0f);
        Bukkit.getServer().getScheduler().runTaskLater(SeichiAssist.instance(), () -> {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f);
        }, 2);
    }

    // 強制時の効果音
    // HARPちゃんは聞こえんのだよ…
    private void playSeForce() {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f);
        Bukkit.getServer().getScheduler().runTaskLater(SeichiAssist.instance(), () -> {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f);
        }, 2);
    }
}
