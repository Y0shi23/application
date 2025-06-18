# ログインテストコード

このプロジェクトには、GoのGin APIアプリケーションのログイン機能をテストするための包括的なテストコードが含まれています。

## テストファイル

### 1. Go言語のユニットテスト
**ファイル**: `backend/app/auth/handler_test.go`

GoのGinフレームワークを使用したHTTPハンドラーのユニットテストです。

#### 含まれるテストケース：
- ✅ **正常なログイン** - 有効な認証情報でのログイン成功
- ❌ **無効な認証情報** - 存在しないユーザーや間違ったパスワード
- ❌ **間違ったパスワード** - 正しいユーザー名、間違ったパスワード
- ❌ **空のリクエスト** - 必須フィールドが空
- ❌ **無効なJSON** - 不正なJSON形式
- ❌ **部分的なデータ** - ユーザー名のみ、パスワードのみ
- 🛡️ **SQLインジェクション対策** - 悪意のあるSQL文の挿入試行
- 📊 **ベンチマークテスト** - パフォーマンス測定

#### 実行方法：
```bash
cd backend/app
go test ./auth -v
```

#### ベンチマーク実行：
```bash
cd backend/app
go test ./auth -bench=.
```

### 2. Python API統合テスト
**ファイル**: `login_test.py`

実際のAPIエンドポイントに対するHTTPリクエストを送信する統合テストです。

#### 含まれるテストケース：
- ✅ **正常なログインフロー** - 登録→ログイン→認証済みリソースアクセス
- ❌ **無効な認証情報** - 各種エラーケース
- ❌ **バリデーションエラー** - 空フィールド、形式エラー
- ❌ **無効なJSON** - 不正なリクエスト形式
- 🛡️ **セキュリティテスト** - SQLインジェクション等
- ⚡ **パフォーマンステスト** - レスポンス時間測定
- 🔧 **ユーティリティテスト** - データ検証ロジック

#### 実行方法：

1. **前提条件**: 
   - Pythonのrequestsライブラリをインストール
   ```bash
   pip install requests
   ```

2. **APIサーバーを起動**:
   ```bash
   cd backend/app
   go run main.go
   ```

3. **テストを実行**:
   ```bash
   python login_test.py
   ```

## テスト実行の完全手順

### 1. 環境準備

```bash
# 1. データベースを起動（Docker Compose使用）
docker-compose up -d

# 2. 必要な依存関係をインストール
cd backend/app
go mod tidy

# 3. Python依存関係をインストール
pip install requests
```

### 2. Goユニットテストの実行

```bash
cd backend/app

# 通常のテスト実行
go test ./auth -v

# カバレッジ付きでテスト実行
go test ./auth -v -cover

# ベンチマークテスト実行
go test ./auth -bench=. -benchmem
```

### 3. Python統合テストの実行

```bash
# APIサーバーを起動（別ターミナル）
cd backend/app
go run main.go

# テストを実行（メインターミナル）
python login_test.py

# または個別のテストクラスを実行
python -m unittest login_test.LoginAPITest.test_login_success -v
```

## テスト項目の詳細

### セキュリティテスト
- **SQLインジェクション対策**: 悪意のあるSQL文が実行されないことを確認
- **入力値検証**: 不正な入力データが適切に拒否されることを確認
- **認証トークン**: JWTトークンが正しく生成・検証されることを確認

### パフォーマンステスト
- **レスポンス時間**: ログインAPIの応答時間が許容範囲内であることを確認
- **同時接続**: 複数のリクエストが適切に処理されることを確認

### エラーハンドリングテスト
- **HTTP ステータスコード**: 各シナリオで適切なステータスコードが返されることを確認
- **エラーメッセージ**: ユーザーフレンドリーなエラーメッセージが返されることを確認

## CI/CD統合

### GitHub Actions例

```yaml
name: Login Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up Go
      uses: actions/setup-go@v3
      with:
        go-version: 1.19
    
    - name: Set up Python
      uses: actions/setup-python@v3
      with:
        python-version: '3.9'
    
    - name: Install Python dependencies
      run: pip install requests
    
    - name: Run Go tests
      run: |
        cd backend/app
        go test ./auth -v
    
    - name: Start API server
      run: |
        cd backend/app
        go run main.go &
        sleep 5
    
    - name: Run Python integration tests
      run: python login_test.py
```

## トラブルシューティング

### よくある問題

1. **データベース接続エラー**
   ```
   解決策: docker-compose up -d でデータベースが起動していることを確認
   ```

2. **ポート競合**
   ```
   解決策: ポート8080が使用されていないことを確認
   ```

3. **JWT秘密鍵エラー**
   ```
   解決策: .envファイルでJWT_SECRETが設定されていることを確認
   ```

### ログの確認方法

```bash
# APIサーバーのログ
cd backend/app
go run main.go

# データベースのログ
docker-compose logs db

# テスト実行時の詳細ログ
go test ./auth -v -run TestLoginHandler
python -m unittest login_test.LoginAPITest.test_login_success -v
```

## カスタマイズ

### テストデータの変更

`login_test.py`の`setUp`メソッドでテストユーザー情報を変更できます：

```python
self.test_user = {
    "username": "your_test_user",
    "email": "your_test@example.com", 
    "password": "your_test_password"
}
```

### 追加テストケースの作成

新しいテストケースを追加する場合は、既存のテスト関数をコピーして修正してください：

```python
def test_your_custom_case(self):
    """カスタムテストケース"""
    # テストロジックを記述
    pass
```

## コードカバレッジ

Goテストのカバレッジレポートを生成：

```bash
cd backend/app
go test ./auth -coverprofile=coverage.out
go tool cover -html=coverage.out -o coverage.html
```

ブラウザで`coverage.html`を開いてカバレッジを確認できます。 