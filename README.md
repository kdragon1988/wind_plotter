# WindPlotter

DJI RC Plus 2 (Android 10) 向けの風況データ収集・可視化アプリケーションです。
DJI Mobile SDK v5 (Enterprise) を使用して、ドローンからリアルタイムの風速・風向・位置情報を取得し、記録します。

## 機能

- **ミッション管理**: 観測ミッションの作成、担当者の記録、履歴の管理（削除機能付き）。
- **操作UI（コックピット）**:
    - カメラ映像、機体ステータス、風速トレンド、風速ログを1画面に集約表示。
    - 計測はミッション中に任意タイミングで開始/停止可能。
    - 風向は方位角（°）だけでなく方位名（北東など）でも表示。
- **UIトンマナ統一**:
    - ホーム画面・ミッション履歴・レポート画面を操作UIと同系統のタクティカルデザインに統一。
    - ミッション履歴は高密度表示に最適化し、1画面でより多くのミッションを確認可能。
- **レポート分析**:
    - ミッション全体（ALL）に加え、計測シーン単位（S1, S2...）で分析対象を切り替え可能。
    - 選択した対象ごとに、最大/平均風速、平均高度、主風向、計測時間帯、危険域件数を表示。
- **データ収集 & 同期**:
    - 1Hzの頻度でデータをローカルデータベース (Room) に保存。
    - ネットワーク接続時にバックグラウンドでクラウドへデータを自動同期 (Mock実装済み)。
- **バックグラウンド実行**: 他のアプリ（DJI Pilot 2など）を使用中もデータ収集を継続。

## 環境

- **Target Device**: DJI RC Plus 2
- **OS**: Android 10 (API Level 29)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **SDK**: DJI Mobile SDK v5.9.0 (Enterprise)

## セットアップ

1. **DJI SDK API Key**:
   - `AndroidManifest.xml` の `com.dji.sdk.API_KEY` に有効な API Key を設定してください。
   - パッケージ名: `com.example.windplotter`

2. **ビルド**:
   - Android Studio Koala Feature Drop | 2024.1.2 以降推奨。
   - `gradle.properties` で Gradle JVM のメモリ設定 (`-Xmx4096m`) を推奨。
   - **重要**: RC Plus 2 実機でデバッグする場合、Android Studio の Run Configuration で **"Always install with package manager"** を有効にしてください（ネイティブライブラリのロードエラー回避のため）。

## ライセンス

[MIT License](LICENSE)
