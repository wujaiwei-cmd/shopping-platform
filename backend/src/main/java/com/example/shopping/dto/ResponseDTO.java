
package com.example.shopping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDTO<T> {

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> ResponseDTO<T> success(T data) {
        return ResponseDTO.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ResponseDTO<T> success(String message) {
        return ResponseDTO.<T>builder()
                .code(200)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ResponseDTO<T> success(String message, T data) {
        return ResponseDTO.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ResponseDTO<T> error(Integer code, String message) {
        return ResponseDTO.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> ResponseDTO<T> error(String message) {
        return error(400, message);
    }

    public static <T> ResponseDTO<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> ResponseDTO<T> forbidden(String message) {
        return error(403, message);
    }
}
