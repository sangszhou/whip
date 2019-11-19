package com.alibaba.service.keep.persistance.sql

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * @author xinsheng
 * @date 2019/11/19
 */
@Configuration
open class JooqConfiguration() {

//    @Value("datasource.url")
//    lateinit var datasourceUrl: String;
//    @Value("datasource.driverClass")
//    lateinit var driverClass: String
//    @Value("datasource.username")
//    lateinit var userName: String
//    @Value("datasource.password")
//    lateinit var password: String

//    @Bean
//    open fun makeDataSource(): DataSource {
//        val dataSource = DataSourceBuilder.create()
//        dataSource.url(datasourceUrl)
//        dataSource.driverClassName(driverClass)
//        dataSource.username(userName)
//        dataSource.password(password)
//        return dataSource.build()
//    }

    /**
     * 需要每次创建还是用完就释放
     */
    @Bean
    open fun makeDslContext(dataSource: DataSource): DSLContext {
        val dslContext = DSL.using(dataSource.getConnection());
        return dslContext;
    }
}