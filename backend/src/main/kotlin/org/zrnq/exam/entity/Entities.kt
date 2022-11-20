package org.zrnq.exam.entity

import com.alibaba.fastjson.annotation.JSONField
import com.baomidou.mybatisplus.annotation.EnumValue
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.util.*

@TableName("xuser")
class User(
    @TableId(type = IdType.AUTO) var userId : Long,
    var username : String,
    @JSONField(serialize = false) var password : String,
    var studentNum : String?,
    var instituteId : Long,
    var role : Role
) {
    enum class Role(@EnumValue val code : Int, val desc : String) {
        ADMIN(2, "管理员"),
        TEACHER(1, "教师"),
        STUDENT(0, "学生");
        companion object {
            fun getByDesc(desc : String) = values().firstOrNull { it.desc == desc }
        }
        override fun toString() = desc
    }
}

class Institute(
    @TableId(type = IdType.AUTO) var instituteId : Long,
    var instituteName : String
)

@TableName("xclass")
class Course(
    @TableId(type = IdType.AUTO) var classId : Long,
    var owner : Long,
    var className : String
)

class StudentClassRel(
    var studentId : Long,
    var classId : Long
)

class Problem (
    @TableId(type = IdType.AUTO) var problemId : Long,
    var userId : Long,
    var content : ByteArray,
    var type : Type,
    var total : Int?
) {
    enum class Type(@EnumValue val code : Int, val desc : String) {
        SINGLE_CHOICE(0, "单选题"),
        BLANK_FILL(1, "填空题");
        companion object {
            fun getByDesc(desc : String) = values().firstOrNull { it.desc == desc }
        }
        override fun toString() = desc
    }
}

class ProblemSet(
    @TableId(type = IdType.AUTO) var problemSetId : Long,
    var owner : Long,
    var problemSetName : String
)

class ProblemSetProblemRel(
    var problemSetId : Long,
    var problemId : Long,
    @TableField("`order`") var order : Int
)


class ProblemSetClassRel(
    var problemSetId : Long,
    var classId : Long,
    var startTime : Date,
    var endTime : Date,
    var problemSetWeight : Double
)

class AnswerSet(
    @TableId(type = IdType.AUTO) var answerSetId : Long,
    var studentId : Long,
    var classId : Long,
    var problemSetId : Long,
    var faceCheck : Int,
    var successCheck : Int
)

abstract class GeneralAnswer(
    @TableId(type = IdType.AUTO) var answerSetId : Long,
    @TableField("`order`") var order : Int
)

class SimpleAnswer(
    answerSetId : Long,
    var answer : Int,
    order : Int
) : GeneralAnswer(answerSetId, order)

class GraphicAnswer(
    answerSetId : Long,
    var content : String,
    var score : Int,
    order : Int
) : GeneralAnswer(answerSetId, order)

class OtherScore(
    var classId : Long,
    var studentId : Long,
    var otherScore : Int
)