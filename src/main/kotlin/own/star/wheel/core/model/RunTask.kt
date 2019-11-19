package com.alibaba.service.keep.model

import com.netflix.spinnaker.q.Message


/**
 * @author xinsheng
 * @date 2019/11/12
 */

class RunTask() : Message() {

    constructor(executionId: String, stageId: String, taskId: String, taskType: String):this(){
        this.executionId = executionId
        this.stageId = stageId
        this.taskId = taskId
        this.taskType = taskType
    }


    lateinit var taskId: String
    lateinit var stageId: String
    lateinit var executionId: String
    lateinit var taskType: String

}