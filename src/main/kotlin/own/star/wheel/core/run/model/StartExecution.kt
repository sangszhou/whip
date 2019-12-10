package own.star.wheel.core.run.model
import com.fasterxml.jackson.annotation.JsonTypeName
import com.netflix.spinnaker.q.Message

/**
 * @author xinsheng
 * @date 2019/11/12
 */
@JsonTypeName("startExecution")
data class StartExecution(val executionId: String): Message()