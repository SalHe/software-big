package org.zrnq.exam.controller

import com.alibaba.fastjson.JSONArray
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.zrnq.exam.config.JsonParam
import org.zrnq.exam.config.JwtRole
import org.zrnq.exam.config.JwtUserId
import org.zrnq.exam.entity.*
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import org.zrnq.exam.exception.cascadeSuccessResponse
import org.zrnq.exam.exception.simpleSuccessResponse
import org.zrnq.exam.mapper.*
import org.zrnq.exam.util.EmotionAnalysis
import org.zrnq.exam.util.WordCloud
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
class AnswerController {
    @Autowired
    lateinit var answerSetMapper : AnswerSetMapper
    @Autowired
    lateinit var simpleAnswerMapper : SimpleAnswerMapper
    @Autowired
    lateinit var graphicAnswerMapper : GraphicAnswerMapper
    @Autowired
    lateinit var problemSetClassRelMapper : ProblemSetClassRelMapper
    @Autowired
    lateinit var problemSetProblemRelMapper : ProblemSetProblemRelMapper
    @Autowired
    lateinit var studentClassRelMapper : StudentClassRelMapper
    @Autowired
    lateinit var courseMapper : CourseMapper
    @Autowired
    lateinit var problemMapper : ProblemMapper
    @Autowired
    lateinit var wordCloud : WordCloud
    @Autowired
    lateinit var emotionAnalysis : EmotionAnalysis
    @PostMapping("/answer")
    @JwtRole(User.Role.STUDENT)
    fun getAnswerSet(@JsonParam problemSetId : Long,
                     @JsonParam classId : Long,
                     @JwtUserId studentId : Long) : String {
        validate(studentId, classId, problemSetId)
        var answerSet = answerSetMapper.selectOne(QueryWrapper<AnswerSet>().apply {
            eq("problem_set_id", problemSetId)
            eq("class_id", classId)
            eq("student_id", studentId)
        })
        if(answerSet == null) {
            answerSet = AnswerSet(0, studentId, classId, problemSetId, 0, 0)
            answerSetMapper.insert(answerSet)
        }
        return simpleSuccessResponse("answerSetId" to answerSet.answerSetId)
    }

    @PostMapping("/answer/simple")
    @JwtRole(User.Role.STUDENT)
    fun appendSimpleAnswer(@JsonParam answerSetId : Long,
                           @JsonParam answer : Int,
                           @JsonParam order : Int,
                           @JwtUserId studentId : Long) : String {
        validate(studentId, answerSetId, order, Problem.Type.SINGLE_CHOICE)
        var previous = simpleAnswerMapper.selectOne(QueryWrapper<SimpleAnswer>().apply {
            eq("answer_set_id", answerSetId)
            eq("`order`", order)
        })
        if(previous == null) {
            previous = SimpleAnswer(answerSetId, answer, order)
            simpleAnswerMapper.insert(previous)
        } else {
            previous.answer = answer
            simpleAnswerMapper.updateById(previous)
        }
        return simpleSuccessResponse()
    }

    @PostMapping("/answer/graphic")
    @JwtRole(User.Role.STUDENT)
    fun appendGraphicAnswer(@JsonParam answerSetId : Long,
                            @JsonParam order : Int,
                            @JsonParam answer : String,
                            @JwtUserId studentId : Long) : String {
        validate(studentId, answerSetId, order, Problem.Type.BLANK_FILL)
        var previous = graphicAnswerMapper.selectOne(QueryWrapper<GraphicAnswer>().apply {
            eq("answer_set_id", answerSetId)
            eq("`order`", order)
        })
        if(previous == null) {
            previous = GraphicAnswer(answerSetId, answer, -1, order)
            graphicAnswerMapper.insert(previous)
        } else {
            previous.content = answer
            graphicAnswerMapper.updateById(previous)
        }
        return simpleSuccessResponse()
    }

    @GetMapping("/answer/simple")
    @JwtRole(User.Role.STUDENT, User.Role.TEACHER)
    fun getSimpleAnswer(@RequestParam answerSetId : Long,
                        @RequestParam order : Int,
                        @JwtUserId studentId : Long,
                        user : User) : String {
        validate(answerSetId, user)
        val answer = simpleAnswerMapper.selectOne(QueryWrapper<SimpleAnswer>().apply {
            eq("answer_set_id", answerSetId)
            eq("`order`", order)
        }) ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "answerSetId[$answerSetId], order[$order]")
        return simpleSuccessResponse("answer" to answer.answer)
    }

    @GetMapping("/answer/graphic")
    @JwtRole(User.Role.STUDENT, User.Role.TEACHER)
    fun getAnswerGraphic(@RequestParam answerSetId : Long,
                         @RequestParam order : Int,
                         user : User,
                         response : HttpServletResponse) : String {
        validate(answerSetId, user)
        val answer = graphicAnswerMapper.selectOne(QueryWrapper<GraphicAnswer>().apply {
            eq("answer_set_id", answerSetId)
            eq("`order`", order)
        }) ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "answerSetId[$answerSetId], order[$order]")
        response.contentType = "image/png"
        return simpleSuccessResponse("answer" to answer.content)
    }

    @JwtRole(User.Role.TEACHER)
    @GetMapping("/answer/cloud")
    fun getAnswerCloud(@RequestParam problemSetId : Long,
                       @RequestParam classId : Long,
                       @RequestParam order : Int) : ByteArray {
        validateT(problemSetId, classId, order, Problem.Type.BLANK_FILL)
        return wordCloud.generateCloud(JSONArray().apply {
            answerSetMapper.selectGraphicAnswers(problemSetId, classId, order)
                .forEach { graphicAnswer -> add(graphicAnswer) }
        })
    }

    @JwtRole(User.Role.TEACHER)
    @PostMapping("/answer/emotion")
    fun getAnswerEmotion(@JsonParam problemSetId : Long,
                         @JsonParam classId : Long,
                         @JsonParam order : Int) : String {
        validateT(problemSetId, classId, order, Problem.Type.BLANK_FILL)
        val result = emotionAnalysis.analyseEmotion(JSONArray().apply {
            answerSetMapper.selectGraphicAnswers(problemSetId, classId, order)
                .forEach { graphicAnswer -> add(graphicAnswer) }
        })
        return simpleSuccessResponse("result" to result)
    }

    @JwtRole(User.Role.TEACHER)
    @PostMapping("/answer/choice")
    fun getChoiceFrequency(@JsonParam problemSetId : Long,
                           @JsonParam classId : Long,
                           @JsonParam order : Int) : String {
        validateT(problemSetId, classId, order, Problem.Type.SINGLE_CHOICE)
        val rel = problemSetProblemRelMapper.selectOne(QueryWrapper<ProblemSetProblemRel>().apply {
            eq("problem_set_id", problemSetId)
            eq("`order`", order)
        })!!
        val problem = problemMapper.selectById(rel.problemId)!!
        val freq = answerSetMapper.selectChoiceFrequency(problemSetId, classId, order)
        val result = JSONArray().apply {
            for(i in 0 until problem.total!!) {
                (freq.find { it.answer == i }?.frequency ?: 0).also { add(it) }
            }
        }
        return simpleSuccessResponse("result" to result)
    }

    @PostMapping("/answer/list")
    @JwtRole(User.Role.TEACHER)
    fun getClassAnswerSets(@JsonParam problemSetId : Long,
                           @JsonParam classId : Long,
                           @JwtUserId userId : Long) : String {
        val course = courseMapper.selectById(classId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "courseId[$classId]")
        if(course.owner != userId)
            throw RequestException(ResponseCode.FORBIDDEN, "userId[${userId}], courseId[$classId]")
        problemSetClassRelMapper.selectOne(QueryWrapper<ProblemSetClassRel>().apply {
            eq("problem_set_id", problemSetId)
            eq("class_id", course.classId)
        }) ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemSetId[$problemSetId], classId[${course.classId}]")
        val answerSets = answerSetMapper.selectFullAnswerSetInfo(course.classId, problemSetId)
        return cascadeSuccessResponse(answerSets)
    }

    private fun validate(answerSetId : Long, user : User) {
        val answerSet = answerSetMapper.selectById(answerSetId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "answerSetId[$answerSetId]")
        if(user.role == User.Role.STUDENT) {
            if(answerSet.studentId != user.userId)
                throw RequestException(ResponseCode.FORBIDDEN, "answerSetId[$answerSetId]")
        } else if(user.role == User.Role.TEACHER) {
            val course = courseMapper.selectById(answerSet.classId)
                ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "answerSet.classId[${answerSet.classId}]")
            if(course.owner != user.userId)
                throw RequestException(ResponseCode.FORBIDDEN, "answerSetId[$answerSetId]")
        }
    }

    private fun validate(studentId : Long, classId : Long, problemSetId : Long) {
        val now = Date()
        studentClassRelMapper.selectOne(QueryWrapper<StudentClassRel>().apply {
            eq("student_id", studentId)
            eq("class_id", classId)
        }) ?: throw RequestException(ResponseCode.FORBIDDEN, "studentId[$studentId], classId[$classId]")
        problemSetClassRelMapper.selectOne(QueryWrapper<ProblemSetClassRel>().apply {
            eq("problem_set_id", problemSetId)
            eq("class_id", classId)
            le("start_time", now)
            ge("end_time", now)
        }) ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "studentId[$studentId], problemSetId[$problemSetId]")
    }

    private fun validate(studentId : Long, answerSetId : Long, order : Int, type : Problem.Type) {
        val answerSet = answerSetMapper.selectById(answerSetId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "answerSetId[$answerSetId]")
        if(answerSet.studentId != studentId)
            throw RequestException(ResponseCode.FORBIDDEN, "answerSetId[$answerSetId]")
        validate(studentId, answerSet.classId, answerSet.problemSetId)
        val problemSetProblemRel = problemSetProblemRelMapper.selectOne(QueryWrapper<ProblemSetProblemRel>().apply {
            eq("problem_set_id", answerSet.problemSetId)
            eq("`order`", order)
        }) ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "order[$order]")
        val problem = problemMapper.selectById(problemSetProblemRel.problemId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemId[${problemSetProblemRel.problemId}]")
        if(problem.type != type)
            throw RequestException(ResponseCode.ILLEGAL_PARAMETER, "problemId[${problemSetProblemRel.problemId}]")
    }

    private fun validateT(problemSetId : Long, classId : Long, order : Int, type : Problem.Type) {
        problemSetClassRelMapper.selectOne(QueryWrapper<ProblemSetClassRel>().apply {
            eq("problem_set_id", problemSetId)
            eq("class_id", classId)
        }) ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemSetId[$problemSetId], classId[$classId]")
        val problemSetProblemRel = problemSetProblemRelMapper.selectOne(QueryWrapper<ProblemSetProblemRel>().apply {
            eq("problem_set_id", problemSetId)
            eq("`order`", order)
        }) ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "order[$order]")
        val problem = problemMapper.selectById(problemSetProblemRel.problemId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "problemId[${problemSetProblemRel.problemId}]")
        if(problem.type != type)
            throw RequestException(ResponseCode.ILLEGAL_PARAMETER, "problemId[${problemSetProblemRel.problemId}]")
    }
}