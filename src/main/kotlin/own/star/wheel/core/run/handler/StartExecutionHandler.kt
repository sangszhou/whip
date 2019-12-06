package own.star.wheel.core.run.handler

import com.alibaba.service.keep.model.StartExecution
import com.alibaba.service.keep.model.StartStage
import com.netflix.spinnaker.q.MessageHandler
import com.netflix.spinnaker.q.Queue
import com.sun.tools.javadoc.Start
import org.springframework.stereotype.Component
import org.springframework.util.CollectionUtils
import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.ExecutionStatus
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
    override val messageType: Class<StartExecution> = StartExecution::class.java

    override fun handle(startExecution: StartExecution) {
        val execution = executionService.getExecution(startExecution.executionId)
        startExecution(execution)
    }

    private fun startExecution(execution: Execution) {
        val stages = execution.initialStages()
        if (CollectionUtils.isEmpty(stages)) {
            execution.status = ExecutionStatus.SUCCEEDED
            executionService.updateExecution(execution)
        } else {
            stages.forEach {queue.push(StartStage(execution.id, it.id))}
            execution.status = ExecutionStatus.RUNNING
            executionService.updateExecution(execution)
        }
    }
}