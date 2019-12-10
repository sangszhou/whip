package own.star.wheel.core.run.service.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.netflix.spinnaker.q.AckAttemptsAttribute
import com.netflix.spinnaker.q.AttemptsAttribute
import com.netflix.spinnaker.q.MaxAttemptsAttribute
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import own.star.wheel.core.run.model.CompleteExecution
import own.star.wheel.core.run.model.CompleteStage
import own.star.wheel.core.run.model.RunTask
import own.star.wheel.core.run.model.StartExecution
import own.star.wheel.core.run.model.StartStage
import java.util.Arrays

/**
 * @author xinsheng
 * @date 2019/11/20
 */
@Configuration
open class JsonMapper {

    @Bean
    open fun makeObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        objectMapper.registerModule(Jdk8Module())
//        objectMapper.registerModule(GuavaModule())
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(KotlinModule())
//        objectMapper.disable(READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
//        objectMapper.disable(WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
//        objectMapper.disable(FAIL_ON_UNKNOWN_PROPERTIES)
//        objectMapper.setSerializationInclusion(NON_NULL)

        // 注册类型
        objectMapper.registerSubtypes(StartExecution::class.java)
        objectMapper.registerSubtypes(StartStage::class.java)
        objectMapper.registerSubtypes(CompleteStage::class.java)
        objectMapper.registerSubtypes(CompleteExecution::class.java)
        objectMapper.registerSubtypes(RunTask::class.java)

        objectMapper.registerSubtypes(AttemptsAttribute::class.java)
        objectMapper.registerSubtypes(MaxAttemptsAttribute::class.java)
        objectMapper.registerSubtypes(AckAttemptsAttribute::class.java)

        objectMapper.registerModule(KotlinModule())
        return objectMapper
    }




}