# Java学習システム

Spring Boot + Gemini API で、Javaの学習を「目標設定 → 毎日の問題 → AIレビュー → 進捗の可視化」の
サイクルで継続できるようにする学習用アプリです。Gemini APIは無料枠があるため、学習用途であれば
費用をかけずに使えます。

```
ブラウザ(静的HTML/JS、Chart.jsはCDN経由)
   ↓
Controller (Goal / Problem / Review / Stats Controller)
   ↓
Service (GoalService, GeminiProblemService, GeminiReviewService, StatsService) --- Gemini API
   ↓
Repository (GoalRepository, ProblemRepository, ReviewRepository / JdbcTemplate)
   ↓
SQLite (review.db)
```

Controller → Service → Repository → DB という、これまで学習してきた構成そのままで、
「Spring BootからAPIを呼ぶ」経験が積めるように作っています。JPAは使わず、
これまで慣れているJDBC(Spring版のJdbcTemplate)でSQLiteにアクセスしています。

## できること

4つのタブで構成されています。

- **🎯 目標**: 「目指す姿」「作りたいもの」「1日の学習時間」「達成目標日」を登録します。
  ヘッダーに常に「目標まであとN日」が表示されます。
- **📅 今日の問題**: その日初めて開いたときに、Geminiが目標に合わせたプログラミング問題を
  1問自動生成します(2回目以降は同じ問題が表示され、再生成はされません)。回答コードを
  提出するとそのままレビューされます。
- **📝 レビュー**: テキストエリアにJavaコードを貼り付けて「レビューを依頼する」を押すと、Geminiが
  バグの可能性・設計/可読性・Javaのベストプラクティス・良い点・次に学ぶと良いことをレビューし、
  100点満点のスコアを返します。右側の「履歴」から過去のレビューをいつでも見返せます。
- **📊 進捗**: 総レビュー数・平均スコア・連続学習日数・目標までの残り日数と進捗率をタイル表示し、
  スコア推移(折れ線)と直近30日の学習件数(棒グラフ)をChart.jsで可視化します。

レビュー結果・目標・出題した問題はすべてSQLiteに保存され、アプリを再起動しても保持されます。

## 事前準備

- JDK 17以上 (`java -version` で確認)
- Maven (`mvn -version` で確認。使っていなければ [Maven公式](https://maven.apache.org/download.cgi) からインストール)
- Gemini APIキー(無料)。[Google AI Studio](https://aistudio.google.com/apikey) にGoogleアカウントで
  ログインして発行してください。無料枠にはレート制限がありますが、個人の学習用途なら十分です。
- 進捗タブのグラフ描画にChart.jsをCDN経由で読み込むため、ブラウザからインターネット接続が必要です。

## 起動方法

1. プロジェクトを展開したフォルダに移動する

   ```
   cd java-review-bot
   ```

2. APIキーを環境変数に設定する

   **Windows (コマンドプロンプト)**

   ```
   set GEMINI_API_KEY=xxxxxxxxxxxxxxxx
   ```

   **Windows (PowerShell)**

   ```
   $env:GEMINI_API_KEY="xxxxxxxxxxxxxxxx"
   ```

   **macOS / Linux**

   ```
   export GEMINI_API_KEY=xxxxxxxxxxxxxxxx
   ```

   ※このウィンドウ/ターミナルを閉じると設定が消えるので、毎回設定するか、
   OSの環境変数設定に恒久的に登録してください。

3. アプリを起動する

   ```
   mvn spring-boot:run
   ```

4. ブラウザで `http://localhost:8080` を開く

初回起動時に `review.db` というSQLiteファイルがプロジェクト直下に自動生成されます
(schema.sqlが自動実行されます)。このファイルにレビュー履歴・目標・出題した問題が溜まっていきます。
以前のバージョンから引き続き使う場合も、起動時に不足しているテーブル/カラムが自動追加されます。

## 使い方のコツ

- テキストエリアにフォーカスした状態で `Ctrl + Enter` (Macは `Cmd + Enter`) でも送信できます。
- 使うモデルは `src/main/resources/application.properties` の `gemini.api.model` で変更できます
  (無料枠で使えるモデルの範囲で選んでください)。
- レビューの観点(何を見てほしいか)は `GeminiReviewService` の `SYSTEM_PROMPT` を書き換えることで
  自由にカスタマイズできます。例えば「Spring Bootの設計原則も見てほしい」のように追記してみてください。
- 今日の問題の出題傾向(テーマの選び方や難易度の書き方)は `GeminiProblemService` の `SYSTEM_PROMPT` で
  カスタマイズできます。
- 目標は常に最新の1件が「現在の目標」として扱われます(履歴管理はしていません)。目標タブで再保存すると
  新しい目標に上書きされます。
- Gemini無料枠のレート制限(1分あたりのリクエスト数など)に達すると、レビュー/問題生成がエラーになる
  ことがあります。少し待ってから再度お試しください。

## この状態からの発展アイデア

- 案件配信ツールと同じくSupabaseに繋ぎ変えて、履歴をクラウドに保存する
- レビュー結果をJSON構造(バグ一覧/改善点一覧など)で返させて、UIをリッチにする
- ファイルアップロード(.javaファイル)に対応する
- Spring Securityでログイン機能を追加する
- 目標の達成度に応じて、翌日の問題の難易度をGeminiに自動調整させる
- 週次/月次のサマリーをGeminiにまとめてもらう機能を追加する
