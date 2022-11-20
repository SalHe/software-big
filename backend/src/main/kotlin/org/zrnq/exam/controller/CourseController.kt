package org.zrnq.exam.controller

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.zrnq.exam.config.JsonParam
import org.zrnq.exam.config.JwtRole
import org.zrnq.exam.config.JwtUserId
import org.zrnq.exam.entity.Course
import org.zrnq.exam.entity.FullCourseInfo
import org.zrnq.exam.entity.StudentClassRel
import org.zrnq.exam.entity.User
import org.zrnq.exam.exception.*
import org.zrnq.exam.mapper.CourseMapper
import org.zrnq.exam.mapper.StudentClassRelMapper
import org.zrnq.exam.mapper.UserMapper
import org.zrnq.exam.util.autoPagingProcessor
import org.zrnq.exam.util.validate

@RestController
class CourseController {
    @Autowired
    lateinit var userMapper : UserMapper
    @Autowired
    lateinit var courseMapper : CourseMapper
    @Autowired
    lateinit var studentClassRelMapper : StudentClassRelMapper

    @PostMapping("/course/create")
    @JwtRole(User.Role.TEACHER)
    fun createCourse(@JsonParam className : String,
                     @JwtUserId userId : Long) : String {
        val course = Course(0, userId, className)
        courseMapper.insert(course)
        return simpleSuccessResponse("classId" to course.classId)
    }

    @GetMapping("/course/create")
    @JwtRole(User.Role.TEACHER)
    fun getCreatedCourse(@RequestParam page : Int?,
                         @RequestParam limit : Int?,
                         @JwtUserId userId : Long) : String {
        return autoPagingProcessor(page, limit, { p, l ->
            courseMapper.selectPage(Page(p, l), QueryWrapper<Course>().eq("owner", userId))
        }, {
            courseMapper.selectList(QueryWrapper<Course>().eq("owner", userId))
        })
    }

    @GetMapping("/course/join")
    @JwtRole(User.Role.STUDENT)
    fun getJoinedCourse(@RequestParam page : Int?,
                        @RequestParam limit : Int?,
                        @JwtUserId userId : Long) : String {
        return autoPagingProcessor(page, limit, { p, l ->
            studentClassRelMapper.selectCourseInfo(Page(p, l), QueryWrapper<FullCourseInfo>().eq("student_id", userId))
        }, {
            studentClassRelMapper.selectAllCourseInfo(QueryWrapper<FullCourseInfo>().eq("student_id", userId))
        })
    }

    @PostMapping("/course/join")
    @JwtRole(User.Role.TEACHER)
    fun joinCourse(@JsonParam classId : Long,
                   @JsonParam studentId : Long,
                   user : User) : String {
        val student = userMapper.selectById(studentId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "studentId[$studentId]")
        if(student.role != User.Role.STUDENT)
            throw RequestException(ResponseCode.ILLEGAL_PARAMETER, "studentId[$studentId]")
        if(student.instituteId != user.instituteId)
            throw RequestException(ResponseCode.FORBIDDEN, "studentId[$studentId]")
        courseMapper.selectById(classId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "classId[$classId]")
        if(studentClassRelMapper.selectOne(QueryWrapper<StudentClassRel>()
                .eq("class_id", classId)
                .eq("student_id", studentId)) != null)
            throw RequestException(ResponseCode.CLASS_JOINED, "classId[$classId]")
        studentClassRelMapper.insert(StudentClassRel(studentId, classId))
        return simpleSuccessResponse()
    }

    @GetMapping("/course/{classId}")
    @JwtRole(User.Role.TEACHER)
    fun listStudent(@PathVariable("classId") classId : Long, @JwtUserId userId : Long) : String {
        val course = courseMapper.selectById(classId)
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "classId[$classId]")
        if(course.owner != userId)
            throw RequestException(ResponseCode.FORBIDDEN, "userId[$userId], classId[$classId]")
        val studentList = studentClassRelMapper.selectList(QueryWrapper<StudentClassRel>().eq("class_id", classId))
        return cascadeSuccessResponse(studentList.map { it.studentId })
    }
}