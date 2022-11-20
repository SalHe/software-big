package org.zrnq.exam.util

import com.alibaba.fastjson.JSONArray
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

@Component
class WordCloud(@Value("\${python.path}") pythonPath : String) : DisposableBean {
    private val logger = org.slf4j.LoggerFactory.getLogger(WordCloud::class.java)
    private val process : Process
    private val bufferedWriter : BufferedWriter
    private val inputStream : InputStream
    private val lock = Object()
    init {
        logger.info("Python path: $pythonPath")
        process = ProcessBuilder().command(listOf(pythonPath, "wordcloud_produce.py"))
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
        bufferedWriter = process.outputStream.bufferedWriter(Charset.forName("gbk"))
        inputStream = process.inputStream
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val queue = LinkedList<Object>() as Queue<Object>
    private var beginGeneration = false
    private var lastCopy = 0
    private val bos = ByteArrayOutputStream()

    fun generateCloud(data : JSONArray) : ByteArray {
        logger.info("=> wordcloud.py:" + data.toJSONString())
        BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)
        val lock = Object()
        val r = synchronized(this.lock) {
            queue.add(lock)
            queue.size > 1
        }
        if(r) synchronized(lock) {
            lock.wait()
        }
        bufferedWriter.write(data.toJSONString())
        bufferedWriter.newLine()
        bufferedWriter.flush()
        beginGeneration = false
        lastCopy = 0
        val t = thread {
            logger.info("Begin word cloud generation")
            val buf = ByteArray(8192)
            var cnt = inputStream.read(buf)
            while(cnt > 0) {
                bos.write(buf)
                lastCopy++
                beginGeneration = true
                cnt = inputStream.read(buf)
            }
        }
        var tlc = lastCopy
        while(!beginGeneration || tlc != lastCopy) {
            tlc = lastCopy
            Thread.sleep(500)
        }
        logger.info("End word cloud generation, $tlc write cycles elapsed.")
        t.interrupt()
        val result = bos.toByteArray()
        bos.reset()
        val next = synchronized(this.lock) {
            val th = queue.remove()
            if(th != lock) {
                logger.warn("Queue inconsistent.")
            }
            if(queue.size > 0) queue.peek()
            else null
        }
        if(next != null) {
            synchronized(next) {
                next.notify()
            }
        }
        return result
    }

    override fun destroy() {
        logger.info("Shutting down word cloud sub-process...")
        process.destroy()
        process.waitFor()
        logger.info("Word cloud service shut down!")
    }
}