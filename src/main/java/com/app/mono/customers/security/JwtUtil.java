// JwtUtil.java
package com.app.mono.customers.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Base64;
import java.util.Date;

/**
 * ✅ JWT 생성/파싱 유틸리티
 *
 * 이 클래스가 "토큰의 규격"을 결정한다.
 * - 어떤 알고리즘(HS512)로 서명하는지
 * - 어떤 Key로 서명하는지(Access/Refresh 키 분리)
 * - 어떤 Claim(subject/role 등)을 넣는지
 * - 만료 시간을 얼마로 할지(exp)
 *
 * 🔥 토큰 종류(현재 설계)
 * - Access Token (AT): 짧게 (실습: 30초 / 운영: 보통 5~30분)
 *   → 매 요청 Authorization: Bearer {AT} 로 보냄
 *
 * - Refresh Token (RT): 길게 (현재 14일)
 *   → /auth/refresh 에서만 사용 (재발급 권한)
 *   → Redis(DB)에 저장해서 "서버가 인정하는 RT"인지 통제
 */
@Component
@Getter
public class JwtUtil {

    /**
     * ✅ AccessToken 서명용 비밀키(문자열, Base64 인코딩된 값)
     *
     * application.yml 에서 주입:
     * jwt.secretKey: "Base64EncodedSecret..."
     *
     * ⚠️ 실무 보안:
     * - 소스/레포에 커밋하면 안 됨 (환경변수/Secret Manager 사용 권장)
     * - 충분히 길고 랜덤한 값을 써야 함
     */
    @Value("${jwt.secretKey}")
    private String secretKey;

    /**
     * ✅ RefreshToken 서명용 비밀키(Base64)
     *
     * Access/Refresh 키를 분리하는 이유:
     * - RT는 더 민감(장기 유효) → 별도 키로 관리하면 키 로테이션/유출 대응이 쉬움
     * - 만약 Access 키가 노출되어도 Refresh 키까지 바로 무너지지 않게 분리 가능
     */
    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    /**
     * ✅ 실제 서명에 사용할 SecretKey 객체
     *
     * - JJWT는 signWith에 SecretKey 객체를 넣는 방식이 권장됨
     * - 문자열 그대로 쓰는 방식보다 안전하고 명확함
     */
    private SecretKey ACCESS_KEY;
    private SecretKey REFRESH_KEY;

    /**
     * ✅ @PostConstruct
     * - 스프링이 Bean 생성/의존성 주입을 끝낸 직후에 한 번 실행됨
     *
     * 여기서 하는 일:
     * - Base64 문자열을 decode 해서 byte[]로 만든 뒤
     * - HMAC용 SecretKey(HS512)를 만든다.
     *
     * ⚠️ 중요한 실무 체크(HS512 키 길이)
     * - HS512는 키 길이가 충분히 길어야 함.
     * - 너무 짧으면 jjwt가 예외를 던질 수 있고, 보안적으로도 취약해짐.
     * - Keys.hmacShaKeyFor(...)는 키 길이가 부족하면 IllegalArgumentException 가능.
     */
    @PostConstruct
    public void init() {
        byte[] accessKeyBytes = Base64.getDecoder().decode(secretKey);
        byte[] refreshKeyBytes = Base64.getDecoder().decode(secretKeyRt);

        // HMAC 키는 SecretKey로 만드는 것이 정석 (서명/검증 모두 동일 키 사용)
        ACCESS_KEY = Keys.hmacShaKeyFor(accessKeyBytes);
        REFRESH_KEY = Keys.hmacShaKeyFor(refreshKeyBytes);
    }

    /**
     * ✅ Access Token 생성
     *
     * payload(Claims) 구성:
     * - subject : email (사용자 식별자)
     * - role    : 권한(인가 판단용)
     *
     * exp(만료):
     * - 지금은 실습용으로 30초
     * - 운영에서는 보통 5~30분 내로 설정
     *
     * 🔥 토큰 관점에서 매우 중요한 포인트
     * - AccessToken은 "자주 바뀌는 증명서"라 짧아야 함
     * - 탈취되어도 피해 시간을 줄이기 위함
     *
     * ⚠️ 주석에 type=access 라고 적혀 있지만,
     * 현재 코드에는 claims.put("type","access")가 없음.
     * - 이게 없으면 JwtFilter에서 access/refresh 구분이 어려워짐(실수 위험)
     */
    public String createToken(String email, String role) {

        // (1) Claims 생성 + subject 지정 (subject는 보통 username/email/userId 중 하나)
        Claims claims = Jwts.claims().setSubject(email);

        // (2) 사용자 권한(ROLE) 저장 → downstream에서 인가에 활용 가능
        claims.put("role", role);

        // (권장) Access/Refresh 구분을 위해 type 클레임을 넣는 것이 안전
         claims.put("type", "access");

        Date now = new Date();

        // 운영 예시: 15분
        // Date exp = new Date(now.getTime() + 1000L * 60 * 15);

        // 실습용: 120초
        Date exp = new Date(now.getTime() + 1000L * 60 * 15);

        /**
         * (3) JWT 빌드
         *
         * - setIssuedAt(now) : 발급시간(iat)
         * - setExpiration(exp): 만료시간(exp)
         * - signWith(ACCESS_KEY, HS512): HMAC-SHA512로 서명
         *
         * ✅ 결과:
         * - "서명된 문자열 토큰"이 생성됨
         * - 서버는 같은 ACCESS_KEY로 검증 가능
         */
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(ACCESS_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * ✅ Refresh Token 생성
     *
     * payload(Claims):
     * - subject : email
     * - role    : role (현재는 포함)
     *
     * exp:
     * - 현재 14일
     *
     * 🔥 RefreshToken의 핵심 역할
     * - "새 AccessToken을 받을 자격"을 증명
     * - 따라서 반드시 서버 저장소(Redis/DB)와 매칭해서 통제해야 함
     *
     * ⚠️ 권장:
     * - claims.put("type","refresh")를 넣어서
     *   AccessToken/RefreshToken 혼용 실수를 원천차단
     */
    public String createRefreshToken(String email, String role) {

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        // (권장) 토큰 타입을 명확히 표시
         claims.put("type", "refresh");

        Date now = new Date();
        Date exp = new Date(now.getTime() + 1000L * 60 * 60 * 24 * 14); // 14일

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(REFRESH_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * ✅ Access 토큰 Claims 파싱(검증 포함)
     *
     * parseClaimsJws(token) 호출 시 내부적으로 수행되는 것:
     * - 서명 검증(ACCESS_KEY로 검증)
     * - exp 만료 검증 (만료면 ExpiredJwtException 발생)
     * - 구조 검증 (형식이 이상하면 예외 발생)
     *
     * 즉, "파싱"이지만 사실상 "검증"이다.
     */
    public Claims parseAccessClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(ACCESS_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * ✅ Refresh 토큰 Claims 파싱(검증 포함)
     *
     * - Refresh 전용 키(REFRESH_KEY)로 검증한다는 점이 핵심
     * - Access 토큰을 여기로 넣으면 검증 실패(서명키가 다르므로)
     */
    public Claims parseRefreshClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(REFRESH_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * ✅ 토큰에서 username(email=subject) 추출
     *
     * JwtFilter에서 사용 중:
     * - Authorization 헤더의 AccessToken을 전달받아
     * - 여기서 parseAccessClaims()가 서명/만료 검증까지 수행
     *
     * 만료 시:
     * - ExpiredJwtException 발생
     * - JwtFilter에서 캐치해서 401 처리
     */
    public String extractUsername(String token) {
        return parseAccessClaims(token).getSubject();
    }

    /**
     * ✅ 만료 여부 확인
     *
     * ⚠️ 주의:
     * - 아래 메서드들은 parse*Claims()를 호출하므로
     *   "이미 만료된 토큰"은 여기서도 ExpiredJwtException이 발생할 수 있음.
     *
     * 즉, boolean 체크처럼 보이지만 실제로는 예외가 날 수 있다.
     *
     * 실무 패턴:
     * - 만료 여부를 체크할 때는 try/catch로 ExpiredJwtException을 처리해서
     *   "만료=true"로 간주하는 방식이 더 안전한 경우가 많다.
     */

    /** Access 토큰 만료 여부 */
    public boolean isAccessExpired(String accessToken) {
        return parseAccessClaims(accessToken).getExpiration().before(new Date());
    }

    /** Refresh 토큰 만료 여부 */
    public boolean isRefreshExpired(String refreshToken) {
        return parseRefreshClaims(refreshToken).getExpiration().before(new Date());
    }
}