package own.star.wheel.core.run.service

import com.netflix.spinnaker.q.Queue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.StartExecution

/**
 * @author xinsheng
 * @date 2019/12/10
 */
interface ExecutionRunner {
    @Throws(Exception::class)
    fun start(execution: Execution)
}

@Component
class QueueExecutionRunner : ExecutionRunner {
    @Autowired
    lateinit var queue: Queue

    @Throws(Exception::class)

    override fun start(execution: Execution) {
        queue.push(StartExecution(execution.id))
    }
}
