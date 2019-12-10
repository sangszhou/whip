package own.star.wheel.core.run.queue.sql

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.netflix.spinnaker.q.Message
import de.huxhorn.sulky.ulid.ULID
import org.junit.Test
import own.star.wheel.core.run.model.StartExecution

/**
 * @author xinsheng
 * @date 2019/12/10
 */
class SqlQueueTest {
    @Test
    fun playWithUid() {
        val ulid: ULID = ULID()
        val v1 = ulid.nextValue()
        println(v1)

        val v2 = ulid.nextMonotonicValue(v1)
        println(v2)
    }

    @Test
    fun seri() {
        val objectMapper = ObjectMapper()
        objectMapper.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        objectMapper.registerModule(Jdk8Module())
        objectMapper.registerModule(KotlinModule())
        objectMapper.registerSubtypes(StartExecution::class.java)
        val se = StartExecution("123")
        println(objectMapper.writeValueAsString(se))


        println(objectMapper.writeValueAsString(objectMapper.readValue<Message>(objectMapper.writeValueAsString(se))))

    }
}