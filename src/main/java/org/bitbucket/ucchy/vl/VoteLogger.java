/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package org.bitbucket.ucchy.vl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

/**
 * Vote Logger
 * @author ucchy
 */
public class VoteLogger extends JavaPlugin implements Listener {

    private static final String HEADER_STATS = "#Votifierが受信した時刻, 投票記録数";
    private static final String NOTIFY_PERMISSION = "votelogger.notify";

    private static VoteLogger instance;

    private SimpleDateFormat dformat;

    private FileLogger fileLogger;
    private HashMap<String, Integer> stats;
    private VoteLoggerConfig config;

    /**
     * プラグインが有効化されたときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // 各種初期化
        instance = this;
        dformat = new SimpleDateFormat("yyyy-MM-dd");
        config = new VoteLoggerConfig();

        // データフォルダが存在しないなら、この時点で作成しておく
        File dir = getDataFolder();
        if ( !dir.exists() || !dir.isDirectory() ) {
            dir.mkdirs();
        }

        // リスナー登録
        getServer().getPluginManager().registerEvents(this, this);

        // 投票ログファイルの作成
        fileLogger = new FileLogger(getDataFolder(), "votes.csv");

        // 統計の取得と記録
        stats = fileLogger.readStats();
        makeStats();
    }

    /**
     * 投票が行われた時に呼び出されるメソッド
     * @param event
     */
    @EventHandler
    public void onVotifierEvent(VotifierEvent event) {

        Vote vote = event.getVote();

        // 投票をログに残す
        fileLogger.log(String.format(
                "%s, %s", vote.getTimeStamp(), vote.getUsername()));

        // 統計に加算する
        String date = dformat.format(new Date());
        if ( !stats.containsKey(date) ) {
            stats.put(date, 0);
        }
        int value = stats.get(date) + 1;
        stats.put(date, value);

        // 統計ファイルを出力する
        makeStats();

        // 投票を通知する
        if ( config.isEnableNotification() ) {
            String message = Utility.replaceColorCode(config.getNotificationMessage());
            message = message.replace("%player", vote.getUsername());
            Bukkit.broadcast(message, NOTIFY_PERMISSION);
        }
    }

    /**
     * コマンドが実行された時に呼び出されるメソッド
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( args.length == 0 ) {
            return false;
        }

        if ( args[0].equalsIgnoreCase("log") ) {

            int day = 7;
            if ( args.length >= 2 && args[1].matches("[0-9]+") ) {
                day = Integer.parseInt(args[1]);
                if ( day == 0 ) {
                    day = 7;
                }
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -day);

            ArrayList<String> messages = fileLogger.readHistory(cal.getTime());

            sender.sendMessage("===== " + day + "日分の投票ログ =====");
            for ( String message : messages ) {
                sender.sendMessage(message);
            }

            return true;

        } else if ( args[0].equalsIgnoreCase("stats") ) {

            ArrayList<String> keys = new ArrayList<String>(stats.keySet());
            Collections.sort(keys);

            sender.sendMessage("===== 投票記録数の集計 =====");
            for ( String key : keys ) {
                String message = String.format("%s - %3d", key, stats.get(key));
                sender.sendMessage(message);
            }

            return true;

        } else if ( args[0].equalsIgnoreCase("reload") ) {

            config.reloadConfig();
            sender.sendMessage("[VoteLogger]config.ymlを再読み込みしました。");

            return true;

        }

        return false;
    }

    /**
     * 集計データから集計結果ファイルを出力する
     */
    private void makeStats() {

        ArrayList<String> keys = new ArrayList<String>(stats.keySet());
        Collections.sort(keys);

        File file = new File(getDataFolder(), "stats.csv");

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(HEADER_STATS);
            writer.newLine();
            for ( String key : keys ) {
                writer.write(key + ", " + stats.get(key));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( writer != null ) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }
    }

    /**
     * インスタンスを返す
     * @return インスタンス
     */
    protected static VoteLogger getInstance() {
        return instance;
    }

    /**
     * このプラグインのJarファイル自身を示すFileクラスを返す。
     * @return Jarファイル
     */
    protected static File getPluginJarFile() {
        return instance.getFile();
    }
}
