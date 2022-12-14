package org.zrnq.exam.exception

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.serializer.SerializerFeature
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

enum class ResponseCode(val msg : String) {
    SUCCESS("操作成功"),
    BATCH_OP_OK("检查通过"),

    TOKEN_EXPIRED("JWT token过期"),
    TOKEN_INVALID("JWT token无效"),
    FORBIDDEN("用户未登录或没有权限"),
    CONTENT_TYPE_REJECTED("请求的Content-Type不支持"),
    REQUEST_METHOD_NOT_SUPPORTED("请求的方法不支持"),

    TARGET_NOT_FOUND("操作对象不存在"),
    ILLEGAL_PARAMETER("参数错误"),
    PARAMETER_MISSING("缺少参数"),
    BAD_CREDENTIALS("密码错误"),
    FILE_SIZE_LIMIT("图片大小超出限制"),

    IDENTIFIER_ALREADY_EXISTS("名称已被使用"),
    FACE_RESULT_NOT_READY("人脸识别结果未准备好"),
    LAST_FACE_REQUEST_INCOMPLETE("上一次人脸识别请求未完成"),
    FACE_RECOGNITION_BUSY("人脸识别系统繁忙"),
    FACE_SAMPLE_NOT_UPLOADED("人脸识别样本未上传"),
    FACE_SAMPLE_VALIDATING("人脸识别样本验证未完成"),
    FACE_SAMPLE_INVALID("人脸识别样本无效"),
    CLOUD_GENERATION_FAILED("词云生成失败"),

    PROBLEM_SET_PUBLISHED("考试已发布"),
    CLASS_JOINED("已加入指定班级"),
    UNEXPECTED_EXCEPTION("意料之外的异常")
}

class RequestException(val status : ResponseCode, val extra : String? = null) : RuntimeException(statusMsg(status, extra)) {
    override fun toString()
        = simpleErrorResponse(status, extra)
}

fun simpleResponse(code : Int, msg : String, data : JSONObject) : String
        = JSONObject().apply {
    put("code", code)
    put("msg", msg)
    putAll(data)
}.toJSONString().exportInfo()

fun simpleSuccessResponse(data : JSONObject) : String
        = simpleResponse(ResponseCode.SUCCESS.ordinal, ResponseCode.SUCCESS.msg, data)

fun simpleSuccessResponse(vararg data : Pair<Any, Any>) : String
        = simpleSuccessResponse(processPairVararg(data))

fun simpleErrorResponse(status : ResponseCode, msg : String? = null, data : JSONObject) : String
        = simpleResponse(status.ordinal, statusMsg(status, msg), data)

fun simpleErrorResponse(status : ResponseCode, msg : String? = null, vararg data : Pair<Any, Any>) : String
        = simpleErrorResponse(status, msg, processPairVararg(data))

fun cascadeResponse(code : Int, msg : String, data : JSON) : String
        = JSONObject().apply {
    put("code", code)
    put("msg", msg)
    put("data", data)
}.toJSONString().exportInfo()

fun cascadeSuccessResponse(data : JSON) : String
        = cascadeResponse(ResponseCode.SUCCESS.ordinal, ResponseCode.SUCCESS.msg, data)

fun cascadeErrorResponse(status : ResponseCode, msg : String? = null, data : JSON) : String
        = cascadeResponse(status.ordinal, statusMsg(status, msg), data)

fun cascadeSuccessResponse(vararg data : Pair<Any, Any>) : String
        = cascadeSuccessResponse(processPairVararg(data))

fun cascadeSuccessResponse(data : List<Any>) : String
        = cascadeResponse(ResponseCode.SUCCESS.ordinal, ResponseCode.SUCCESS.msg, data.toFormattedJSONArray())

fun cascadeErrorResponse(status : ResponseCode, msg : String? = null, vararg data : Pair<Any, Any>) : String
        = cascadeErrorResponse(status, msg, processPairVararg(data))

fun mixedResponse(code : Int, msg : String, surface : JSONObject, cascade : JSON) : String
        = JSONObject().apply {
    put("code", code)
    put("msg", msg)
    putAll(surface)
    put("data", cascade)
}.toJSONString().exportInfo()

fun mixedSuccessResponse(cascade : JSON, surface : JSONObject) : String
        = mixedResponse(ResponseCode.SUCCESS.ordinal, ResponseCode.SUCCESS.msg, surface, cascade)

fun mixedSuccessResponse(cascade : List<Any>, vararg surface : Pair<Any, Any>) : String
        = mixedSuccessResponse(cascade.toFormattedJSONArray(), processPairVararg(surface))

fun mixedErrorResponse(status : ResponseCode, msg : String? = null, surface : JSONObject, cascade : JSON) : String
        = mixedResponse(status.ordinal, statusMsg(status, msg), surface, cascade)

fun mixedErrorResponse(status : ResponseCode, msg : String? = null, cascade : List<Any>, vararg surface : Pair<Any, Any>) : String
        = mixedErrorResponse(status, msg, processPairVararg(surface), cascade.toFormattedJSONArray())

fun <E> monitorBatchOp(loopRange : IntRange,
                       transform : (Int) -> E,
                       block : (E) -> Pair<ResponseCode, String?>) : Pair<Boolean, JSONArray> {
    var success = true
    val msgList = mutableListOf<String>()
    for (i in loopRange) {
        val result = block(transform(i))
        if(result.first == ResponseCode.SUCCESS) {
            msgList.add(ResponseCode.BATCH_OP_OK.msg)
        } else {
            success = false
            msgList.add(statusMsg(result.first, result.second))
        }
    }
    return success to JSONArray(msgList as List<String>)
}

private val logger = LoggerFactory.getLogger("Response")

private fun processPairVararg(data : Array<out Pair<Any, Any>>)
        = JSONObject(data.associate {
    when (it.first) {
        is String -> (it.first as String) to it.second
        is KProperty<*> -> (it.first as KProperty<*>).name to (it.first as KProperty<*>).getter.call(it.second)?.toString()
        else -> throw IllegalArgumentException("Unsupported key type: ${it.first::class.qualifiedName}")
    }
})

private fun statusMsg(status : ResponseCode, msg : String?) : String
        = if (msg != null) "${status.msg} : $msg" else status.msg

private fun String.exportInfo()
        = this.also { logger.info("Response : $it") }

private fun List<Any>.toFormattedJSONArray()
        = JSON.parseArray(JSON.toJSONString(this, SerializerFeature.WriteEnumUsingToString, SerializerFeature.WriteDateUseDateFormat))

fun Any.toFormattedJSONObject()
        = JSON.parseObject(JSON.toJSONString(this, SerializerFeature.WriteEnumUsingToString, SerializerFeature.WriteDateUseDateFormat))