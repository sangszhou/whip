package own.star.wheel.core.run.handler

import com.netflix.spinnaker.q.MessageHandler
import com.netflix.spinnaker.q.Queue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.CollectionUtils
import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.model.StartExecution
import own.star.wheel.core.run.model.StartStage
import own.star.wheel.core.run.service.ExecutionService

/**
 * @author xinsheng
 * @date 2019/11/12
 */
@Component
class StartExecutionHandler(
    override val queue: Queue,
    val executionService: ExecutionService
) : MessageHandler<StartExecution> {
    private val log = LoggerFactory.getLogger(javaClass)

    override val messageType: Class<StartExecution> = StartExecution::class.java

    override fun handle(startExecution: StartExecution) {
        val execution = executionService.getExecution(startExecution.executionId)
        startExecution(execution)
    }

    private fun startExecution(execution: Execution) {
        val stages = execution.initialStages()
        if (CollectionUtils.isEmpty(stages)) {
            log.info("stage is empty")
            execution.status = ExecutionStatus.SUCCEEDED
            executionService.updateExecution(execution)
        } else {
            log.info("stage size: {}", stages.size)
            stages.forEach {queue.push(StartStage(execution.id, it.id))}
            execution.status = ExecutionStatus.RUNNING
            executionService.updateExecution(execution)
        }
    }
}