package com.example.mydamo.data.api

object MockErrorController {
    // NONE: 正常
    // TIMEOUT: 模拟连接超时
    // HTTP_500: 模拟服务器内部错误
    var errorMode: ErrorMode = ErrorMode.NONE
}

enum class ErrorMode {
    NONE, TIMEOUT, HTTP_500
}