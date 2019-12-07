package own.star.wheel.core

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan

/**
 * @author xinsheng
 * @date 2019/11/20
 */

@SpringBootApplication(scanBasePackages = ["own.star.wheel.core"])
@EnableConfigurationProperties
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}