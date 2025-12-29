package me.chik4ge.tradukisto

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import kotlin.test.assertNotNull

@SpringBootTest
class TradukistoApplicationTests(
    private val context: ApplicationContext,
) {
    @Test
    fun contextLoads() {
        assertNotNull(context)
    }
}
