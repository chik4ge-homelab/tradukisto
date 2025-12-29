package me.chik4ge.tradukisto

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TradukistoApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<TradukistoApplication>(*args)
}
