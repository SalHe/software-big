package org.zrnq.exam.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.zrnq.exam.config.JsonParam
import org.zrnq.exam.config.JwtRole
import org.zrnq.exam.config.JwtUserId
import org.zrnq.exam.entity.FullProblemInfo
import org.zrnq.exam.entity.Problem
import org.zrnq.exam.entity.User
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import org.zrnq.exam.exception.mixedSuccessResponse
import org.zrnq.exam.exception.simpleSuccessResponse
import org.zrnq.exam.mapper.ProblemMapper
import org.zrnq.exam.util.checkImageSize
import org.zrnq.exam.util.validate
import javax.servlet.http.HttpServletResponse

@RestController
class ProblemController {
    @Autowired
    lateinit var problemMapper : ProblemMapper

    @PostMapping("/problem")
    @JwtRole(User.Role.TEACHER)
    fun createProblem(@RequestParam type : String,
                      @RequestParam(required = false) total : Int?,
                      @RequestParam image : MultipartFile,
                      @JwtUserId userId : Long) : String {
        checkImageSize(image)
        val problemType = Problem.Type.getByDesc(type)
            ?: throw RequestException(ResponseCode.ILLEGAL_PARAMETER, "type[$type]")
        if(problemType == Problem.Type.SINGLE_CHOICE && total == null)
                throw RequestException(ResponseCode.PARAMETER_MISSING, "total")
        val problem = Problem(0, userId, image.bytes, problemType, total)
        problemMapper.insert(problem)
        return simpleSuccessResponse("problemId" to problem.problemId)
    }

    @GetMapping("/problem")
    @JwtRole(User.Role.TEACHER)
    fun getProblem(@RequestParam page : Int,
                   @RequestParam limit : Int,
                   user : User) : String {
        validate(page, limit)
        val problemList = problemMapper.selectProblemInfo(Page(page.toLong(), limit.toLong()),
            QueryWrapper<FullProblemInfo>().apply {
            eq("institute_id", user.instituteId)
        })
        return mixedSuccessResponse(problemList.records, "count" to problemList.total)
    }

    @GetMapping("/problem/{problemId}")
    @JwtRole(User.Role.STUDENT, User.Role.TEACHER)
    fun getProblemImage(@PathVariable problemId : Long,
                        response : HttpServletResponse) : ByteArray {
        val problem = problemMapper.selectById(problemId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemId[$problemId]")
        response.contentType = "image/png"
        return problem.content
    }

    @DeleteMapping("/problem")
    @JwtRole(User.Role.TEACHER)
    fun deleteProblem(@JsonParam problemId : Long,
                      user : User) : String {
        val problem = problemMapper.selectById(problemId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemId[$problemId]")
        if(user.userId != problem.userId && user.role != User.Role.ADMIN)
            throw RequestException(ResponseCode.FORBIDDEN)
        problemMapper.deleteById(problemId)
        return simpleSuccessResponse()
    }
}