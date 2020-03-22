package own.star.wheel.core.run.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author xinsheng
 * @date 2020/03/22
 */
@RestController
@RequestMapping("/health")
class HealthCheckController {

    @GetMapping("/check")
    fun check(): String {
        return "success"
    }
}