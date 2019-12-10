package own.star.wheel.core.run.task.impl

import org.springframework.stereotype.Component
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.model.Stage
import own.star.wheel.core.run.model.TaskResult
import own.star.wheel.core.run.task.protocol.TaskRunner

/**
 * @author xinsheng
 * @date 2019/12/10
 */
@Component
class EchoTask: TaskRunner {
    override fun execute(stage: Stage): TaskResult {
        println("echo task executed")
        println("echo task data: ${stage}")

        return TaskResult(ExecutionStatus.SUCCEEDED)
    }

    override fun getName(): String {
        return "echoTask"
    }
}