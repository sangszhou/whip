package own.star.wheel.core.run.queue.config

/**
 * @author xinsheng
 * @date 2019/12/06
 */
data class SqlRetryProperties(
    var transactions: RetryProperties = RetryProperties(),
    var reads: RetryProperties = RetryProperties()
)

data class RetryProperties(
    var maxRetries: Int = 5,
    var backoffMs: Long = 100
)