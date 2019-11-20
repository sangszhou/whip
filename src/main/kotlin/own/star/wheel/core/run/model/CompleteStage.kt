package com.alibaba.service.keep.model

import com.netflix.spinnaker.q.Message
import own.star.wheel.core.run.model.ExecutionStatus

/**
 * @author xinsheng
 * @date 2019/11/12
 */

class CompleteStage(): Message() {

    constructor(executionId: String, stageId: String, executionStatus: ExecutionStatus, message: String):this(){
        this.executionId = executionId
        this.executionStatus = executionStatus
        this.message = message
        this.stageId = stageId
    }

    lateinit var executionId: String
    lateinit var stageId: String
    lateinit var executionStatus: ExecutionStatus
    lateinit var message: String



}