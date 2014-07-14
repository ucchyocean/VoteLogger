/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package org.bitbucket.ucchy.vl;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * VoteLoggerのコンフィグ管理クラス
 * @author ucchy
 */
public class VoteLoggerConfig {

    /** 投票通知を受け取った時に、通知メッセージを流すかどうか */
    private boolean enableNotification;

    /** 通知メッセージ */
    private String notificationMessage;

    /**
     * コンストラクタ
     */
    protected VoteLoggerConfig() {
        reloadConfig();
    }

    /**
     * config.yml を再読み込みする
     */
    public void reloadConfig() {

        File configFile = new File(
                VoteLogger.getInstance().getDataFolder(), "config.yml");
        if ( !configFile.exists() ) {
            Utility.copyFileFromJar(VoteLogger.getPluginJarFile(),
                    configFile, "config_ja.yml", false);
        }

        VoteLogger.getInstance().reloadConfig();
        FileConfiguration config = VoteLogger.getInstance().getConfig();

        enableNotification = config.getBoolean("enableNotification", true);
        notificationMessage = config.getString("notificationMessage",
                "&e[投票通知]&f%playerさんがサーバーに投票しました。");
    }

    /**
     * @return enableNotification
     */
    public boolean isEnableNotification() {
        return enableNotification;
    }

    /**
     * @return notificationMessage
     */
    public String getNotificationMessage() {
        return notificationMessage;
    }
}
