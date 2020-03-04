package com.github.devcsrj.docparsr

class ParsingJobException(message: String, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String) : this(message, null)
}