package own.star.wheel.core.run.queue.sql

import de.huxhorn.sulky.ulid.ULID
import org.junit.Assert.*
import org.junit.Test

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


}