# EventShare - イベント情報共有プラットフォーム

## 概要
EventShareは、同じ趣味を持つ人々との交流を目的としたイベント情報共有プラットフォームです。
野球観戦、お笑い鑑賞、写真撮影などのイベントを作成・参加し、参加者同士の交流を促進します。

## 技術スタック

### バックエンド
- **Java 21**
- **Spring Boot 3.5.3**
- **Spring Security** (JWT認証)
- **Spring Data JPA**
- **PostgreSQL**
- **Gradle**

### フロントエンド
- **HTML/CSS/JavaScript**
- **Vanilla JavaScript** (フレームワークなし)

### その他
- **Docker/Docker Compose**
- **JUnit 5** (テスト)
- **Apache Tika** (ファイル処理)

## 主な機能

### 認証機能
- ユーザー登録・ログイン
- JWT トークンによる認証

### イベント管理
- イベント作成・編集・削除
- イベント一覧表示・検索
- カテゴリ別分類（野球、写真・カメラ、お笑い、その他）

### イベント参加
- イベント参加登録・キャンセル
- 定員管理（定員超過時はキャンセル待ち）
- 参加状況確認

### 写真共有機能
- イベント写真のアップロード
- 写真一覧表示
- 写真削除（投稿者・イベント作成者のみ）

## セットアップ

### 必要環境
- Java 17以上
- PostgreSQL 16
- Docker (推奨)

### 1. リポジトリをクローン
```bash
git clone [リポジトリURL]
cd EventShare
```

### 2. Docker Composeで起動（推奨）
```bash
cd app
docker-compose up -d
```

### 3. 手動セットアップ

#### PostgreSQLデータベース作成
```sql
CREATE DATABASE eventshare;
CREATE USER admin WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE eventshare TO admin;
```

#### アプリケーション起動
```bash
cd app
./gradlew bootRun
```

## API仕様

### 認証API
- `POST /api/auth/register` - ユーザー登録
- `POST /api/auth/login` - ログイン
- `POST /api/auth/validate` - トークン検証

### イベントAPI
- `GET /api/events` - イベント一覧取得
- `POST /api/events` - イベント作成 (要認証)
- `GET /api/events/{id}` - イベント詳細取得
- `PUT /api/events/{id}` - イベント更新 (要認証・作成者のみ)
- `DELETE /api/events/{id}` - イベント削除 (要認証・作成者のみ)

### イベント参加API
- `POST /api/events/{id}/participate` - イベント参加 (要認証)
- `DELETE /api/events/{id}/participate` - 参加キャンセル (要認証)
- `GET /api/events/{id}/participants` - 参加者一覧 (要認証)

### 写真API
- `POST /api/photos/upload/{eventId}` - 写真アップロード (要認証)
- `GET /api/photos/event/{eventId}` - イベント写真一覧
- `GET /api/photos/file/{filename}` - 写真ファイル取得
- `DELETE /api/photos/{photoId}` - 写真削除 (要認証)

### ユーザーAPI
- `GET /api/users/me` - 現在のユーザー情報取得 (要認証)
- `PUT /api/users/me` - ユーザー情報更新 (要認証)

## アクセス方法

アプリケーション起動後、ブラウザで以下にアクセス：
```
http://localhost:8080
```

## テスト実行

```bash
cd app
./gradlew test
```

## 開発について

### プロジェクト構成
```
app/
├── src/main/java/com/eventshare/app/
│   ├── controller/     # APIエンドポイント
│   ├── service/        # ビジネスロジック
│   ├── repository/     # データアクセス
│   ├── entity/         # エンティティ
│   ├── dto/            # データ転送オブジェクト
│   ├── security/       # セキュリティ設定
│   └── config/         # 設定クラス
├── src/main/resources/
│   ├── application.properties
│   └── static/         # フロントエンド（HTML/CSS/JS）
└── src/test/           # テストコード
```

### 初期データ
アプリケーション起動時に以下のカテゴリが自動作成されます：
- 野球
- 写真・カメラ
- お笑い
- その他

## ライセンス
このプロジェクトは学習目的で作成されています。

## 作者
Heiwa Fujino
