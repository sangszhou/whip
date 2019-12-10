package own.star.wheel.core.run.model
import com.fasterxml.jackson.annotation.JsonTypeName
import com.netflix.spinnaker.q.Message
import own.star.wheel.core.run.model.ExecutionStatus


/**
 * @author xinsheng
 * @date 2019/11/12
 */
@JsonTypeName("completeExecution")
data class CompleteExecution(val executionId: String,
                             val executionStatus: ExecutionStatus,
                             val message: String): Message()
