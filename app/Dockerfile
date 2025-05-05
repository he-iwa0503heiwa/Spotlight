# Java 17ベースイメージ
FROM openjdk:17-jdk-slim

# 作業ディレクトリ設定
WORKDIR /app

# ビルドされたjarファイルをコピー
COPY build/libs/*.jar app.jar

# アプリケーションポート
EXPOSE 8080

# アプリケーション起動
ENTRYPOINT ["java", "-jar", "/app/app.jar"]