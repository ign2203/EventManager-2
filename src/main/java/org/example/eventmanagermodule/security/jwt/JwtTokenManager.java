package org.example.eventmanagermodule.security.jwt;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


/*
создаем отдельный класс по выдаче токенов
в пропертис файле указали
jwt.lifetime=864000000 // вроде бы это время жизни токена, равна суткам
по времени жизни углубимся позже
jwt.secretKey = FF14A62F5B9D7B20A3E89885FF35E486413277C075B9D02DD98A0AF640B656B8

 */
@Component
public class JwtTokenManager {

    private final SecretKey key; // что такое тип SecretKey я не понимаю
    private final long expirationTime;

    // это конструктор для создания самого JwtTokenManager
    public JwtTokenManager(
            @Value("${jwt.secretKey}") String keyString,
            @Value("${jwt.lifetime}") Long expirationTime
    ) {
        this.key = Keys.hmacShaKeyFor(keyString.getBytes());// как будто я какую то зависимость не подтянул Keys подчеркаивается
        this.expirationTime = expirationTime;
    }
/*
keyString.getBytes() → превращает строку из application.properties в массив байтов.
Keys.hmacShaKeyFor(...) → не хэширует ключ, а просто создаёт объект SecretKey, который подходит для алгоритма HMAC-SHA256.
 */


    public String generateToken(String login) {
        return Jwts
                .builder()
                .subject(login) // subject → кто (user) -- подчеркивается subject
                .issuedAt(new Date()) //issuedAt → когда создан
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) //expiration → до какого времени валиден
                .signWith(key) //signWith(key) → подпись (обязательно безопасный ключ)
                .compact()//compact() → получает финальную строку
                ;
    }

    public String getLoginFromToken(String token) {
        return Jwts
                .parser()// разделять на HEADER, PAYLOAD, SIGNATURE, проверять подпись.
                .verifyWith(key) //«Используй этот SecretKey для проверки подписи».
                .build()//Получаем готовый объект парсера, которым можно распарсить токен.
                .parseSignedClaims(token)// // здесь не понимаю
                .getPayload()// Получает содержимое PAYLOAD токена.
                .getSubject(); // Этот вызов достаёт именно логин,
    }
}







