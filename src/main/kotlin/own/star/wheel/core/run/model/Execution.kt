package own.star.wheel.core.run.model

import com.alibaba.service.keep.model.Stage
import java.sql.Time
import java.util.ArrayList
import kotlin.streams.toList

/**
 * @author xinsheng
 * @date 2019/11/12
 */
class Execution() {
    /**
     * template definition
     */
    lateinit var id: String
    lateinit var name: String
    var triggerInterval: Long? = null
    var stages: List<Stage>? = ArrayList()

    /**
     * runtime definition
     */
    var instanceId: String? = null
    var startTime: Time? = null
    var endTime: Time? = null
    var executionStatus = ExecutionStatus.NOT_STARTED

    fun initialStages(): List<Stage> {
        return stages!!.stream().filter{ it.initial() }.toList()
    }

    fun isCompleted(): Boolean {
        return executionStatus == ExecutionStatus.SUCCEEDED || executionStatus == ExecutionStatus.FAILED
    }

}