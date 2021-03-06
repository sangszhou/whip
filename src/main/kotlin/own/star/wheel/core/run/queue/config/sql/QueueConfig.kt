package own.star.wheel.core.run.queue.config.sql

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.config.SqlQueueConfiguration
import com.netflix.spinnaker.q.Activator
import com.netflix.spinnaker.q.DeadMessageCallback
import com.netflix.spinnaker.q.MessageHandler
import com.netflix.spinnaker.q.Queue
import com.netflix.spinnaker.q.QueueExecutor
import com.netflix.spinnaker.q.QueueProcessor
import com.netflix.spinnaker.q.metrics.EventPublisher
import com.netflix.spinnaker.q.metrics.QueueEvent
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import own.star.wheel.core.run.queue.DumbDeadMsgCb
import own.star.wheel.core.run.queue.EnabledActivator
import own.star.wheel.core.run.queue.MutableClock
import own.star.wheel.core.run.queue.config.SqlRetryProperties
import own.star.wheel.core.run.queue.sql.SqlQueue
import java.time.Duration
import java.util.LinkedList
import java.util.Optional

/**
 * @author xinsheng
 * @date 2019/12/06
 */
@Configuration
open class QueueConfig {
    val log = LoggerFactory.getLogger(javaClass)

    @Bean
    open fun messageHandlerPool() =
        ThreadPoolTaskExecutor().apply {
            threadNamePrefix = "queue-thread-prefix"
            corePoolSize = 3
            maxPoolSize = 5
            setQueueCapacity(0)
        }


    /**
     * 这是在干嘛, 为什么要引入 object
     * activeCount 和 maximumPoolSize 是可见的嘛
     *
     */
    @Bean
    open fun queueExecutor(messageHandlerPool: ThreadPoolTaskExecutor) =
        object : QueueExecutor<ThreadPoolTaskExecutor>(messageHandlerPool) {
            override fun hasCapacity() =
                executor.threadPoolExecutor.run {
                    activeCount < maximumPoolSize
                }

            override fun availableCapacity() =
                executor.threadPoolExecutor.run {
                    maximumPoolSize - activeCount
                }
        }

    @Bean
    open fun enabledActivator() = EnabledActivator(true)

    @Bean
    open fun queueEventPublisher(
        applicationEventPublisher: ApplicationEventPublisher
    ) = object : EventPublisher {
        override fun publishEvent(event: QueueEvent) {
            applicationEventPublisher.publishEvent(event)
        }
    }

//    DO NOT DELETE
//    @Bean
//    open fun makeImMemQueue(publisher: EventPublisher): Queue {
//        val clock = MutableClock();
//        return InMemoryQueue(clock,
//            Duration.ofMinutes(1), LinkedList<DeadMessageCallback>(),
//            false, publisher);
//    }

    @Primary
    @Bean
    open fun makeSqlMemQueue(publisher: EventPublisher,
                             dslContext: DSLContext,
                             mapper: ObjectMapper): Queue {
        val clock = MutableClock();
        val queueName = "sqlQueue"
        val lockTtlTime = 100
        return SqlQueue(queueName,
            SqlQueueConfiguration.SCHEMA_VERSION,
            dslContext,
            clock, lockTtlTime, mapper,  Optional.empty(),
            deadMessageHandlers = LinkedList<DeadMessageCallback>(),
            publisher = publisher,
            sqlRetryProperties = SqlRetryProperties()
        )
    }

    @Bean
    open fun makeDeadMessageCallback(): DeadMessageCallback {
        return DumbDeadMsgCb()
    }

    @Bean
    open fun queueProcessor(
        queue: Queue,
        executor: QueueExecutor<*>,
        handlers: Collection<MessageHandler<*>>,
        activators: List<Activator>,
        publisher: EventPublisher,
        deadMessageHandler: DeadMessageCallback
    ): QueueProcessor {
        print("handle size ${handlers.size}")
        log.info("handler size: {}", handlers.size)

        return QueueProcessor(
            queue,
            executor,
            handlers,
            activators,
            publisher,
            deadMessageHandler,
            false,
            Duration.ofSeconds(0),
            Duration.ofSeconds(0)
        )
    }
}