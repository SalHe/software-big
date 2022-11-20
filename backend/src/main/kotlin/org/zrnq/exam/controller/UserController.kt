package org.zrnq.exam.controller

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.zrnq.exam.config.JsonParam
import org.zrnq.exam.config.JwtRole
import org.zrnq.exam.config.JwtUserId
import org.zrnq.exam.entity.Institute
import org.zrnq.exam.entity.User
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import org.zrnq.exam.exception.simpleSuccessResponse
import org.zrnq.exam.exception.toFormattedJSONObject
import org.zrnq.exam.mapper.InstituteMapper
import org.zrnq.exam.mapper.UserMapper
import org.zrnq.exam.util.JwtTokenUtil

@RestController
class UserController {
    @Autowired
    lateinit var userMapper : UserMapper
    @Autowired
    lateinit var jwtTokenUtil : JwtTokenUtil
    @Autowired
    lateinit var instituteMapper : InstituteMapper
    @Autowired
    lateinit var passwordEncoder : BCryptPasswordEncoder

    @PostMapping("/user/login")
    fun login(@JsonParam username : String,
              @JsonParam password : String) : String {
        val user = userMapper.selectOne(QueryWrapper<User>().eq("username", username))
            ?: throw RequestException(ResponseCode.TARGET_NOT_FOUND, "username[$username]")
        if (!passwordEncoder.matches(password, user.password))
            throw RequestException(ResponseCode.BAD_CREDENTIALS)
        val jwt = jwtTokenUtil.createJWT(user.userId, user.username, user.role.desc)
        return simpleSuccessResponse("token" to jwt)
    }

    @PostMapping("/user/register")
    fun register(@JsonParam username : String,
                 @JsonParam password : String,
                 @JsonParam instituteName : String) : String {
        val user = userMapper.selectOne(QueryWrapper<User>().eq("username", username))
        if(user != null)
            throw RequestException(ResponseCode.IDENTIFIER_ALREADY_EXISTS, "username[$username]")
        val institute = Institute(0, instituteName)
        instituteMapper.insert(institute)
        val newUser = User(0, username, passwordEncoder.encode(password), null, institute.instituteId, User.Role.TEACHER)
        userMapper.insert(newUser)
        return simpleSuccessResponse("userId" to newUser.userId, "instituteId" to institute.instituteId)
    }

    @JwtRole(User.Role.TEACHER)
    @PostMapping("/user/student")
    fun createStudent(@JsonParam studentNum : String,
                      @JsonParam studentName : String,
                      @JsonParam password : String,
                      user : User) : String {
        val exist = userMapper.selectOne(QueryWrapper<User>().eq("username", studentName))
        if (exist != null)
            throw RequestException(ResponseCode.IDENTIFIER_ALREADY_EXISTS, "studentName[$studentName]")
        val newUser = User(0, studentName, passwordEncoder.encode(password), studentNum, user.instituteId, User.Role.STUDENT)
        userMapper.insert(newUser)
        return simpleSuccessResponse("userId" to newUser.userId)
    }

    @GetMapping("/user")
    @JwtRole(User.Role.STUDENT, User.Role.TEACHER)
    fun getInfo(@JwtUserId userId : Long) : String {
        val info = userMapper.selectUserInfo(userId)
        return simpleSuccessResponse(info.toFormattedJSONObject())
    }
}