# Multi-Site Project

このプロジェクトは、フロントエンド、バックエンド、モバイルアプリケーションを含む統合的なマルチサイトプロジェクトです。

## プロジェクト構成

```
├── frontend/          # フロントエンドアプリケーション (Next.js/React)
├── backend/           # バックエンドAPI
├── tango/            # Androidモバイルアプリ
├── nginx/            # Nginxリバースプロキシ設定
├── PostgreSQL/       # データベース設定
├── docker-compose.yml # Docker設定
└── setup-multi-site.sh # セットアップスクリプト
```

## 必要な環境

- Docker & Docker Compose
- Node.js (フロントエンド開発用)
- Python (バックエンド開発用)
- Android Studio (モバイルアプリ開発用)

## セットアップ

### 1. リポジトリのクローン
```bash
git clone --recursive https://github.com/[username]/[repository-name].git
cd [repository-name]
```

### 2. 環境変数の設定
```bash
# .envファイルを作成（.env.exampleを参考に）
cp .env.example .env

# 必要な環境変数を設定
# - データベース接続情報
# - API設定
# - その他の機密情報
```

### 3. Docker環境の起動
```bash
# 通常の起動
docker-compose up -d

# マルチサイト環境の起動
./setup-multi-site.sh
```

## 開発

### フロントエンド
```bash
cd frontend
npm install
npm run dev
```

### バックエンド
```bash
cd backend
pip install -r requirements.txt
python manage.py runserver
```

### モバイルアプリ
```bash
cd tango
./gradlew assembleDebug
```

## 機能

- **フロントエンド**: モダンなWeb UI
- **バックエンド**: RESTful API
- **モバイルアプリ**: Android ネイティブアプリ
- **Docker**: 簡単なデプロイメント
- **Nginx**: リバースプロキシとロードバランシング

## ライセンス

[ライセンスを指定してください]

## 貢献

プルリクエストや課題報告を歓迎します。

## 連絡先

[連絡先情報を追加してください] 