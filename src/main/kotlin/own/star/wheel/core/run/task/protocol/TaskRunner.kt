package own.star.wheel.core.run.task.protocol

import own.star.wheel.core.run.model.Stage
import own.star.wheel.core.run.model.TaskResult

/**
 * @author xinsheng
 * @date 2019/11/12
 */
interface TaskRunner {

    fun execute(stage: Stage): TaskResult

    fun getName(): String {
        return javaClass.simpleName
    }

}