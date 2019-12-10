package own.star.wheel.core.run.handler

import com.netflix.spinnaker.q.MessageHandler
import com.netflix.spinnaker.q.Queue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.model.RunTask
import own.star.wheel.core.run.model.Stage
import own.star.wheel.core.run.model.StartStage
import own.star.wheel.core.run.service.ExecutionService
import java.util.Date
import java.util.UUID

/**
 * @author xinsheng
 * @date 2019/11/12
 */
@Component
class StartStageHandler (
    override val queue: Queue,
    val executionService: ExecutionService
) : MessageHandler<StartStage> {
    val log = LoggerFactory.getLogger(javaClass)

    override val messageType: Class<StartStage> = StartStage::class.java

    override fun handle(message: StartStage) {
        val execution = executionService.getExecution(message.executionId)
        val theStage = execution.stages
            .first{ it -> it.id.equals(message.stageId, ignoreCase = true) }
        // 前序任务还没有完成, 这里直接跳过即可, 不需要前进
        if (!theStage.allUpstreamSuccess()) {
            log.info("the stage name: {} upstream stage has not completed yet: ${theStage.name}")
            return
        }

        // 对自身状态的判断
        if (theStage.status === ExecutionStatus.NOT_STARTED) {
            theStage.startTime = Date()
            theStage.status = ExecutionStatus.RUNNING
            // TODO executionRepository.storeStage(theStage)
            executionService.saveStage(theStage)

            // start stage
            startTask(theStage)
        } else if (theStage.status === ExecutionStatus.RUNNING) {
            // 正常, 不应该在 stage 上进行重试, 可能是驱动触发
            log.info("staging is already running")
        } else {
            // 结束状态, 这个也不应该继续重试了
            log.info("stage unknown status: {}", theStage.status)
        }
    }

    fun startTask(stage: Stage) {
        val type = stage.type
        val taskId = type + "_" + UUID.randomUUID().toString().substring(5, 10)
        log.info("startStage send task : ${type}, ${stage.execution!!.id}")
//        queue.push(RunTask(taskId, stage.id, stage.execution!!.id, type))
        queue.push(RunTask(stage.execution!!.id, stage.id, taskId, type))
    }

}