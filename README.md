# EconomyCore

所持金、初期付与、入出金、送金と取引履歴を管理する。通貨単位は円、初期所持金は1000。

## 依存・導入

Java 21、DBConnectorN 1.3.0以降、PlayersID 1.3.1以降が必要。`./gradlew test shadowJar`で生成した`build/libs/`の`-all.jar`を配置し、依存先と一緒に再起動する。既存の財布・履歴のテーブル、ID、保存済み値は維持する。

## 公開API

新しい連携は`jp.asteria.economy.api.EconomyApi`へPlayersIDの整数IDを渡す。プレイヤー実体やDAOを渡さず、オフライン・workerからも使用できる。

- `getWallet`は不変の`WalletSnapshot`を返す。未作成・未知IDはnull。`existsData`は存在を判定する。
- `getBalance`は残高を返し、財布がなければ例外になる。照会では行を作成しない。外側にExposed transactionがあれば、その照会スナップショットに参加する。
- `ensureWallet`は財布がない場合にだけ初期1000と付与ログを同時保存する。既存残高0にも再付与しない。ログイン時もこの経路を使う。
- `increaseMoney`・`decreaseMoney`・`transferMoney`は財布の作成後に使う。負数はfalse、不足時もfalse。0は残高を保ち、従来どおり履歴を記録する。
- 送金は両方の残高を検査してから保存する。自己送金も残高確認を行い、残高を変更せず1件記録する。

更新はPlayersIDのプレイヤー行を最初にロックし、最新の財布をSQLで読み、残高と履歴を同じExposed transactionへ保存する。送金は両IDを昇順で一括取得する。複数プレイヤーを外側の同一transactionで操作するときは、最初に`PlayerTransactions.write(全対象ID)`でまとめて取得し、ロック順を逆行させない。

加算overflowは`ArithmeticException`、保存残高が負数の場合は入力エラーとして更新を拒否する。例外が起きた更新を外側で捕捉しても、そのtransaction全体のcommitを拒否する。途中までの残高・履歴を確定させない。負の旧残高を推測で修復しない。

DBConnectorNのraw `Database.transaction`へは参加できない。更新と同じtransaction・親・後発の子で、財布や履歴のDAO変更を混在させるとcommitを拒否する。公開DAO型と履歴helperは旧連携の互換性のために残すが、新しい保存処理では使用しない。

停止時は新しい更新の受付を止め、root待機後にも停止状態を再確認する。root取得後の確認を通過していた処理は完了できる。

### 旧IPlayer APIと履歴

`EconomyCore.api`と`IEconomyCore`は維持する。IPlayerの識別子・名前を主スレッドで解決してから、同じID APIへ委譲する。workerでは整数ID APIを使う。旧Boolean APIではoverflowをfalseとして返すが、外側transactionのcommit拒否は維持する。

0.3.0以降の送金ログは、送金元・送金先を同じ1行へ記録する。過去の2行形式の履歴は書き換えない。理由省略時の旧APIは、両者の名前を含む送金理由を記録する。自己送金の履歴は二重表示しない。

## コマンド

`/money`で所持金を確認する。`help`・`show <名前>`・`pay <名前> <金額> [理由]`・`give <名前> <金額>`・`take <名前> <金額>`を使用できる。各サブコマンドは実行前に`jp.asteria.economy.commands.<名前>`の権限を確認し、give・takeの既定権限はop。
