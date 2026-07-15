// SecurityConfig.java
package com.app.mono.customers.config;

import com.app.mono.customers.exception.RestAccessDeniedHandler;
import com.app.mono.customers.exception.RestAuthenticationEntryPoint;
import com.app.mono.customers.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 🔐 Spring Security 설정 클래스 (JWT Stateless 인증)
 *
 * ✅ 이 클래스의 역할(토큰 관점)
 * 1) "세션 기반 로그인"을 끄고, "JWT 토큰 기반 인증"으로 전환
 * 2) 요청이 들어올 때, Authorization 헤더의 JWT를 먼저 검증하도록 "JwtFilter"를 필터 체인에 배치
 * 3) 어떤 URL은 토큰 없이 허용(permitted), 나머지는 토큰이 있어야 통과(authenticated)
 * 4) 프론트(Vue/React)에서 호출 가능하도록 CORS 정책을 명시
 *
 * ⚠️ JWT 구조에서 가장 자주 발생하는 실수
 * - JwtFilter 위치가 잘못되어 토큰이 인증으로 반영되지 않음
 * - sessionCreationPolicy를 STATELESS로 안 해서 세션/토큰 혼합 상태가 됨
 * - CORS 설정이 누락되어 브라우저에서 Authorization 헤더가 막힘
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * ✅ 우리가 직접 구현한 JWT 검증 필터
     *
     * JwtFilter 내부에서 하는 일(일반적 흐름):
     * - Authorization 헤더에서 "Bearer {token}" 추출
     * - 서명(Secret Key) 검증 + 만료시간(exp) 검증 + 클레임 추출
     * - 유효하면 SecurityContextHolder 에 Authentication 을 넣어 "인증 완료 상태"로 만든다.
     *
     * 즉, JwtFilter는 "토큰을 로그인 상태로 바꿔주는 관문" 역할이다.
     */
    private final JwtFilter jwtFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    /**
     * ✅ Security Filter Chain 설정
     *
     * Spring Security는 요청이 들어오면 "Filter Chain"을 통과시키며,
     * 그 과정에서 인증(Authentication) / 인가(Authorization)를 처리한다.
     *
     * JWT 핵심 포인트:
     * - UsernamePasswordAuthenticationFilter(폼 로그인용)보다 "먼저" JwtFilter가 실행되어야 한다.
     *   → 그래야 토큰 기반으로 Authentication이 세팅되고, 이후 인가 판단(anyRequest().authenticated())이 정상 동작한다.
     *
     * @param http 보안 설정 빌더
     * @return 최종 SecurityFilterChain
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                /**
                 * (1) CSRF 비활성화
                 *
                 * CSRF는 "브라우저 쿠키 기반 세션 인증"에서 주로 방어하는 공격.
                 * JWT를 Authorization 헤더로 보내는 구조는 일반적으로 CSRF 리스크가 낮아서 비활성화하는 경우가 많다.
                 *
                 * ⚠️ 단, JWT를 쿠키(HttpOnly cookie)에 저장해서 보내는 구조면 CSRF 방어가 다시 필요할 수 있다.
                 */
                .csrf(csrf -> csrf.disable())

                /**
                 * (2) CORS 설정
                 *
                 * 프론트(Vue/React)에서 API 요청 시,
                 * 브라우저가 OPTIONS Preflight를 보내고 허용 여부를 검사한다.
                 *
                 * ⚠️ 특히 JWT는 Authorization 헤더를 쓰므로,
                 * CORS에서 allowedHeaders / exposedHeaders / allowCredentials 설정이 꼬이면
                 * "프론트에서 토큰을 못 보내거나(차단)" 또는 "응답 헤더를 못 읽는" 문제가 생긴다.
                 */
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable) // http basic 보안방식 비활성화
                /**
                 * (3) 세션 비활성화 (Stateless)
                 *
                 * ✅ JWT는 "서버가 상태(세션)를 저장하지 않는" 인증 방식이므로 STATELESS가 기본이다.
                 *
                 * 이 설정의 의미:
                 * - 서버는 HttpSession을 만들거나 저장하지 않는다.
                 * - 매 요청마다 JWT가 있어야 인증이 된다. (토큰이 곧 로그인 증명)
                 *
                 * ⚠️ 이걸 안 하면:
                 * - 토큰 인증과 세션 인증이 섞여서 디버깅이 지옥이 될 수 있다.
                 */
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                /**
                 * (4) URL별 인가 규칙(Authorization)
                 *
                 * requestMatchers("/auth/**").permitAll()
                 *  → 로그인/회원가입/토큰 재발급 같은 "토큰 발급 API"는
                 *    당연히 토큰 없이 접근 가능해야 한다.
                 *
                 * anyRequest().authenticated()
                 *  → 그 외는 모두 토큰이 필요함.
                 *    (JwtFilter가 Authentication을 세팅해줘야 통과 가능)
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/signup-accounts").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
//                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )

                /**
                 * (5) JWT 필터 위치 지정
                 *
                 * addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                 *  → UsernamePasswordAuthenticationFilter 보다 앞에서 jwtFilter 실행
                 *
                 * 왜 "before"가 중요하냐?
                 * - 인가 단계에서 authenticated() 판단은
                 *   SecurityContextHolder 안의 Authentication 유무로 결정된다.
                 * - JwtFilter가 먼저 실행되어 Authentication을 넣어줘야 한다.
                 *
                 * ⚠️ 만약 이 순서가 뒤로 가면:
                 * - 토큰이 있어도 인증이 안된 것으로 처리되어 401/403이 발생
                 */
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // 최종 보안 필터 체인을 생성해서 Spring Security에 등록
        return http.build();
    }

    /**
     * ✅ PasswordEncoder (비밀번호 해싱)
     *
     * 회원가입 시:
     * - raw password를 그대로 DB에 저장하면 절대 안됨
     * - BCrypt로 해싱해서 저장
     *
     * 로그인 시:
     * - 입력값(raw)과 DB의 해시값을 비교(PasswordEncoder.matches)
     *
     * JWT와의 연결점:
     * - 로그인 성공 후(인증 완료 후) JWT를 발급한다.
     * - 즉, 비밀번호 검증은 "토큰 발급의 전제조건"이다.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ✅ AuthenticationManager
     *
     * Spring Security의 "인증 엔진"
     * 보통 로그인 API(/auth/login)에서 아래처럼 사용:
     *
     * Authentication auth =
     *   authenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken(username, password)
     *   );
     *
     * 인증이 성공하면:
     * - auth.getPrincipal() 에 UserDetails 가 들어오고
     * - 그 사용자 정보로 JWT(Access/Refresh)를 발급한다.
     *
     * 즉, AuthenticationManager는 "토큰 발급 이전 단계의 공식 인증 절차"이다.
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ✅ CORS 설정 (브라우저 기반 프론트 연동 필수)
     *
     * allowedOrigins:
     * - 허용할 프론트 도메인
     *
     * allowedMethods:
     * - 허용할 HTTP 메서드
     *
     * allowedHeaders:
     * - 프론트가 보내는 헤더 허용
     *   (JWT 사용 시 Authorization 헤더 허용이 매우 중요)
     *
     * allowCredentials:
     * - 쿠키/인증정보 포함 요청 허용
     *
     * ⚠️ JWT를 Authorization 헤더로만 보낼 거면
     * allowCredentials=true 가 필수는 아니지만,
     * "쿠키 기반 리프레시 토큰" 같은 설계를 섞으면 필요해질 수 있다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // ✅ 허용할 프론트엔드 주소 (개발 서버)
        cfg.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));

        // ✅ 허용할 HTTP 메서드
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // ✅ 허용할 요청 헤더
        // 실무에서는 Authorization을 명시해두면 더 명확함.
        // cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        cfg.setAllowedHeaders(List.of("*"));

        // ✅ 쿠키/인증정보 포함 요청 허용 (필요 시)
        cfg.setAllowCredentials(true);

        // 설정을 모든 경로에 적용
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}