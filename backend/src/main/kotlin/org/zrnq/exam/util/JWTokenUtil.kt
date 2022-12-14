package org.zrnq.exam.util

import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.zrnq.exam.entity.User
import org.zrnq.exam.exception.RequestException
import org.zrnq.exam.exception.ResponseCode
import org.zrnq.exam.mapper.UserMapper
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

@Component
class JwtTokenUtil {
    private val client = "webclient"
    private val secret = "aW50ZXJtZWRpYXRlLWRpbGVtbWEtbWVkaWF0aW9uLXdlYmNsaWVudA=="
    private val issuer = "ZRnQ"
    private val expire = 14400000 // 4 hours

    private val jwtParser = Jwts.parserBuilder().setSigningKey(secret).build()

    @Autowired
    lateinit var userMapper : UserMapper

    fun parseJWT(jsonWebToken : String?) : Jws<Claims> {
        return try {
            jwtParser.parseClaimsJws(jsonWebToken)
        } catch (eje : ExpiredJwtException) {
            throw RequestException(ResponseCode.TOKEN_EXPIRED)
        } catch (e : Exception) {
            throw RequestException(ResponseCode.TOKEN_INVALID)
        }
    }

    fun createJWT(userId : Long, username : String, role : String) : String {
        val signatureAlgorithm : SignatureAlgorithm = SignatureAlgorithm.HS256
        val now = Date()
        val expirationDate = Date(now.time + expire)
        val apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secret)
        val signingKey = SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.jcaName)
        return Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .claim("role", role)
            .claim("id", userId)
            .setSubject(username)
            .setIssuer(issuer)
            .setIssuedAt(now)
            .setAudience(client)
            .signWith(signingKey, signatureAlgorithm)
            .setExpiration(expirationDate)
            .setNotBefore(now)
            .compact()
    }

    fun getUser(token : String) : User {
        val claims = parseJWT(token)
        val userId = claims.body.get("id", java.lang.Long::class.java)
        return userMapper.selectById(userId)
    }
}