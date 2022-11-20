package org.zrnq.exam.controller

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.zrnq.exam.config.JsonParam
import org.zrnq.exam.config.JwtRole
import org.zrnq.exam.config.JwtUserId
import org.zrnq.exam.entity.*
import org.zrnq.exam.exception.*
import org.zrnq.exam.mapper.*
import org.zrnq.exam.util.autoPagingProcessor
import org.zrnq.exam.util.dateFormat
import org.zrnq.exam.util.removeDuplicate
import org.zrnq.exam.util.validate
import java.util.Date

@RestController
class ProblemSetController {
    @Autowired
    lateinit var problemSetMapper : ProblemSetMapper
    @Autowired
    lateinit var problemMapper : ProblemMapper
    @Autowired
    lateinit var problemSetProblemRelMapper : ProblemSetProblemRelMapper
    @Autowired
    lateinit var problemSetClassRelMapper : ProblemSetClassRelMapper
    @Autowired
    lateinit var studentClassRelMapper : StudentClassRelMapper
    @Autowired
    lateinit var courseMapper : CourseMapper
    @PostMapping("/exam")
    @JwtRole(User.Role.TEACHER)
    fun createProblemSet(@JsonParam problemSetName : String,
                         @JwtUserId userId : Long) : String {
        val problemSet = ProblemSet(0, userId, problemSetName)
        problemSetMapper.insert(problemSet)
        return simpleSuccessResponse("problemSetId" to problemSet.problemSetId)
    }

    @PutMapping("/exam")
    @JwtRole(User.Role.TEACHER)
    fun updateProblemSet(@JsonParam problemSetId : Long,
                         @JsonParam problemList : String,
                         @JwtUserId userId : Long) : String {
        val problemSet = problemSetMapper.selectById(problemSetId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemSetId[$problemSetId]")
        if(problemSet.owner != userId)
            throw RequestException(ResponseCode.FORBIDDEN, "userId[$userId], problemSetId[$problemSetId]")
        if(problemSetClassRelMapper.selectCount(QueryWrapper<ProblemSetClassRel>()
                .eq("problem_set_id", problemSetId)) > 0)
            throw RequestException(ResponseCode.PROBLEM_SET_PUBLISHED, "problemSetId[$problemSetId]")
        val json = JSON.parseArray(problemList)
        val problemIdList = json.indices.map { json.getLongValue(it) }
        if(problemMapper.selectCount(QueryWrapper<Problem>().`in`("problem_id", problemIdList)) < problemIdList.size)
            throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemId[${problemIdList.joinToString(",")}]")
        problemSetProblemRelMapper.delete(QueryWrapper<ProblemSetProblemRel>().eq("problem_set_id", problemSetId))
        problemIdList
            .forEachIndexed { index, it -> problemSetProblemRelMapper.insert(
                ProblemSetProblemRel(problemSetId, it, index)
            ) }
        return simpleSuccessResponse()
    }

    @GetMapping("/exam")
    @JwtRole(User.Role.TEACHER)
    fun getProblemSets(@RequestParam page : Int?,
                       @RequestParam limit : Int?,
                       @JwtUserId userId : Long) : String {
        return autoPagingProcessor(page, limit, { p, l ->
            problemSetMapper.selectPage(Page(p, l), QueryWrapper<ProblemSet>().eq("owner", userId))
        }, {
            problemSetMapper.selectList(QueryWrapper<ProblemSet>().eq("owner", userId))
        })
    }

    @PostMapping("/exam/publish")
    @JwtRole(User.Role.TEACHER)
    fun publishProblemSet(@JsonParam problemSetId : Long,
                          @JsonParam classId : Long,
                          @JsonParam startTime : Date,
                          @JsonParam endTime : Date,
                          @JwtUserId userId : Long) : String {
        val problemSet = problemSetMapper.selectById(problemSetId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemSetId[$problemSetId]")
        if(problemSet.owner != userId)
            throw RequestException(ResponseCode.FORBIDDEN)
        if(endTime.before(startTime))
            throw RequestException(ResponseCode.ILLEGAL_PARAMETER,
                "startTime[${dateFormat.format(startTime)}], endTime[${dateFormat.format(endTime)}]")
        val problemSetPublish = problemSetClassRelMapper.selectOne(QueryWrapper<ProblemSetClassRel>().apply {
            eq("problem_set_id", problemSetId)
            eq("class_id", classId)
        })
        if(problemSetPublish != null)
            throw RequestException(ResponseCode.PROBLEM_SET_PUBLISHED, "problemSetId[$problemSetId], classId[$classId]")
        problemSetClassRelMapper.insert(ProblemSetClassRel(problemSetId, classId, startTime, endTime, 0.0))
        return simpleSuccessResponse()
    }

    @PostMapping("/exam/info")
    @JwtRole(User.Role.TEACHER, User.Role.STUDENT)
    fun getProblemSetInfo(@JsonParam(required = false) problemSetId : Long?,
                          @JsonParam(required = false) classId : Long?,
                          user : User) : String {
        val problemSet = if(user.role == User.Role.STUDENT) {
            problemSetClassRelMapper.selectFullProblemSetInfoForStudent(user.userId, QueryWrapper<FullProblemSetInfo>().apply {
                eq(problemSetId != null, "problem_set_id", problemSetId)
                eq(classId != null,"class_id", classId)
            })
        } else {
            problemSetClassRelMapper.selectFullProblemSetInfoForTeacher(user.userId, QueryWrapper<FullProblemSetInfo>().apply {
                eq(problemSetId != null, "problem_set_id", problemSetId)
                eq(classId != null,"class_id", classId)
            })
        }
        return cascadeSuccessResponse(problemSet)
    }

    @PostMapping("/exam/content")
    @JwtRole(User.Role.TEACHER, User.Role.STUDENT)
    fun getProblemSetContent(@JsonParam problemSetId : Long,
                             user : User) : String {
        val problemSet = problemSetMapper.selectById(problemSetId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemSetId[$problemSetId]")
        if(user.role == User.Role.STUDENT) {
            if(problemSetClassRelMapper.selectFeasibleProblemSetForStudent(problemSetId, user.userId) <= 0)
                throw RequestException(ResponseCode.FORBIDDEN, "problemSetId[$problemSetId]")
        } else if(user.role == User.Role.TEACHER) {
            if(problemSet.owner != user.userId)
                throw RequestException(ResponseCode.FORBIDDEN, "problemSetId[$problemSetId]")
        }
        val problemSetProblems = problemSetProblemRelMapper.selectProblemList(problemSetId)
        return cascadeSuccessResponse(problemSetProblems)
    }
}