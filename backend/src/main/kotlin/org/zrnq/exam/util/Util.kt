package org.zrnq.exam.util

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import org.zrnq.exam.entity.Course
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import org.zrnq.exam.exception.cascadeSuccessResponse
import org.zrnq.exam.exception.mixedSuccessResponse
import java.text.SimpleDateFormat

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

const val mybatisPlusInjection = "\${ew.customSqlSegment}"
val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

fun validate(page : Int, limit : Int) {
    if(page < 1)
        throw RequestException(ResponseCode.ILLEGAL_PARAMETER, "page[$page]")
    if(limit !in 1..50)
        throw RequestException(ResponseCode.ILLEGAL_PARAMETER, "limit[$limit]")
}

fun checkImageSize(image : MultipartFile) {
    if(image.size > 3145700)
        throw RequestException(ResponseCode.FILE_SIZE_LIMIT)
}

fun autoPagingProcessor(page : Int?,
                        limit : Int?,
                        pagingSelect : (Long, Long) -> IPage<out Any>,
                        fullSelect : () -> List<Any>) : String {
    return if(page == null && limit == null) {
        val list = fullSelect()
        cascadeSuccessResponse(list)
    } else if(page == null || limit == null) {
        throw RequestException(ResponseCode.ILLEGAL_PARAMETER, "page[$page], limit[$limit]")
    } else {
        validate(page, limit)
        val courseList = pagingSelect(page.toLong(), limit.toLong())
        mixedSuccessResponse(courseList.records, "count" to courseList.total)
    }
}

fun <T> List<T>.removeDuplicate() : List<T> {
    val set = mutableSetOf<T>()
    forEach {
        set.add(it)
    }
    return set.toList()
}