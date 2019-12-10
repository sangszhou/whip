package own.star.wheel.core.run.dao

import own.star.wheel.core.run.model.Execution
import own.star.wheel.core.run.model.ExecutionStatus
import own.star.wheel.core.run.model.Stage

/**
 * @author xinsheng
 * @date 2019/11/12
 */
interface ExecutionRepository {
    fun upsertExecution(execution: Execution)

    fun upsertStage(stage: Stage)

    fun retrieve(id: String): Execution

    fun retrieveStage(stageId: String): Stage

    fun updateStageStatus(stage: Stage)
    fun updateExecutionStatus(id: String, status: ExecutionStatus)

}
