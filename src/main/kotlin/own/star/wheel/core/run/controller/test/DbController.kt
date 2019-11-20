package own.star.wheel.core.run.controller.test

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import own.star.wheel.core.run.dao.PipelineTemplateDao
import own.star.wheel.core.run.model.Execution
import javax.annotation.PostConstruct

/**
 * @author xinsheng
 * @date 2019/11/20
 */
@RestController
@RequestMapping("/db")
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
    fun upsertPipeline(@RequestBody exe: Execution): String {
        log.info("upsert execution {}", mapper.writeValueAsString(exe))
        try {
            pipelineTemplateDao.upsertExecution(exe)
        } catch (exp: Exception) {
            log.info("failed to save to db", exp)
            return exp.message!!
        }

        return exe.id
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
}