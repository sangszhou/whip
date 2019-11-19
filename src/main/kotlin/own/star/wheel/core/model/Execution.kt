package own.star.wheel.core.model

import com.alibaba.service.keep.model.Stage
import java.util.ArrayList
import kotlin.streams.toList

/**
 * @author xinsheng
 * @date 2019/11/12
 */
class Execution() {
    /**
     * 本次释放的 id
     */
    lateinit var id: String
    lateinit var name: String
    var startTime: Long? = null
    var endTime: Long? = null
    var triggerInterval: Long? = null


    var stages: List<Stage> = ArrayList()
    var executionStatus = ExecutionStatus.NOT_STARTED

    fun initialStages(): List<Stage> {
        return stages.stream().filter{ it.isInitial() }.toList()

    }

    fun isCompleted(): Boolean {
        return executionStatus == ExecutionStatus.SUCCEEDED || executionStatus == ExecutionStatus.FAILED
    }

}