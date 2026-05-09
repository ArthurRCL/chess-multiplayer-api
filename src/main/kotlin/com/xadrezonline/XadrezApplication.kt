package com.xadrezonline

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class XadrezApplication

fun main(args: Array<String>) {
    runApplication<XadrezApplication>(*args)
}
