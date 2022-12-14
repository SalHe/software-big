package org.zrnq.exam.config

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
// import jdk.nashorn.internal.runtime.ParserException
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import org.zrnq.exam.util.dateFormat
import java.util.*
import javax.servlet.http.HttpServletRequest

annotation class JsonParam(val name : String = ValueConstants.DEFAULT_NONE,
                           val default : String = ValueConstants.DEFAULT_NONE,
                           val required : Boolean = true)

@Component
class FastJsonParameterResolver : HandlerMethodArgumentResolver {
    private val logger = org.slf4j.LoggerFactory.getLogger(FastJsonParameterResolver::class.java)
    companion object {
        const val JSON_BUFFER_ATTRIBUTE = "_json_buffer"
    }
    override fun supportsParameter(parameter : MethodParameter) : Boolean {
        return parameter.hasParameterAnnotation(JsonParam::class.java)
    }

    override fun resolveArgument(
        parameter : MethodParameter,
        mavContainer : ModelAndViewContainer?,
        webRequest : NativeWebRequest,
        binderFactory : WebDataBinderFactory?
    ) : Any? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)!!
        if(request.contentType != "application/json")
            throw RequestException(ResponseCode.CONTENT_TYPE_REJECTED)
        val jsonStr : String
        if(request.getAttribute(JSON_BUFFER_ATTRIBUTE) == null) {
            jsonStr = request.reader.readText()
            request.setAttribute(JSON_BUFFER_ATTRIBUTE, jsonStr)
        } else {
            jsonStr = request.getAttribute(JSON_BUFFER_ATTRIBUTE) as String
        }
        val json = JSON.parseObject(jsonStr) ?: JSONObject()
        val defaultValue = parameter.getParameterAnnotation(JsonParam::class.java)!!.default
        val isRequired = parameter.getParameterAnnotation(JsonParam::class.java)!!.required &&
                defaultValue == ValueConstants.DEFAULT_NONE
        val name = parameter.getParameterAnnotation(JsonParam::class.java)!!.name
            .let { if(it == ValueConstants.DEFAULT_NONE) parameter.parameterName else it }
        if(isRequired && !json.containsKey(name))
            throw RequestException(ResponseCode.PARAMETER_MISSING, name)
        if(!json.containsKey(name)) return if(defaultValue == ValueConstants.DEFAULT_NONE) null else defaultValue
        return try {
            when(parameter.parameterType) {
                String::class.java -> json.getString(name)
                Int::class.java -> json.getInteger(name)
                java.lang.Integer::class.java -> json.getIntValue(name)
                Long::class.java -> json.getLong(name)
                java.lang.Long::class.java -> json.getLongValue(name)
                Double::class.java -> json.getDouble(name)
                Boolean::class.java -> json.getBoolean(name)
                Date::class.java ->
                    try {
                        dateFormat.parse(json.getString(name))
                    // } catch (e : ParserException) {
                    } catch (e : Exception) {
                        throw RequestException(ResponseCode.ILLEGAL_PARAMETER, "$name[${json.getString(name)}]")
                    }
                else -> {
                    logger.warn("Unsupported parameter type: ${parameter.parameterType}")
                    null
                }
            }
        } catch (e : Exception) {
            throw RequestException(ResponseCode.ILLEGAL_PARAMETER, "$name[${json.getString(name)}]:${e.message}")
        }
    }
}