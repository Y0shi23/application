#!/bin/bash

echo "=== 複数サイト環境のセットアップ ==="

# ディレクトリ作成
echo "必要なディレクトリを作成しています..."
mkdir -p nginx/ssl
mkdir -p frontend2/app
mkdir -p backend2/app
mkdir -p PostgreSQL2

# frontend2の初期化
echo "frontend2の初期化..."
if [ ! -f frontend2/package.json ]; then
    cd frontend2
    npm init -y
    npm install react react-dom next
    cd ..
fi

# backend2の初期化
echo "backend2の初期化..."
if [ ! -f backend2/app/main.go ]; then
    cat > backend2/app/main.go << 'EOF'
package main

import (
    "fmt"
    "net/http"
    "log"
)

func main() {
    http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
        fmt.Fprintf(w, "Hello from MyS	ite2 Backend!")
    })
    
    http.HandleFunc("/api/health", func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json")
        fmt.Fprintf(w, `{"status": "ok", "site": "mysite2"}`)
    })
    
    log.Println("MyS	ite2 Backend server starting on :8081")
    log.Fatal(http.ListenAndServe(":8081", nil))
}
EOF
fi

# backend2のDockerfile作成
echo "backend2のDockerfileを作成..."
cat > backend2/Dockerfile << 'EOF'
FROM golang:1.21-alpine

WORKDIR /app/src

# 依存関係をインストール
COPY app/go.mod app/go.sum ./
RUN go mod download

# ソースコードをコピー
COPY app/ ./

# アプリケーションをビルド
RUN go build -o main .

# ポート8081を公開
EXPOSE 8081

# アプリケーションを実行
CMD ["./main"]
EOF

# frontend2のDockerfile作成
echo "frontend2のDockerfileを作成..."
cat > frontend2/Dockerfile << 'EOF'
FROM node:18-alpine

WORKDIR /app

# package.jsonをコピーして依存関係をインストール
COPY app/package*.json ./
RUN npm install

# アプリケーションコードをコピー
COPY app/ ./

# ポート3001を公開
EXPOSE 3001

# 開発サーバーを起動
CMD ["npm", "run", "dev"]
EOF

# SSL証明書の生成（開発用）
echo "開発用SSL証明書を生成..."
if [ ! -f nginx/ssl/server.crt ]; then
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout nginx/ssl/server.key \
        -out nginx/ssl/server.crt \
        -subj "/C=JP/ST=Tokyo/L=Tokyo/O=MyOrg/CN=fumi042-server.top" \
        -addext "subjectAltName=DNS:*.fumi042-server.top"
fi

# DNS設定のメモを作成
echo "DNS設定についてのメモを作成..."
cat > DNS_SETUP.md << 'EOF'
# DNS設定ガイド

## 必要なDNSレコード

ドメインプロバイダー（お名前.comなど）で以下のAレコードまたはCNAMEレコードを設定してください：

1. `mysite7.fumi042-server.top` → サーバーのIPアドレス
2. `mysite2.fumi042-server.top` → サーバーのIPアドレス
3. `*.fumi042-server.top` → サーバーのIPアドレス（ワイルドカード設定）

## ローカル開発用（/etc/hosts）

ローカル開発の場合は、以下を `/etc/hosts` ファイルに追加：

```
127.0.0.1 mysite7.fumi042-server.top
127.0.0.1 mysite2.fumi042-server.top
```

## 確認方法

設定後、以下のコマンドで確認：

```bash
nslookup mysite7.fumi042-server.top
nslookup mysite2.fumi042-server.top
```
EOF

echo "=== セットアップ完了 ==="
echo ""
echo "次のステップ："
echo "1. DNS_SETUP.mdを確認してDNS設定を行ってください"
echo "2. .env.multi-siteファイルの設定を確認してください"
echo "3. 以下のコマンドでサービスを起動："
echo "   docker-compose -f docker-compose.multi-site.yml --env-file .env.multi-site up -d"
echo ""
echo "アクセス方法："
echo "- http://mysite7.fumi042-server.top/polls"
echo "- http://mysite2.fumi042-server.top" 