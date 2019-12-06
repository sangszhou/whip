package own.star.wheel.core.run.queue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;

/**
 * @author xinsheng
 * @date 2019/11/11
 */
public class MutableClock extends Clock {

    private Instant instant;
    private final ZoneId zone;

    public MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    public MutableClock(Instant instant) {
        this(instant, ZoneId.systemDefault());
    }

    public MutableClock(ZoneId zone) {
        this(Instant.now(), zone);
    }

    public MutableClock() {
        this(Instant.now(), ZoneId.systemDefault());
    }

    @Override
    public MutableClock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Instant instant() {
        return instant;
    }

    public void incrementBy(TemporalAmount amount) {
        instant = instant.plus(amount);
    }

    public void instant(Instant newInstant) {
        instant = newInstant;
    }


}
