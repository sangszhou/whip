package own.star.wheel.core.run.model
import own.star.wheel.core.run.model.ExecutionStatus


/**
 * @author xinsheng
 * @date 2019/11/12
 */
open class TaskResult(var status: ExecutionStatus) {

    constructor(status: ExecutionStatus, outputs: HashMap<String, *>): this(status) {
        this.status = status
        this.outputs = outputs
    }

    var outputs: HashMap<String, *>? = null
}