package own.star.wheel.core.run.model.util

class RestResponse<T> (val success: Boolean) {

    var code: String ? = null
    var data: T? = null

    constructor(success: Boolean, code: String): this(success) {
        this.code = code
    }

    constructor(success: Boolean, code: String, data: T): this(success) {
        this.code = code
        this.data = data
    }

}