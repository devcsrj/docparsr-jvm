package com.github.devcsrj.docparsr

import java.lang.RuntimeException

class ParsingJobException(message: String, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String) : this(message, null)
}