package own.star.wheel.core.run.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.TriggerConfig

/**
 * @author xinsheng
 * @date 2019/12/10
 */
@Component
class ExecutionLauncher {
    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var executionRunner: ExecutionRunner
    @Autowired
    lateinit var executionService: ExecutionService
    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Throws(Exception::class)
    fun start(tmpId: String, context: Map<String, String>): Execution {
        val executionId = executionService.triggerPipeline(tmpId, context)
        val execution = executionService.getExecution(executionId)

        try {
            start(execution)
        } catch (exp: Exception) {
            log.info("failed to start execution")
        }

        return execution
    }

    @Throws(Exception::class)
    fun start(execution: Execution) {
        log.info("start execution")
        executionRunner.start(execution)
    }
}

