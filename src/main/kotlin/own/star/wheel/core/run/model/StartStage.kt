package com.alibaba.service.keep.model

import com.netflix.spinnaker.q.Message

/**
 * @author xinsheng
 * @date 2019/11/12
 */

open class StartStage() : Message() {

//    constructor()
    constructor(executionId: String, stageId: String):this(){
        this.executionId = executionId
        this.stageId = stageId
    }

    lateinit var executionId: String
    lateinit var stageId: String
}