package own.star.wheel.core.run.controller.test

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import own.star.wheel.core.run.dao.mysql.PipelineTemplateDao
import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.model.PipelineTemplate
import java.sql.Time
import java.util.Date
import java.util.UUID
import javax.annotation.PostConstruct

/**
 * @author xinsheng
 * @date 2019/11/20
 */
@RestController
class DbController {
    val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    open fun makePipeline() {
        log.info("making DbController")
    }

    @Autowired
    lateinit var pipelineTemplateDao: PipelineTemplateDao
    @Autowired
    lateinit var mapper: ObjectMapper

    @PostMapping("/pipeline/save")
    fun upsertPipeline(@RequestBody template: PipelineTemplate): String {
        log.info("upsert execution {}", mapper.writeValueAsString(template))

        try {
            pipelineTemplateDao.upsertPipelineTemplate(template)
        } catch (exp: Exception) {
            log.info("failed to save to db", exp)
            return exp.message!!
        }

        return template.id
    }

    @GetMapping("/pipeline")
    fun getPipeline(id: String): String {
        log.info("get pipeline id: {}", id)
        try {
            val pipeline = pipelineTemplateDao.retrievePipelineTemplate(id)
            return mapper.writeValueAsString(pipeline)
        } catch (exp: Exception) {
            log.error("failed to query db", exp)
            return id
        }
    }

    /**
     * 包括参数填充和一些其他的东西
     */
    @PostMapping("/pipeline/trigger")
    fun triggerExecution(@RequestParam("pipelineTemplateId") pipelineTemplateId: String,
                         @RequestBody context: Map<String, String>): String {
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

            // 别忘记了 对于 stage 的初始化操作
            execution.stages?.forEach {
                it.execution = execution
                it.instanceId = UUID.randomUUID().toString()
                it.status = ExecutionStatus.NOT_STARTED
            }
            log.info("execution data: ${execution}")

            pipelineTemplateDao.upsertExecution(execution, true)
            return execution.id
        } catch (exp: java.lang.Exception) {
            log.error("failed to save data to db", exp)
            return exp.message.toString()
        }
    }
}