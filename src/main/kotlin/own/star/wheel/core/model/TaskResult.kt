package com.alibaba.service.keep.model

import own.star.wheel.core.model.ExecutionStatus


/**
 * @author xinsheng
 * @date 2019/11/12
 */
open class TaskResult() {

    constructor(status: ExecutionStatus): this() {
        this.status = status
    }

    lateinit var status: ExecutionStatus
    lateinit var outputs: HashMap<String, *>
}