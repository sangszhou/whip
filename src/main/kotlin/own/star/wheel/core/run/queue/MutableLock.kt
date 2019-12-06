package own.star.wheel.core.run.queue

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * @author xinsheng
 * @date 2019/12/06
 */
class MutableLock(instant: Instant, zone: ZoneId) : Clock() {
    override fun withZone(zone: ZoneId?): Clock {
        return MutableClock(instant(), zone)
    }

    override fun getZone(): ZoneId {
        return zone
    }

    override fun instant(): Instant {
        return instant()
    }


}