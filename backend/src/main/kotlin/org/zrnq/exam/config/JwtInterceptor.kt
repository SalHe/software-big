package org.zrnq.exam.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import org.zrnq.exam.entity.User
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import org.zrnq.exam.util.JwtTokenUtil
import org.zrnq.exam.util.logger
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class JwtRole(vararg val role : User.Role)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class JwtUserId

private const val authorizationHeader = "Authorization"
private const val jwtPrefix = "Bearer "

@Component
class JwtInterceptor : HandlerInterceptorAdapter() {
    private val logger = logger()

    @Autowired
    lateinit var jwtUtil : JwtTokenUtil

    private val interceptMethods = listOf("GET", "POST", "PUT", "DELETE")

    override fun preHandle(request : HttpServletRequest,
                           response : HttpServletResponse,
                           handler : Any) : Boolean {
        logger.info("[${request.method}] ${request.requestURI}")
        if (request.method !in interceptMethods)
            return true
        var jwtRole : Array<out User.Role> = User.Role.values()
        if (handler is HandlerMethod)
            jwtRole = (handler.getMethodAnnotation(JwtRole::class.java) ?: return true).role
        else return true
        val authHeader = request.getHeader(authorizationHeader)
        if (authHeader.isNullOrEmpty() || !authHeader.startsWith(jwtPrefix))
            throw RequestException(ResponseCode.FORBIDDEN)
        val token = authHeader.substring(7)
        val user = jwtUtil.getUser(token)
        if (user.role !in jwtRole && user.role != User.Role.ADMIN)
            throw RequestException(ResponseCode.FORBIDDEN)
        return true
    }
}

@Component
class JwtUserParameterResolver : HandlerMethodArgumentResolver {
    @Autowired
    lateinit var jwtUtil : JwtTokenUtil
    override fun supportsParameter(parameter : MethodParameter) : Boolean {
        return parameter.parameterType == User::class.java
    }

    override fun resolveArgument(
        parameter : MethodParameter,
        modelAndViewContainer : ModelAndViewContainer?,
        webRequest : NativeWebRequest,
        binderFactory : WebDataBinderFactory?
    ) : Any? {
        val authHeader = webRequest.getHeader(authorizationHeader)
        if (authHeader.isNullOrEmpty() || !authHeader.startsWith(jwtPrefix))
            throw RequestException(ResponseCode.FORBIDDEN)
        val token = authHeader.substring(7)
        return jwtUtil.getUser(token)
    }
}

@Component
class JwtUserIdParameterResolver : HandlerMethodArgumentResolver {
    @Autowired
    lateinit var jwtUtil : JwtTokenUtil
    override fun supportsParameter(parameter : MethodParameter) : Boolean {
        return parameter.hasParameterAnnotation(JwtUserId::class.java)
                && parameter.parameterType == Long::class.java
    }

    override fun resolveArgument(
        parameter : MethodParameter,
        modelAndViewContainer : ModelAndViewContainer?,
        webRequest : NativeWebRequest,
        binderFactory : WebDataBinderFactory?
    ) : Any? {
        val authHeader = webRequest.getHeader(authorizationHeader)
        if (authHeader.isNullOrEmpty() || !authHeader.startsWith(jwtPrefix))
            throw RequestException(ResponseCode.FORBIDDEN)
        val token = authHeader.substring(7)
        return jwtUtil.parseJWT(token).body.get("id", java.lang.Long::class.java)
    }
}