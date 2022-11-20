package org.zrnq.exam

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@MapperScan("org.zrnq.exam.mapper")
class ExamManagerApplication

fun main(args : Array<String>) {
    runApplication<ExamManagerApplication>(*args)
}
