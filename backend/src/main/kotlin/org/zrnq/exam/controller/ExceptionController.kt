package org.zrnq.exam.controller

import org.apache.catalina.connector.ClientAbortException
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import org.zrnq.exam.exception.simpleErrorResponse
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ExceptionController {
    private val logger = org.slf4j.LoggerFactory.getLogger(ExceptionController::class.java)
    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handelUnexpectedException(ex: Exception) : String? {
        return when (ex) {
            is RequestException -> simpleErrorResponse(ex.status, ex.extra)
            is HttpRequestMethodNotSupportedException -> simpleErrorResponse(ResponseCode.REQUEST_METHOD_NOT_SUPPORTED)
            is MissingServletRequestParameterException -> simpleErrorResponse(ResponseCode.PARAMETER_MISSING, ex.parameterName)
            else -> {
                logger.error("Unexpected exception", ex)
                simpleErrorResponse(ResponseCode.UNEXPECTED_EXCEPTION, ex.message)
            }
        }
    }

    @ExceptionHandler(ClientAbortException::class)
    fun handleClientAbortException(ex : ClientAbortException) {
        logger.warn("Client aborted connection")
    }
}