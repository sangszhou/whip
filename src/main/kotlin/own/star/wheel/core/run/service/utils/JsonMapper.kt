package own.star.wheel.core.run.service.utils

import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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

        return objectMapper
    }

}