package own.star.wheel.core.run.queue

import com.netflix.spinnaker.q.Activator

/**
 * @author xinsheng
 * @date 2019/11/12
 */
class EnabledActivator(override val enabled: Boolean = true) : Activator