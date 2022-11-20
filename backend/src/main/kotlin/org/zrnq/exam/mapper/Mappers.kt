package org.zrnq.exam.mapper

import com.baomidou.mybatisplus.core.conditions.Wrapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.toolkit.Constants
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component
import org.zrnq.exam.entity.*
import org.zrnq.exam.util.mybatisPlusInjection

@Component
interface UserMapper : BaseMapper<User> {
    @Select("""
        SELECT user_id, username, institute_id, institute_name, role
        FROM xuser NATURAL JOIN institute
        WHERE user_id = #{userId}
    """)
    fun selectUserInfo(@Param("userId") userId : Long) : FullUserInfo
}

@Component
interface InstituteMapper : BaseMapper<Institute>

@Component
interface CourseMapper : BaseMapper<Course> {
    @Select("""
        SELECT class_id, owner, class_name, username FROM
        xclass JOIN xuser ON owner = user_id
        $mybatisPlusInjection
    """)
    fun selectCourseInfo(page : Page<FullCourseInfo>, @Param(Constants.WRAPPER) wrapper : QueryWrapper<FullCourseInfo>): IPage<FullCourseInfo>
}

@Component
interface StudentClassRelMapper : BaseMapper<StudentClassRel> {
    @Select("""
        SELECT class_id, owner, class_name, username FROM
        student_class_rel NATURAL JOIN xclass
        JOIN xuser ON owner = user_id
        $mybatisPlusInjection
    """)
    fun selectCourseInfo(page : Page<FullCourseInfo>, @Param(Constants.WRAPPER) wrapper : Wrapper<FullCourseInfo>) : IPage<FullCourseInfo>
    @Select("""
        SELECT class_id, owner, class_name, username FROM
        student_class_rel NATURAL JOIN xclass
        JOIN xuser ON owner = user_id
        $mybatisPlusInjection
    """)
    fun selectAllCourseInfo(@Param(Constants.WRAPPER) wrapper : Wrapper<FullCourseInfo>) : List<FullCourseInfo>
}

@Component
interface ProblemMapper : BaseMapper<Problem> {
    @Select("""
        SELECT problem_id, type, `total`, username FROM
        problem NATURAL JOIN xuser
        $mybatisPlusInjection
    """)
    fun selectProblemInfo(page : Page<FullProblemInfo>, @Param(Constants.WRAPPER) wrapper : QueryWrapper<FullProblemInfo>) : IPage<FullProblemInfo>
}

@Component
interface ProblemSetMapper : BaseMapper<ProblemSet>

@Component
interface ProblemSetProblemRelMapper : BaseMapper<ProblemSetProblemRel> {
    @Select("""
        SELECT problem_id, type, `total` FROM
        problem_set_problem_rel NATURAL JOIN problem
        WHERE problem_set_id = #{problemSetId}
    """)
    fun selectProblemList(@Param("problemSetId") problemSetId : Long) : List<CompactProblemInfo>
}

@Component
interface ProblemSetClassRelMapper : BaseMapper<ProblemSetClassRel> {
    @Select("""
        SELECT COUNT(*) FROM
        (SELECT class_id FROM problem_set_class_rel WHERE problem_set_id = #{problemSetId} AND NOW() BETWEEN start_time AND end_time) AS C
            NATURAL JOIN xclass
            NATURAL JOIN student_class_rel
        WHERE student_id = #{studentId}
    """)
    fun selectFeasibleProblemSetForStudent(@Param("problemSetId") problemSetId : Long, @Param("studentId") studentId : Long) : Int
    @Select("""
        SELECT problem_set_id, class_id, start_time, end_time, class_name, problem_set_name FROM
            (SELECT class_id, class_name
                FROM xclass NATURAL JOIN student_class_rel
                WHERE student_id = #{studentId}) AS C
            NATURAL JOIN problem_set_class_rel
            NATURAL JOIN problem_set
        $mybatisPlusInjection
    """)
    fun selectFullProblemSetInfoForStudent(@Param("studentId") studentId : Long, @Param(Constants.WRAPPER) wrapper : Wrapper<FullProblemSetInfo>) : List<FullProblemSetInfo>
    @Select("""
        SELECT problem_set_id, class_id, start_time, end_time, class_name, problem_set_name FROM
            (SELECT class_id, class_name
                FROM xclass
                WHERE owner = #{teacherId}) AS C
            NATURAL JOIN problem_set_class_rel
            NATURAL JOIN problem_set
        $mybatisPlusInjection
    """)
    fun selectFullProblemSetInfoForTeacher(@Param("teacherId") teacherId : Long, @Param(Constants.WRAPPER) wrapper : Wrapper<FullProblemSetInfo>) : List<FullProblemSetInfo>
}

@Component
interface AnswerSetMapper : BaseMapper<AnswerSet> {
    @Select("""
        SELECT answer_set_id, student_id, username FROM
            answer_set JOIN xuser ON student_id = user_id
            WHERE problem_set_id = #{problemSetId}
            AND class_id = #{classId}
    """)
    fun selectFullAnswerSetInfo(@Param("classId") classId : Long, @Param("problemSetId") problemSetId : Long) : List<FullAnswerSetInfo>
    @Select("""
        SELECT content FROM
            (SELECT answer_set_id FROM answer_set WHERE problem_set_id = #{problemSetId} AND class_id = #{classId}) AS A
            JOIN 
            (SELECT answer_set_id, content FROM graphic_answer WHERE `order` = #{order}) AS GA
            ON A.answer_set_id = GA.answer_set_id
            
    """)
    fun selectGraphicAnswers(@Param("problemSetId") problemSetId : Long, @Param("classId") classId : Long, @Param("order") order : Int) : List<String>
    @Select("""
        SELECT answer, COUNT(*) AS count FROM
            (SELECT answer_set_id FROM answer_set WHERE problem_set_id = #{problemSetId} AND class_id = #{classId}) AS A
            JOIN 
            (SELECT answer_set_id, answer FROM simple_answer WHERE `order` = #{order}) AS GA
            ON A.answer_set_id = GA.answer_set_id
            GROUP BY answer
    """)
    fun selectChoiceFrequency(@Param("problemSetId") problemSetId : Long, @Param("classId") classId : Long, @Param("order") order : Int) : List<AnswerFrequency>
}

@Component
interface SimpleAnswerMapper : BaseMapper<SimpleAnswer>

@Component
interface GraphicAnswerMapper : BaseMapper<GraphicAnswer>

@Component
interface OtherScoreMapper : BaseMapper<OtherScore>