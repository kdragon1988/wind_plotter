# WindPlotter

DJI RC Plus 2 (Android 10) 向けの風況データ収集・可視化アプリケーションです。
DJI Mobile SDK v5 (Enterprise) を使用して、ドローンからリアルタイムの風速・風向・位置情報を取得し、記録します。

## 機能

- **ミッション管理**: 観測ミッションの作成、担当者の記録、履歴の管理（削除機能付き）。
- **リアルタイムモニタリング**: ドローンからのテレメトリデータ（風速、風向、高度）をリアルタイムに表示。
    - **風速グラフ**: 直近の風速推移をグラフで可視化。
    - **コンパクトUI**: コントローラーの画面に最適化された1画面レイアウト。
- **データ分析**: ミッション完了後、最大/平均風速、平均高度、計測時間などの統計レポートを表示。
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
