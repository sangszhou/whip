package com.alibaba.service.keep.provider.queue

import com.netflix.spinnaker.q.DeadMessageCallback
import com.netflix.spinnaker.q.Message
import com.netflix.spinnaker.q.Queue

/**
 * @author xinsheng
 * @date 2019/11/12
 */
open class DumbDeadMsgCb: DeadMessageCallback{
    override fun invoke(p1: Queue, p2: Message) {
    }
}