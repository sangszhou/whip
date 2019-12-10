package own.star.wheel.core.run.dao.mysql

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.exception.SQLDialectNotSupportedException
import org.jooq.impl.DSL
import org.jooq.impl.DSL.all
import org.jooq.impl.DSL.field
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RestController
import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.model.PipelineTemplate
import own.star.wheel.core.run.model.Stage
import java.lang.IllegalArgumentException
import java.lang.System.currentTimeMillis
import java.sql.ResultSet
import java.util.Arrays
import java.util.Date
import java.util.LinkedList
import javax.annotation.PostConstruct

/**
 * @author xinsheng
 * @date 2019/11/19
 */
@Service
class PipelineTemplateDao(val dslContext: DSLContext, val mapper: ObjectMapper) {
    private val log = LoggerFactory.getLogger(javaClass)

    val pipelineTemplateTable = "pipeline_template"
    /**
     * pipeline template 的一次执行叫做 execution
     */
    val pipelineInstanceTable = "execution"
    /**
     * stage 不缺分 template 和 instance, 都是 instance
     * 因为 template 的定义在 pipeline 中已经存在了
     */
    val stage = "stage"

    @PostConstruct
    fun makePipeline() {
        log.info("making pipeline template dao")
    }

    fun retrievePipelineTemplate(id: String): PipelineTemplate? {
        val table = DSL.table(pipelineTemplateTable)
        val resultSet = dslContext
            .select(field("content"))
            .from(table)
            .where(field("id").eq(id))
            .fetch().intoResultSet()

        if (resultSet.next()) {
            val template = mapper.readValue<PipelineTemplate>(resultSet.getString("content"))
            return template
        }

        return null
    }

    /**
     * 获取所有的 stage 和 execution instance 信息
     * execution 是 pipeline 的一次运行
     */
    fun retrieveExecution(instanceId: String): Execution {
        val pipelineInstanceTbl = DSL.table(pipelineInstanceTable)

        // 获取 stage instance 信息
        val resultSet = dslContext
            .select(
                field("id"), field("name"),
                field("template_id"), field("status"),
                field("start_time"), field("end_time")
            )
            .from(pipelineInstanceTbl)
            .where(field("id").eq(instanceId))
            .fetch().intoResultSet()

        val execution = mapResultToExecution(resultSet)
        val stageList = retrieveExecutionStage(execution!!)
        log.info("stage list size ${stageList.size}")
        execution!!.stages = stageList

        return execution
    }

    fun retrieveExecutionStage(execution: Execution): List<Stage> {
        val stageInstanceTbl = DSL.table(stage)

        val resultSet = dslContext.select(
            field("id"), field("instance_id"), field("ref_id"), field("type"),
            field("output"), field("context"), field("required"),
            field("name"), field("start_time"), field("end_time"),
            field("status"), field("execution_id")
        )
            .from(stageInstanceTbl)
            .where(field("execution_id").eq(execution.id))
            .fetch().intoResultSet()

        val listStage = LinkedList<Stage>()

        while (resultSet.next()) {
            val stage = mapResultToStage(resultSet)
            stage?.execution = execution
            stage?.let { listStage.add(it) }
        }

        return listStage
    }

    fun retrieveStage(instanceId: String): Stage? {
        val stageInstanceTbl = DSL.table(stage)
        val resultSet = dslContext.select(
            field("id"), field("instance_id"), field("ref_id"), field("type"),
            field("execution_id"), field("name"),
            field("output"), field("context"), field("required"),
            field("start_time"), field("end_time"), field("status")
        )
            .from(stageInstanceTbl)
            .where(field("id").eq(instanceId))
            .fetch().intoResultSet()

        if (resultSet.next()) {
            return mapResultToStage(resultSet)
        }

        return null
    }

    fun mapResultToExecution(resultSet: ResultSet): Execution? {
        if (resultSet.next()) {
            val execution = Execution()

            execution.id = resultSet.getString("id")
            execution.name = resultSet.getString("name")
            execution.templateId = resultSet.getString("template_id")
            execution.status = ExecutionStatus.valueOf(resultSet.getString("status"))
            execution.startTime = resultSet.getDate("start_time")
            execution.endTime = resultSet.getDate("end_time")

            return execution
        }

        return null
    }

    fun mapResultToStage(resultSet: ResultSet): Stage? {
        val stage = Stage()
        stage.id = resultSet.getString("id")
        stage.refId = resultSet.getString("ref_id")
        stage.type = resultSet.getString("type")
        stage.executionId = resultSet.getString("execution_id")
        stage.instanceId = resultSet.getString("instance_id")
        stage.name = resultSet.getString("name")
        resultSet.getString("output")?.let {  stage.output = mapper.readValue(it)}
        resultSet.getString("context")?.let { stage.context = mapper.readValue(it) }

        stage.required = LinkedList()
        resultSet.getString("required")?.let{stage.required = it.split(",").toList()}

        stage.startTime = resultSet.getDate("start_time")
        resultSet.getDate("end_time")?.let { stage.endTime = it }
        stage.status = ExecutionStatus.valueOf(resultSet.getString("status"))
        return stage
    }

    /**
     * 插入的是实例
     */
    fun upsertExecution(execution: Execution, storeStage: Boolean) {
        val pipelineInstanceTbl = DSL.table(pipelineInstanceTable)
        val stageInstanceTbl = DSL.table(stage)

        val stageData = execution.stages
        val insertPairs = mapOf(
            field("id") to execution.id,
            field("template_id") to execution.templateId,
            field("start_time") to execution.startTime,
            field("name") to execution.name,
            field("status") to ExecutionStatus.NOT_STARTED.name
        )

        /**
         * 如果 end_time 没有设置会怎样呢
         */
        val updatePairs = mapOf(
            DSL.field("end_time") to execution.endTime,
            DSL.field("status") to execution.status.name
        )

        upsert(dslContext, pipelineInstanceTbl, insertPairs, updatePairs, execution.id)

        // 插入到 stage 中, 不需要考虑 update stage 的情况
        if (!storeStage) {
            log.info("upsert execution without storing stages")
            return
        }

        dslContext.deleteFrom(stageInstanceTbl)
            .where(field("execution_id").eq(execution.id))
            .execute()

        /**
         * stage 必须要存在的, 所以用!也行
         */
        stageData.forEach { storeStage(it) }
    }

    /**
     * 如果 start time 不存在, 是不是就不需要存储了呢
     */
    fun storeStage(stageDef: Stage) {
        val stageInstanceTbl = DSL.table(stage)

        val insertPair = mapOf(
            field("id") to stageDef.id,
            field("instance_id") to stageDef.instanceId,
            field("ref_id") to stageDef.refId,
            field("execution_id") to stageDef.executionId,
            field("type") to stageDef.type,
            field("name") to stageDef.name,
            field("execution_id") to stageDef.execution!!.id,
            field("context") to mapper.writeValueAsString(stageDef.context),
            field("output") to mapper.writeValueAsString(stageDef.output),
            field("start_time") to stageDef.startTime,
            field("status") to stageDef.status.name
        )

        val updatePair = mapOf(
            field("context") to mapper.writeValueAsString(stageDef.context),
            field("output") to mapper.writeValueAsString(stageDef.output),
            field("end_time") to stageDef.endTime,
            field("status") to stageDef.status.name
        )

        upsert(dslContext, stageInstanceTbl, insertPair, updatePair, stageDef.instanceId!!)
    }

    /**
     * 首先要, validate 格式是否正确
     * 拆解成对 pipeline 和 stages 的存储两部分
     * execution 是静态额
     */
    fun upsertPipelineTemplate(template: PipelineTemplate) {
        validatePipelineTemplate(template)

        val table = DSL.table(pipelineTemplateTable)
        val content = mapper.writeValueAsString(template)

        val insertPairs = mapOf(
            field("name") to template.name,
            field("id") to template.id,
            field("gmt_create") to Date(),
            field("gmt_modified") to Date(),
            field("trigger_interval") to template.triggerInterval,
            field("content") to content
        )

        log.info("insert pairs: ${insertPairs}")

        val updatePairs = mapOf(
            DSL.field("name") to template.name,
            DSL.field("content") to content,
            DSL.field("trigger_interval") to template.triggerInterval,
            DSL.field("gmt_modified") to Date()
        )

        log.info("update pairs: ${updatePairs}")

        upsert(
            dslContext,
            table,
            insertPairs,
            updatePairs,
            template.id
        )
    }

    /**
     * 校验格式
     */
    fun validatePipelineTemplate(template: PipelineTemplate) {
        if (StringUtils.isEmpty(template.name)) {
            throw IllegalArgumentException("pipeline template should have name")
        }
        if (StringUtils.isEmpty(template.id)) {
            throw IllegalArgumentException("pipeline template should have id")
        }

        if (CollectionUtils.isEmpty(template.stages)) {
            throw IllegalArgumentException("pipeline template should have at least 1 stage")
        }
    }

    private fun upsert(
        ctx: DSLContext,
        table: Table<Record>,
        insertPairs: Map<Field<Any?>, Any?>,
        updatePairs: Map<Field<Any>, Any?>,
        updateId: String
    ) {

        log.info("upsert insert pair: ${insertPairs}, update pair: ${updatePairs}")
        // MySQL & PG support upsert concepts. A nice little efficiency here, we
        // can avoid a network call if the dialect supports it, otherwise we need
        // to do a select for update first.
        // TODO rz - Unfortunately, this seems to come at the cost of try/catching
        // to fallback to the simpler behavior.
        try {
            ctx.insertInto(table, *insertPairs.keys.toTypedArray())
                .values(insertPairs.values)
                .onDuplicateKeyUpdate()
                .set(updatePairs)
                .execute()
        } catch (e: SQLDialectNotSupportedException) {
            log.info("Falling back to primitive upsert logic: ${e.message}")
            val exists = ctx.fetchExists(ctx.select().from(table).where(DSL.field("id").eq(updateId)).forUpdate())
            if (exists) {
                ctx.update(table).set(updatePairs).where(DSL.field("id").eq(updateId)).execute()
            } else {
                ctx.insertInto(table).columns(insertPairs.keys).values(insertPairs.values).execute()
            }
        } catch (e: Exception) {
            log.error("upsert exception", e)
        }
    }
}