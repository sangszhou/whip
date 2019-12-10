package com.netflix.spinnaker.kork.web.exceptions;

/**
 * @author xinsheng
 * @date 2019/12/10
 */


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationListener

class DefaultThreadUncaughtExceptionHandler : Thread.UncaughtExceptionHandler,
    ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private var priorHandler: Thread.UncaughtExceptionHandler? = null

    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val isEnabled =
            event.environment.getProperty("globalExceptionHandlingEnabled", Boolean::class.javaPrimitiveType!!, true)

        if (isEnabled) {
            priorHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(this)
        }
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        logger.error("Uncaught exception in thread", exception)

        if (priorHandler != null) {
            priorHandler!!.uncaughtException(thread, exception)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultThreadUncaughtExceptionHandler::class.java)
    }
}
