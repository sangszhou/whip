package own.star.wheel.core.run.service

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import own.star.wheel.core.run.dao.mysql.PipelineTemplateDao
import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.model.PipelineTemplate
import own.star.wheel.core.run.model.Stage
import java.util.Date
import java.util.UUID

@Service
class ExecutionService(val pipelineTemplateDao: PipelineTemplateDao,
                       val objectMapper: ObjectMapper) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getPipelineTemplate(id: String): PipelineTemplate? {
        return pipelineTemplateDao.retrievePipelineTemplate(id)
    }

    fun getExecution(id: String): Execution {
        return pipelineTemplateDao.retrieveExecution(id)
    }

    fun updateExecution(execution: Execution) {
        return pipelineTemplateDao.upsertExecution(execution, false)
    }

    fun saveStage(stg: Stage) {
        return pipelineTemplateDao.storeStage(stg)
    }

    fun savePipelineTemplate(template: PipelineTemplate) {
        try {
            pipelineTemplateDao.upsertPipelineTemplate(template)
        } catch (exp: Exception) {
            log.info("failed to save to db", exp)
            throw exp
        }
    }

    /**
     * 触发一次 pipeline 操作
     */
    fun triggerPipeline(pipelineTemplateId: String, context: Map<String, String>): String {
        log.info("trigger execution with ${pipelineTemplateId}, context: ${context}")

        try {
            val pipelineTemplate = pipelineTemplateDao.retrievePipelineTemplate(pipelineTemplateId)

            if (pipelineTemplate == null) {
                return "not found"
            }

            val execution = Execution()
            execution.id = UUID.randomUUID().toString()
            execution.templateId = pipelineTemplate.id
            execution.name = pipelineTemplate.name
            execution.startTime = Date()
            execution.status = ExecutionStatus.NOT_STARTED
            execution.stages = pipelineTemplate.stages

            // 别忘记了 对于 stage 的初始化操作, 这个可以再优化一些
            execution.stages.forEach {
                it.execution = execution
                it.executionId = execution.id
                it.instanceId = UUID.randomUUID().toString()
                it.status = ExecutionStatus.NOT_STARTED
            }

            // 有循环以来的问题, 所以无法 write as string, 蛋疼
            log.info("execution data: ${JSONObject.toJSONString(execution)}")

            pipelineTemplateDao.upsertExecution(execution, true)
            return execution.id
        } catch (exp: java.lang.Exception) {
            log.error("failed to save data to db", exp)
            throw exp
        }

    }
}