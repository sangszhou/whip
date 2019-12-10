package own.star.wheel.core.run.model
import com.fasterxml.jackson.annotation.JsonTypeName
import com.netflix.spinnaker.q.Message
import own.star.wheel.core.run.model.ExecutionStatus

/**
 * @author xinsheng
 * @date 2019/11/12
 */

@JsonTypeName("completeStage")
data class CompleteStage(val executionId: String,
                         val stageId: String,
                         val executionStatus: ExecutionStatus,
                         val message: String): Message()