// JwtFilter.java
package com.app.mono.customers.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ✅ 매 요청마다 동작하는 JWT 인증 필터(Access Token 검증용)
 *
 * Spring Security 동작 원리(핵심):
 * - "인가(permitAll / authenticated)" 판단은
 *   SecurityContextHolder 안에 Authentication이 있느냐로 결정됨.
 * - 따라서 이 필터의 목표는:
 *   "요청의 JWT가 유효하면 Authentication을 만들어 SecurityContextHolder에 넣어준다."
 *
 * 🔥 JWT 필터의 표준 흐름
 * 1) Authorization 헤더에서 Bearer 토큰 추출
 * 2) 토큰 서명/만료 검증 + subject(username/email) 추출
 * 3) UserDetailsService로 사용자 정보 조회 (권한 포함)
 * 4) Authentication 생성 후 SecurityContext에 저장
 * 5) 다음 필터로 진행
 *
 * ⚠️ 주의: 이 필터는 "AccessToken" 검증용이어야 한다.
 * - RefreshToken이 여기로 들어오면 안 됨.
 * - 실무에서는 토큰에 type=access/refresh 클레임을 넣고 여기서 access만 허용한다.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * ✅ OncePerRequestFilter
     * - 동일 요청에 대해 필터가 한 번만 실행되도록 보장
     *
     * doFilterInternal은 "모든 요청"에 대해 실행될 수 있으므로,
     * 여기 코드는 반드시 빠르고 안전해야 함.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        /**
         * (0) 이미 인증 정보가 SecurityContext에 들어있으면 굳이 다시 세팅하지 않도록 방어
         *
         * - 보통 한 요청에서 JwtFilter는 1번만 실행되지만,
         *   일부 환경/구성에서 중복 세팅을 피하는 게 안전
         * - 이미 인증된 상태면 그대로 통과시키는 것이 일반적
         */
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        /**
         * (1) Authorization 헤더 읽기
         *
         * 브라우저/클라이언트는 보통 아래 형태로 보냄:
         *   Authorization: Bearer <ACCESS_TOKEN>
         *
         * ⚠️ 실무에서 흔한 오류
         * - "Bearer" 오타
         * - 공백 누락/추가
         * - 다른 헤더에 넣어 보냄(예: X-Auth-Token)
         */
        String header = request.getHeader("Authorization");

        /**
         * (2) Bearer 토큰 형식인지 확인
         *
         * - header == null: 토큰 없이 온 요청 (permitAll 이거나 401로 막힐 요청)
         * - startsWith("Bearer "): 표준 접두사 체크
         */
        if (header != null && header.startsWith("Bearer ")) {

            // "Bearer " 는 7글자 → 실제 토큰만 추출
            String token = header.substring(7);

            /**
             * (2-1) 토큰 문자열이 비어있는지 방어 (실습/운영 모두 유용)
             */
            if (token.isBlank()) {
                // 토큰이 없으면 인증 세팅을 하지 않고 다음 필터로 넘김
                // → 최종적으로 authenticated()가 걸려있으면 Security가 401/403 처리
                filterChain.doFilter(request, response);
                return;
            }

            try {
                /**
                 * ✅ (추가) Claims를 먼저 파싱해서 type 검증까지 수행
                 *
                 * parseAccessClaims(token) 내부에서 이미
                 * - 서명 검증(ACCESS_KEY)
                 * - 만료(exp) 검증
                 * 을 수행함.
                 */
                Claims claims = jwtUtil.parseAccessClaims(token);

                /**
                 * ✅ (추가) 토큰 타입(type) 검증
                 *
                 * - JwtUtil에서 AccessToken 발급 시 claims.put("type","access")를 넣었으므로
                 * - 여기서는 반드시 "access"만 허용해야 함.
                 *
                 * 왜 중요?
                 * - RefreshToken이 Authorization 헤더로 들어오는 실수/공격을 명시적으로 차단
                 */
                String type = String.valueOf(claims.get("type"));
                if (!"access".equals(type)) {
                    SecurityContextHolder.clearContext();

                }else {

                    /**
                     * ✅ subject(username/email) 추출
                     * - 기존 extractUsername(token)과 동일한 의미지만,
                     * - 우리는 이미 claims를 뽑았으므로 여기서 바로 꺼내면 2번 파싱을 피할 수 있음(성능/깔끔)
                     */
                    String username = claims.getSubject();


                    /**
                     * (4) 사용자 정보 조회(UserDetailsService)
                     *
                     * 왜 DB/서비스 조회를 하냐?
                     * - 토큰은 username/email만 들고 있을 수 있고,
                     * - Spring Security 인가(ROLE 체크)는 authorities를 필요로 함
                     * - UserDetails에는 권한(ROLE_*)이 담겨있음
                     *
                     * ⚠️ 성능 고려:
                     * - 매 요청마다 DB 조회가 발생할 수 있음
                     * - 개선하려면:
                     *   a) 토큰에 role을 넣고 바로 authorities를 구성하거나
                     *   b) Redis/Cache로 UserDetails를 캐싱하거나
                     *   c) Gateway 단에서 검증하고 Downstream에는 헤더로 사용자정보를 전달(주의 필요)
                     */
                    UserDetails user = userDetailsService.loadUserByUsername(username);

                    /**
                     * (5) Authentication 생성
                     *
                     * principal: user (인증된 사용자)
                     * credentials: null (이미 JWT로 인증했으니 비밀번호 같은 credential 불필요)
                     * authorities: user.getAuthorities() (ROLE 정보)
                     *
                     * 이 Authentication이 SecurityContext에 들어가야
                     * Controller에서 @AuthenticationPrincipal 사용 가능
                     */
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    /**
                     * (6) SecurityContext에 인증 정보 저장
                     *
                     * 이후 흐름:
                     * - Spring Security의 authorizeHttpRequests 판단에서 authenticated() 통과
                     * - Controller에서 @AuthenticationPrincipal로 user 주입 가능
                     */
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // 그냥 인증 안 된 상태로 둔다
                SecurityContextHolder.clearContext();

            } catch (Exception e) {
                /**
                 * ✅ 기타 토큰 오류 처리
                 *
                 * 예:
                 * - 서명키 불일치(위조 토큰)
                 * - 형식 오류(malformed)
                 * - 지원되지 않는 토큰(alg 등)
                 *
                 * 지금 코드는 401만 내려주고 끝.
                 *
                 * ⚠️ 운영에서는:
                 * - 로그를 남기되(민감정보인 token 전체를 찍으면 안 됨)
                 * - 동일하게 401 반환하는 게 일반적
                 */
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        /**
         * (7) 다음 필터로 진행
         *
         * 토큰이 유효한 경우:
         * - SecurityContext에 Authentication이 있으므로 authenticated() 통과
         *
         * 토큰이 없는/무효한 경우:
         * - SecurityContext가 비어있음
         * - authenticated()가 걸린 URL이면 401/403
         */
        filterChain.doFilter(request, response);
    }
}