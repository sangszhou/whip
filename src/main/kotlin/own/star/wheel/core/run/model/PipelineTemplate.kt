package own.star.wheel.core.run.model

import com.alibaba.service.keep.model.Stage
import java.util.ArrayList

class PipelineTemplate {
    lateinit var id: String
    lateinit var name: String
    var triggerInterval: Long? = null
    var stages: List<Stage>? = ArrayList()
}