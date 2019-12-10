package own.star.wheel.core.run.handler

import com.netflix.spinnaker.q.MessageHandler
import com.netflix.spinnaker.q.Queue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.CollectionUtils
import own.star.wheel.core.run.model.CompleteExecution
import own.star.wheel.core.run.model.CompleteStage
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.model.Stage
import own.star.wheel.core.run.model.StartStage
import own.star.wheel.core.run.service.ExecutionService
import java.time.Duration
import java.util.Date

/**
 * @author xinsheng
 * @date 2019/11/12
 */

@Component
class CompleteStageHandler(
    override val queue: Queue,
    val executionService: ExecutionService
) : MessageHandler<CompleteStage> {
    val log = LoggerFactory.getLogger(javaClass)
    override val messageType: Class<CompleteStage> = CompleteStage::class.java

    override fun handle(message: CompleteStage) {
        val execution = executionService.getExecution(message.executionId)
        val theStage = execution.stages
            .first { it.id.equals(message.stageId) }

        if (theStage.status.equals(ExecutionStatus.RUNNING)) {
            // 如果是 running, 那这是我们的所期待的状态

            theStage.endTime = Date()
            theStage.status = message.executionStatus
            executionService.saveStage(theStage)

            when (message.executionStatus) {
                ExecutionStatus.SUCCEEDED -> {
                    // find downstream stages
                    log.info("find down stream stages")
                    triggerDownstreamTask(theStage)
                }

                ExecutionStatus.FAILED -> {
                    log.info("failed tasks")
                    delayReportError(theStage, message.message)
                }
            }
        } else {
            // Unknown?
            // else return error
            log.error("try to complete a invalid state stage: {}", theStage.status)
        }
    }

    fun triggerDownstreamTask(theStage: Stage) {
        val execution = theStage.execution
        val stageList = theStage.execution!!.stages
            .filter { it.required!!.contains(theStage.refId) }

        if (!CollectionUtils.isEmpty(stageList)) {
            stageList.stream().forEach { queue.push(StartStage(execution!!.id, it.id)) }
        } else {
            // finish execution
            queue.push(CompleteExecution(execution!!.id, ExecutionStatus.SUCCEEDED, "success"))
        }
    }

    /**
     * stage 执行卡主了, 要通知上游响应
     */
    fun delayReportError(stage: Stage, msg: String) {
        // collect data, which step failed
        // save report to db
        // 发出一个延迟消息, 20 分钟后统计相关的信息
        queue.push(
            CompleteExecution(
                stage.execution!!.id, ExecutionStatus.FAILED,
                stage.name + " failed: " + msg
            ), Duration.ofMinutes(10)
        )
    }

}