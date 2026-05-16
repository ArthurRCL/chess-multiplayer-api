package com.xadrezonline

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class XadrezApplication

fun main(args: Array<String>) {
    runApplication<XadrezApplication>(*args)
}
