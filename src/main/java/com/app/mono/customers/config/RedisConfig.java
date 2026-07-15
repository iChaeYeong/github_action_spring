package com.app.mono.customers.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 🔥 Redis 설정 클래스
 *
 * - JWT Refresh Token 저장
 * - AccessToken 블랙리스트 저장
 * - 로그인 세션 관리
 * - 임시 인증 코드 저장
 *
 * 등을 위해 Redis 연결을 설정하는 클래스
 *
 * @Configuration
 *  → Spring Boot가 시작될 때 Bean으로 등록됨
 */
@Configuration
public class RedisConfig {

    /**
     * application.yml 또는 application.properties 에서
     * Redis 서버 주소를 주입받음
     *
     * 예)
     * spring:
     *   data:
     *     redis:
     *       host: localhost
     */
    @Value("${spring.data.redis.host}")
    private String host;

    /**
     * Redis 포트 주입
     * 기본값은 6379
     */
    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * 🔥 Redis 연결 객체 생성
     *
     * RedisConnectionFactory 는
     * Redis 서버와 실제 연결을 관리하는 객체
     *
     * @Bean
     *  → Spring Container에 Bean 등록
     *
     * @Qualifier("rtdb")
     *  → 같은 타입 Bean이 여러 개 있을 때 구분하기 위해 사용
     *
     * 실무 TIP:
     *  - JWT Refresh Token 저장용 Redis
     *  - 캐시용 Redis
     *  - 세션용 Redis
     *  이렇게 여러 Redis를 나눌 수도 있음
     */
    @Bean("rtdb")
    public RedisConnectionFactory redisConnectionFactory(){

        // 단일 Redis 서버 설정
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        // Redis 서버 주소 설정
        configuration.setHostName(host);

        // Redis 포트 설정
        configuration.setPort(port);

        // Redis DB 번호 (기본 0)
        // 하나의 Redis 서버 안에서 논리적으로 DB를 분리 가능
        configuration.setDatabase(0);

        // Lettuce는 비동기/논블로킹 Redis Client (Spring Boot 기본)
        return new LettuceConnectionFactory(configuration);
    }

    /**
     * 🔥 RedisTemplate 생성
     *
     * RedisTemplate은 실제 Redis에 데이터를
     * 저장/조회할 때 사용하는 핵심 객체
     *
     * <String, Object>
     *  - key는 String
     *  - value는 Object
     *
     * ⚠️ JWT 관련 실무 중요 포인트:
     *  - RefreshToken은 보통 String
     *  - AccessToken 블랙리스트도 String
     *  - 사용자 ID를 key로 쓰는 경우가 많음
     *
     * 예)
     *   key   : refresh:userId
     *   value : 실제 RefreshToken 문자열
     *
     * redisTemplate은 반드시 하나 이상 정의되어야 함.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(
            @Qualifier("rtdb") RedisConnectionFactory redisConnectionFactory){

        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

        /**
         * 🔥 Key 직렬화 설정
         *
         * Redis는 기본적으로 byte[]로 저장함.
         * 직렬화 설정을 안하면 깨진 값으로 보일 수 있음.
         *
         * StringRedisSerializer
         *  → 사람이 읽을 수 있는 문자열 형태로 저장됨
         *
         * JWT 토큰은 문자열이므로 StringSerializer 사용이 적절함
         */
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        /**
         * 🔥 Value 직렬화 설정
         *
         * 현재는 StringRedisSerializer로 지정
         *
         * ⚠️ 주의:
         *  - Object 타입을 쓰고 있지만
         *  - 실제로는 JWT 문자열 저장이 목적이라면 String이 더 안전함
         *
         * 실무에서는:
         *  - Jackson2JsonRedisSerializer
         *  - GenericJackson2JsonRedisSerializer
         *  등을 사용하는 경우도 많음
         */
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        /**
         * Redis 연결 설정
         */
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        return redisTemplate;
    }

}