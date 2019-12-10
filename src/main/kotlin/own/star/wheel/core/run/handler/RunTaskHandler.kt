package own.star.wheel.core.run.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.q.MessageHandler
import com.netflix.spinnaker.q.Queue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import own.star.wheel.core.run.model.CompleteStage
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.model.RunTask
import own.star.wheel.core.run.model.Stage
import own.star.wheel.core.run.model.TaskResult
import own.star.wheel.core.run.service.ExecutionService
import own.star.wheel.core.run.task.protocol.TaskRunner
import java.time.Duration
import java.util.Date

/**
 * @author xinsheng
 * @date 2019/11/12
 */
@Component
class RunTaskHandler(
    override val queue: Queue,
    val taskRunnerList: List<TaskRunner>,
    val executionService: ExecutionService,
    val objectMapper: ObjectMapper
) : MessageHandler<RunTask> {
    val log = LoggerFactory.getLogger(javaClass)

    override val messageType: Class<RunTask> = RunTask::class.java

    override fun handle(message: RunTask) {
        log.info("run task handler message: ${message}")
        val execution = executionService.getExecution(message.executionId)
        log.info("run task handler execution: ${execution}")
        val theStage = execution.stages
            .first { it.id.equals(message.stageId) }

        if (execution.isCompleted()) {
            log.info("run task handler finished: {}:{}", execution.id, execution.name)
            return
        }

        val taskRunner = taskRunnerList
            .first{ it.getName() == message.taskType }

        val taskResult = taskRunner.execute(theStage)

        // 不管结果正确与否, 都写入 context, 暂时不考虑失败后继续的场景
        mergeOutputToContext(taskResult, theStage)

        // 把 output 写到 stage 中去
        when (taskResult.status) {
            ExecutionStatus.RUNNING -> {
//                taskRunner is Retryable
                if (false) {
//                    queue.push(message, Duration.ofMillis((taskRunner as Retryable).backoffPeriod))
                    println("console")
                } else {
                    log.error("cannot return running ...")
                    queue.push(
                        CompleteStage(
                            execution.id, theStage.id,
                            ExecutionStatus.FAILED,
                            "non-retryable task return running which is invalid")
                    )
                }
            }

            ExecutionStatus.SUCCEEDED -> {
                queue.push(CompleteStage(execution.id, theStage.id,
                    ExecutionStatus.SUCCEEDED, "success"))
            }

            ExecutionStatus.FAILED -> {
                queue.push(CompleteStage(execution.id, theStage.id,
                    ExecutionStatus.FAILED, "failed"))
            }

            else -> queue.push(
                CompleteStage(
                    execution.id,
                    theStage.id,
                    ExecutionStatus.FAILED,
                    "unknown task result status " + taskResult.status.toString())
            )
        }
    }

    /**
     * 更新数据到数据库, 这里并不会放到下一个 stage 中, 在下一个 stage 需要获取的时候, 直接从所有的
     * ancester 中获取即可
     */
    private fun mergeOutputToContext(taskResult: TaskResult, theStage: Stage) {
        // 及时写入数据库

        taskResult.outputs?.let {
            log.info("merge task result: ${taskResult.outputs} to stage: ${theStage.context}")
            theStage.output = it
            // 及时写入数据库
            executionService.saveStage(theStage)
        }

    }

}