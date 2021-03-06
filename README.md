VoteLogger
==========

概要
----

Votifierと連携して、投票が通知されるたびに、投票時刻と投票者を記録することができます。<br/>
また、日付ごとの投票記録数を集計しており、投票記録数の遷移を確認することができます。<br/>
投票記録、投票数の統計は、csvファイルで出力されますので、後から表計算ソフトなどで開いて確認することができます。

使用方法
--------
pluginsフォルダに、jarファイルを入れてください。

コンフィグ
----------
plugins/VoteLogger/config.yml に生成されます。
<pre>
# 投票通知を受け取った時に、通知メッセージを流すかどうかを設定します。
# true に設定すると、"votelogger.notify" 権限を持っているプレイヤーの画面と、
# サーバーコンソールに、通知を送信します。
enableNotification: true

# 通知メッセージです。
# カラーコード（&0～&f）と、プレイヤー名キーワード %player が利用可能です。
# enableNotification設定がtrueになっていないと、本設定は無意味です。
notificationMessage: '&e[投票通知]&f%playerさんがサーバーに投票しました。'
</pre>

権限
----
- `votelogger.command` - voteloggerコマンドの実行権限
- `votelogger.notify` - 投票の通知を受け取る権限
- `votelogger.*` - 上記の votelogger.command と votelogger.notify の両方を含む権限

コマンド
--------
コマンドは `/votelogger` です。省略形 `/vl` を利用することも可能です。
- `/votelogger log [日数]` - 直近の投票ログを参照します。日数を省略すると7日分表示します。
- `/votelogger stats` - 投票記録数の日付ごとの集計を表示します。
- `/votelogger reload` - config.ymlを再読み込みします。

ログファイル
------------
- `plugins/VoteLogger/votes.csv` - 投票記録ログファイルです。
- `plugins/VoteLogger/stats.csv` - 1日毎の投票記録数の統計です。

ライセンス
----------
LGPLv3を適用します。<br/>
ソースコードを流用する場合は、流用先にもLGPLv3を適用してください。

ダウンロード
------------
https://github.com/ucchyocean/VoteLogger/blob/master/release/VoteLogger.zip?raw=true

