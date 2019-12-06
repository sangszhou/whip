package own.star.wheel.core.run.queue

import com.alibaba.service.keep.provider.queue.DumbDeadMsgCb
import com.alibaba.service.keep.provider.queue.EnabledActivator
import com.netflix.spinnaker.q.Activator
import com.netflix.spinnaker.q.DeadMessageCallback
import com.netflix.spinnaker.q.MessageHandler
import com.netflix.spinnaker.q.Queue
import com.netflix.spinnaker.q.QueueExecutor
import com.netflix.spinnaker.q.QueueProcessor
import com.netflix.spinnaker.q.memory.InMemoryQueue
import com.netflix.spinnaker.q.metrics.EventPublisher
import com.netflix.spinnaker.q.metrics.QueueEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.Duration
import java.util.LinkedList

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

    @Bean
    open fun makeQueue(publisher: EventPublisher): Queue {
        val clock = MutableClock();
        return InMemoryQueue(clock,
            Duration.ofMinutes(1), LinkedList<DeadMessageCallback>(),
            false, publisher);
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