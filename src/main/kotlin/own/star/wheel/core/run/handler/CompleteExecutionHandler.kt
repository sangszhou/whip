package own.star.wheel.core.run.handler

import com.alibaba.fastjson.JSON
import com.netflix.spinnaker.q.MessageHandler
import com.netflix.spinnaker.q.Queue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import own.star.wheel.core.run.model.CompleteExecution
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.service.ExecutionService

/**
 * @author xinsheng
 * @date 2019/11/12
 */
@Component
class CompleteExecutionHandler(override val queue: Queue,
                               val executionService: ExecutionService
): MessageHandler<CompleteExecution> {
    val log = LoggerFactory.getLogger(javaClass)

    override val messageType = CompleteExecution::class.java

    override fun handle(message: CompleteExecution) {
        val execution = executionService.getExecution(message.executionId)

        if (execution.status == ExecutionStatus.SUCCEEDED || execution.status == ExecutionStatus.FAILED) {
            log.info(
                "execution has already finished, id: {}, name: {}",
                execution.id, execution.name
            )
            return
        }
        execution.status = message.executionStatus

        when (message.executionStatus) {
            ExecutionStatus.FAILED, ExecutionStatus.SUCCEEDED -> {
                log.info("executing status: ${JSON.toJSONString(message)}")
                executionService.updateExecution(execution)
            }
            else -> {
                log.info("failed to save execution status, only fail or success is valid")
                executionService.updateExecution(execution)
            }
        }
    }
}