package org.zrnq.exam.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.charset.StandardCharsets

@Configuration
class IntegratedWebMvcConfig : WebMvcConfigurer {
    @Autowired
    lateinit var jwtInterceptor : JwtInterceptor
    @Autowired
    lateinit var jwtUserParameterResolver : JwtUserParameterResolver
    @Autowired
    lateinit var jwtUserIdParameterResolver : JwtUserIdParameterResolver
    @Autowired
    lateinit var fastJsonParameterResolver : FastJsonParameterResolver
    override fun addArgumentResolvers(argumentResolvers : MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(jwtUserParameterResolver)
        argumentResolvers.add(jwtUserIdParameterResolver)
        argumentResolvers.add(fastJsonParameterResolver)
        super.addArgumentResolvers(argumentResolvers)
    }

    override fun addInterceptors(registry : InterceptorRegistry) {
        registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/**")
        super.addInterceptors(registry)
    }
    /**
     * 允许跨域请求
     */
    override fun addCorsMappings(registry : CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*")
        super.addCorsMappings(registry)
    }

    /**
     * 设置返回类型为application/json，无需设置编码，主流浏览器默认为UTF-8
     * */
    override fun configureMessageConverters(converters : MutableList<HttpMessageConverter<*>>) {
        converters.add(object : StringHttpMessageConverter(StandardCharsets.UTF_8) {
            init {
                super.setSupportedMediaTypes(listOf(MediaType.APPLICATION_JSON, MediaType.ALL))
            }
        })
        converters.add(object : ByteArrayHttpMessageConverter() {
            init {
                super.setSupportedMediaTypes(listOf(MediaType.IMAGE_PNG, MediaType.ALL))
            }
        })
        super.configureMessageConverters(converters)
    }
}