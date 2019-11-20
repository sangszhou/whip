package com.alibaba.service.keep.model

import com.netflix.spinnaker.q.Message

/**
 * @author xinsheng
 * @date 2019/11/12
 */
class StartExecution(): Message() {

    constructor(executionId: String):this(){
        this.executionId = executionId
    }

    lateinit var executionId: String
}