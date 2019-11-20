package own.star.wheel.core.run.dao

import com.alibaba.service.keep.model.Stage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.exception.SQLDialectNotSupportedException
import org.jooq.impl.DSL
import org.jooq.impl.DSL.field
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController
import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.ExecutionStatus
import java.lang.System.currentTimeMillis
import java.sql.ResultSet
import java.util.Date
import java.util.LinkedList
import javax.annotation.PostConstruct

/**
 * @author xinsheng
 * @date 2019/11/19
 */
@RestController
class PipelineTemplateDao(val dslContext: DSLContext, val mapper: ObjectMapper) {
    private val log = LoggerFactory.getLogger(javaClass)

    val pipelineTable = "pipeline_template"
    val pipelineInstance = "pipeline"
    val stage = "stage"

    @PostConstruct
    fun makePipeline() {
        log.info("making pipeline template dao")
    }

    fun retrievePipelineTemplate(id: String): Execution? {
        val table = DSL.table(pipelineTable)
        val resultSet = dslContext
            .select(field("content"))
            .from(table)
            .where(field("id").eq(id))
            .fetch().intoResultSet()

        if (resultSet.next()) {
            val execution = mapper.readValue<Execution>(resultSet.getString("content"))
            return execution
        }
        return null
    }

    /**
     * 获取所有的 stage 和 execution instance 信息
     */
    fun retrieveExecution(instanceId: String): Execution {
        val pipelineInstanceTbl = DSL.table(pipelineInstance)
        val stageInstanceTbl = DSL.table(stage)

        // 获取 stage instance 信息
        val resultSet = dslContext.select(field("*"))
            .from(pipelineInstanceTbl)
            .where(field("id").eq(instanceId))
            .fetch().intoResultSet()

        val execution = mapResultToExecution(resultSet)

        // TODO 如果没有的话该怎么处理呢?

        val stageList = retrieveExecutionStage(instanceId)

        execution!!.stages = stageList

        return execution
    }

    fun retrieveExecutionStage(exeId: String): List<Stage> {
        val stageInstanceTbl = DSL.table(stage)

        val resultSet = dslContext.select(field("*"))
            .from(stageInstanceTbl)
            .where(field("exe_id").eq(exeId))
            .fetch().intoResultSet()

        val listStage = LinkedList<Stage>()

        while (resultSet.next()) {
            val stage = mapResultToStage(resultSet)
            stage?.let { listStage.add(it) }
        }

        return listStage
    }

    fun retrieveStage(instanceId: String): Stage? {
        val stageInstanceTbl = DSL.table(stage)
        val resultSet = dslContext.select(field("*"))
            .from(stageInstanceTbl)
            .where(field("id").eq(instanceId))
            .fetch().intoResultSet()

        return mapResultToStage(resultSet)
    }

    fun mapResultToExecution(resultSet: ResultSet): Execution? {
        if (resultSet.next()) {
            val execution = Execution()
            execution.id = resultSet.getString("")
            execution.instanceId = resultSet.getString("")
            execution.executionStatus = ExecutionStatus.valueOf(resultSet.getString(""))
            execution.startTime = resultSet.getTime("")
            execution.endTime = resultSet.getTime("")

            return execution
        }
        return null
    }

    fun mapResultToStage(resultSet: ResultSet): Stage? {
        if (resultSet.next()) {
            val stage = Stage()
            stage.id = resultSet.getString("id")
            stage.instanceId = resultSet.getString("")
            stage.output = mapper.readValue(resultSet.getString(""))
            stage.context = mapper.readValue(resultSet.getString(""))
            stage.required = resultSet.getString("").split(",").toList()
            stage.startTime = resultSet.getTime("")
            stage.endTime = resultSet.getTime("")
            stage.status = ExecutionStatus.valueOf(resultSet.getString(""))

            return stage
        } else {
            return null
        }
    }


    /**
     * 插入的是实例
     */
    fun upsertExexuctionInstance(execution: Execution, storeStage: Boolean) {
        val pipelineInstanceTbl = DSL.table(pipelineInstance)
        val stageInstanceTbl = DSL.table(stage)

        val stageDef = execution.stages
        execution.stages = null

//        Random(currentTimeMillis()).nextInt()
        val insertPairs = mapOf(
            field("id") to execution.instanceId,
            field("template_id") to execution.id,
            field("start_time") to currentTimeMillis(),
            field("status") to ExecutionStatus.NOT_STARTED.toString())

        /**
         * 如果 end_time 没有设置会怎样呢
         */
        val updatePairs = mapOf(
            DSL.field("end_time") to execution.endTime,
            DSL.field("status") to execution.executionStatus
        )

        upsert(dslContext, pipelineInstanceTbl, insertPairs, updatePairs, execution.instanceId!!)

        // 插入到 stage 中, 不需要考虑 update stage 的情况
        if (!storeStage) {
            return
        }

        dslContext.deleteFrom(stageInstanceTbl)
            .where(field("").eq(execution.instanceId))
            .execute()

        //
        stageDef!!.forEach { storeStage(it) }
    }

    fun storeStage(stageDef: Stage) {
        val stageInstanceTbl = DSL.table(stage)

        val insertPair = mapOf(
            field("id") to stageDef.id,
            field("instance_id") to stageDef.instanceId,
            field("ref_id") to stageDef.refId,
            field("exe_id") to stageDef.execution!!.instanceId,
            field("type") to stageDef.type,
            field("name") to stageDef.name,
            field("context") to mapper.writeValueAsString(stageDef.context),
            field("context") to mapper.writeValueAsString(stageDef.context),
            field("output") to mapper.writeValueAsString(stageDef.output),
            field("start_time") to currentTimeMillis(),
            field("status") to stageDef.status
            )

        val updatePair = mapOf(
            field("ref_id") to stageDef.refId,
            field("type") to stageDef.type,
            field("name") to stageDef.name,
            field("context") to mapper.writeValueAsString(stageDef.context),
            field("output") to mapper.writeValueAsString(stageDef.output),
            field("end_time") to currentTimeMillis(),
            field("status") to stageDef.status
        )

        upsert(dslContext, stageInstanceTbl, insertPair, updatePair, stageDef.instanceId!!)
    }

    /**
     * 拆解成对 pipeline 和 stages 的存储两部分
     * execution 是静态额
     */
    fun upsertExecution(execution: Execution) {
        val table = DSL.table(pipelineTable)
        val content = mapper.writeValueAsString(execution)

        val insertPairs = mapOf(
            field("name") to execution.name,
            field("id") to execution.id,
            field("gmt_create") to Date(),
            field("gmt_modified") to Date(),
            field("trigger_interval") to execution.triggerInterval,
            field("content") to content)

        val updatePairs = mapOf(
            DSL.field("name") to execution.name,
            DSL.field("content") to content,
            DSL.field("trigger_interval") to execution.triggerInterval,
            DSL.field("gmt_modified") to currentTimeMillis()
        )

        upsert(dslContext,
            table,
            insertPairs,
            updatePairs,
            execution.id
        )
    }

    private fun upsert(
        ctx: DSLContext,
        table: Table<Record>,
        insertPairs: Map<Field<Any?>, Any?>,
        updatePairs: Map<Field<Any>, Any?>,
        updateId: String) {
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
            log.debug("Falling back to primitive upsert logic: ${e.message}")
            val exists = ctx.fetchExists(ctx.select().from(table).where(DSL.field("id").eq(updateId)).forUpdate())
            if (exists) {
                ctx.update(table).set(updatePairs).where(DSL.field("id").eq(updateId)).execute()
            } else {
                ctx.insertInto(table).columns(insertPairs.keys).values(insertPairs.values).execute()
            }
        }
    }
}