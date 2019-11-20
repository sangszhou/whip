package own.star.wheel.core.run.controller.test

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * @author xinsheng
 * @date 2019/11/19
 */
@Configuration
open class JooqConfiguration {
    val log = LoggerFactory.getLogger(javaClass)
    /**
     * 需要每次创建还是用完就释放
     */
    @Bean
    open fun makeDslContext(dataSource: DataSource): DSLContext {
        log.info("making dsl context")
        val dslContext = DSL.using(dataSource.getConnection());
        return dslContext;
    }
}