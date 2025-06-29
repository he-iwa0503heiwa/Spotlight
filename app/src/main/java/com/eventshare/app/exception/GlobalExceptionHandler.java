package com.eventshare.app.exception;

import org.springframework.boot.context.properties.bind.validation.ValidationErrors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/*
グローバル例外ハンドラー
このクラスで全体の例外を統一管理
 */
//すべてのコントローラーで発生した例外をここで処理
@RestControllerAdvice
public class GlobalExceptionHandler {
    /*
    バリデーションエラーのハンドリング
    @Validアノテーションで発生したエラーを処理
     */
    //MethodArgumentNotValidExceptionはSpring Bootのバリデーション機能（@Validアノテーションを発見したら実行）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex){
        //フィールド名とエラーメッセージのペアを格納するエラーマップ作成
        Map<String, String> errors = new HashMap<>();

        //バリーデーション結果を取得し取得したエラーに対してループ処理
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();//エラーが発生してフィールド名取得
            String errorMessage = error.getDefaultMessage();//エラーメッセージ取得
            errors.put(fieldName, errorMessage);//マップに格納
        });

        //レスポンス用オブジェクトを生成
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                "バリデーションエラー",
                "入力内容に不備があります。以下の項目を確認してください。",
                errors,
                LocalDateTime.now()
        );

        //400エラーとしてJSONレスポンスを返す
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /*
    バリデーション以外のRuntime例外のハンドリング
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex){
        //エラーレスポンス生成
        ErrorResponse errorResponse = new ErrorResponse(
                "エラー",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /*
    予期しない例外のハンドリング
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex){
        //エラーレスポンス生成
        ErrorResponse errorResponse = new ErrorResponse(
                "エラー",
                "予期しないエラーが発生しました。しばらく時間をおいて再度お試しください。",
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*
    バリデーションエラー用のレスポンス内部クラス
     */
    public static class ValidationErrorResponse{
        private String error;                    //エラーの種類
        private String message;                  //全体的なエラーメッセージ
        private Map<String, String> fieldErrors; //フィールド名とエラーメッセージのマップ
        private LocalDateTime timestamp;         //エラーが発生した日時

        //コンストラクタ
        public ValidationErrorResponse(String error, String message, Map<String, String> fieldErrors, LocalDateTime timestamp) {
            this.error = error;
            this.message = message;
            this.fieldErrors = fieldErrors;
            this.timestamp = timestamp;
        }

        //Getters
        public String getError() { return error; }
        public String getMessage() { return message; }
        public Map<String, String> getFieldErrors() { return fieldErrors; }
        public LocalDateTime getTimestamp() { return timestamp; }

        //Setters
        public void setError(String error) { this.error = error; }
        public void setMessage(String message) { this.message = message; }
        public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /*
     一般的なエラーレスポンス用の内部クラス
     */
    public static class ErrorResponse {  //バリデーションエラー以外の、シンプルなエラー用のクラス

        private String error;           //エラーの種類
        private String message;         //エラーメッセージ
        private LocalDateTime timestamp; //エラー発生日時

        //コンストラクタ
        public ErrorResponse(String error, String message, LocalDateTime timestamp) {
            this.error = error;
            this.message = message;
            this.timestamp = timestamp;
        }

        //Getter
        public String getError() { return error; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }

        //Setter
        public void setError(String error) { this.error = error; }
        public void setMessage(String message) { this.message = message; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
