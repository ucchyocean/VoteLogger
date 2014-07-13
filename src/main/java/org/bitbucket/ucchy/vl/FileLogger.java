/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package org.bitbucket.ucchy.vl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ログファイルへ1行書き込むユーティリティクラス
 * @author ucchy
 */
public class FileLogger {

    private static final String HEADER = "#Votifierが受信した時刻, 投票のタイムスタンプ, 投票者";

    private SimpleDateFormat lformat;
    private SimpleDateFormat dformat;

    private File file;

    /**
     * コンストラクタ
     * @param dir ログを格納するフォルダ
     * @param fileName ログファイル名
     */
    public FileLogger(File dir, String fileName) {

        lformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dformat = new SimpleDateFormat("yyyy-MM-dd");

        initialize(dir, fileName);
    }

    /**
     * ログを出力する
     * @param message ログ内容
     */
    public synchronized void log(final String message) {

        // 以降の処理を、発言処理の負荷軽減のため、非同期実行にする。
        Bukkit.getScheduler().runTaskAsynchronously(
                VoteLogger.getInstance(), new BukkitRunnable() {
            public void run() {
                FileWriter writer = null;
                try {
                    writer = new FileWriter(file, true);
                    String str = lformat.format(new Date()) + ", " + message;
                    writer.write(str + "\r\n");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if ( writer != null ) {
                        try {
                            writer.close();
                        } catch (Exception e) {
                            // do nothing.
                        }
                    }
                }
            }
        });
    }

    /**
     * 内容を読み取って集計する
     * @return 集計結果
     */
    public HashMap<String, Integer> readStats() {

        // ファイルの内容を読み出す
        ArrayList<String> contents = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ( (line = reader.readLine()) != null ) {

                line = line.trim();

                // 頭にシャープが付いている行は、コメントとして読み飛ばす
                if ( !line.startsWith("#") ) {
                    contents.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }

        // 内容の解析
        HashMap<String, Integer> stats = new HashMap<String, Integer>();

        for ( String line : contents ) {

            String[] data = line.split(",");

            if ( data.length < 3 ) {
                continue;
            }

            String temp = data[0].trim();
            String date = temp.substring(0, temp.indexOf(" "));

            if ( !stats.containsKey(date) ) {
                stats.put(date, 0);
            }

            int value = stats.get(date) + 1;
            stats.put(date, value);
        }

        return stats;
    }

    public ArrayList<String> readHistory(Date startDate) {

        // ファイルの内容を読み出す
        ArrayList<String> contents = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ( (line = reader.readLine()) != null ) {

                line = line.trim();

                // 頭にシャープが付いている行は、コメントとして読み飛ばす
                if ( line.startsWith("#") ) {
                    continue;
                }

                // データの形式が正しくないなら読み飛ばす
                String[] data = line.split(",");
                if ( data.length < 3 ) {
                    continue;
                }

                try {
                    // 日付を取得し、ボーダーの日付より古ければ読み飛ばす
                    Date tdate = dformat.parse(data[0].substring(0, data[0].indexOf(" ")));

                    if ( !tdate.before(startDate) ) {
                        contents.add(data[0] + " " + data[2].trim());
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }

        // ソートして返す（日付時刻順になる。）
        Collections.sort(contents);
        return contents;
    }

    /**
     * 初期化する
     */
    private void initialize(File dir, String fileName) {

        file = new File(dir, fileName);

        // ファイルが存在しないなら、この時点で作成しておく
        if ( !file.exists() ) {
            FileWriter writer = null;
            try {
                writer = new FileWriter(file);
                writer.write(HEADER + "\r\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if ( writer != null ) {
                    try {
                        writer.close();
                    } catch (Exception e) {
                        // do nothing.
                    }
                }
            }
        }
    }
}
