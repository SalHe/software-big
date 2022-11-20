package org.zrnq.exam.entity

import java.util.*

class FullCourseInfo(
    var classId : Long,
    var owner : Long,
    var className : String,
    var teacherName : String
)

class FullProblemInfo(
    var problemId : Long,
    var type : Problem.Type,
    var total : Int?,
    var creator : String
)

class CompactProblemInfo(
    var problemId : Long,
    var type : Problem.Type,
    var total : Int?
)

class FullProblemSetInfo(
    var problemSetId : Long,
    var classId : Long,
    var startTime : Date,
    var endTime : Date,
    var className : String,
    var problemSetName : String
)

class FullUserInfo(
    var userId : Long,
    var username : String,
    var instituteId : Long,
    var instituteName : String,
    var role : User.Role
)

class FullAnswerSetInfo(
    var answerSetId : Long,
    var studentId : Long,
    var studentName : String
)

class AnswerFrequency(
    var answer : Int,
    var frequency : Int
)