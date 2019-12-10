package own.star.wheel.core.run.model
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.util.CollectionUtils
import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.ExecutionStatus
import java.sql.Time
import java.util.Date
import java.util.LinkedList

/**
 * @author xinsheng
 * @date 2019/11/12
 */

class Stage() {

    /**
     * 模板数据
     */
    lateinit var id: String
    lateinit var refId: String
    lateinit var type: String
    lateinit var name: String

    /**
     * 实例数据, 可以为空
     */
    var instanceId: String? = null
    var executionId: String? = null

    var execution: Execution? = null
    var context: HashMap<String, Any> = LinkedHashMap<String, Any>()
    var output: HashMap<String, Any> = LinkedHashMap<String, Any>()

    var required: List<String>? = null
    var startTime: Date? = null
    var endTime: Date? = null
    var status: ExecutionStatus = ExecutionStatus.NOT_STARTED


    open fun upstreamStages(): List<Stage> {
        return execution!!.stages.filter { required!!.contains(it.refId) }.toList()
    }

    open fun allUpstreamSuccess(): Boolean {
        return upstreamStages().all {it.status == ExecutionStatus.SUCCEEDED}
    }

    open fun downstreamStages(): List<Stage> {
        return execution!!.stages.filter { it.required!!.contains(refId) }.toList()
    }

    open fun initial(): Boolean {
        return CollectionUtils.isEmpty(required)
    }

    /**
     * 没有正逆序关系, 直接拿
     */
    fun getOutput(key: String): Any? {
        return execution!!.stages.first { it.output.containsKey(key) }.output.get(key)
    }

}