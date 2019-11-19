package com.alibaba.service.keep.persistance.sql

import com.alibaba.fastjson.JSON
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.exception.SQLDialectNotSupportedException
import org.jooq.impl.DSL
import org.jooq.impl.DSL.field
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import own.star.wheel.core.model.Execution
import java.lang.System.currentTimeMillis

/**
 * @author xinsheng
 * @date 2019/11/19
 */
@Component
class PipelineTemplateDao(val dslContext: DSLContext) {
    private val log = LoggerFactory.getLogger(javaClass)

    val pipelineTable = "pipeline_template"

    /**
     * 拆解成对 pipeline 和 stages 的存储两部分
     * execution 是静态额
     */
    fun upsertExecution(execution: Execution) {
        val table = DSL.table(pipelineTable)
        val content = JSON.toJSONString(execution)

        val insertPairs = mapOf(
            field("name") to execution.name,
            field("id") to execution.id,
            field("gmt_create") to currentTimeMillis(),
            field("gmt_modified") to currentTimeMillis(),
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