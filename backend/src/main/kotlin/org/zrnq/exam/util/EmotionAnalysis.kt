package org.zrnq.exam.util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import java.io.BufferedReader
import java.io.BufferedWriter
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

@Component
class EmotionAnalysis(@Value("\${python.path}") pythonPath : String) : DisposableBean {
    private val logger = org.slf4j.LoggerFactory.getLogger(EmotionAnalysis::class.java)
    private val process : Process
    private val bufferedWriter : BufferedWriter
    private val bufferedReader : BufferedReader
    private val lock = Object()
    private var faceRecognitionThreadClosed = false
    init {
        logger.info("Python path: $pythonPath")
        process = ProcessBuilder().command(listOf(pythonPath, "emotion_evaluate.py"))
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
        bufferedWriter = process.outputStream.bufferedWriter(Charset.forName("gbk"))
        bufferedReader = process.inputStream.bufferedReader()
        thread {
            loop()
            if(!faceRecognitionThreadClosed)
                logger.error("EmotionAnalysis thread exited unexpectedly!")
        }
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val queue = LinkedList<Pair<JSONArray, Object>>() as Queue<Pair<JSONArray, Object>>
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val results = ConcurrentHashMap<Object, JSONArray>()

    fun analyseEmotion(data : JSONArray) : JSONArray {
        logger.info("=> emotion.py:" + data.toJSONString())
        val lock = Object()
        queue.add(data to lock)
        synchronized(lock) {
            synchronized(this.lock) {
                this.lock.notify()
            }
            lock.wait()
        }
        return synchronized(this.lock) {
            results.remove(lock) ?: throw RequestException(ResponseCode.CLOUD_GENERATION_FAILED)
        }
    }

    private fun loop() {
        logger.info("EmotionAnalysis thread started")
        while(true) {
            synchronized(lock) {
                if(queue.isEmpty()) {
                    lock.wait()
                }
            }
            if(faceRecognitionThreadClosed)
                break
            val (data, lock) = synchronized(lock) {
                 queue.poll()
            }
            bufferedWriter.write(data.toJSONString())
            bufferedWriter.newLine()
            bufferedWriter.flush()
            val result = JSON.parseArray(bufferedReader.readLine())
            synchronized(this.lock) {
                results[lock] = result
                synchronized(lock) {
                    lock.notify()
                }
            }
        }
    }

    override fun destroy() {
        logger.info("Shutting down emotion analysis thread...")
        faceRecognitionThreadClosed = true
        synchronized(lock) {
            lock.notify()
        }
        logger.info("Shutting down emotion analysis sub-process...")
        process.destroy()
        process.waitFor()
        logger.info("Emotion analysis service shut down!")
    }
}