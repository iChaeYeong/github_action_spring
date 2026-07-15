// AuthController.java
package com.app.mono.customers.controller;

import com.app.mono.accounts.constants.AccountsConstants;

import com.app.mono.common.ResponseDto;
import com.app.mono.customers.constants.CustomersConstants;
import com.app.mono.customers.dto.CustomerDto;
import com.app.mono.customers.dto.LoginDTO;
import com.app.mono.customers.dto.TokenRefreshDTO;
import com.app.mono.customers.security.JwtUtil;
import com.app.mono.customers.service.CustomersService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ✅ 인증/인가(토큰) 관련 REST 컨트롤러
 *
 * 엔드포인트:
 * - POST /auth/signup  : 회원가입(계정 생성)
 * - POST /auth/login   : 로그인 성공 시 AccessToken + RefreshToken 발급
 * - POST /auth/refresh : RefreshToken 검증 후 새 AccessToken 발급
 * - POST /auth/logout  : RefreshToken 폐기(로그아웃)
 *
 * 🔥 토큰 설계 관점 핵심
 * - AccessToken(AT): 짧게(예: 5~30분). 매 요청 Authorization 헤더로 보냄.
 * - RefreshToken(RT): 길게(예: 7~30일). 서버 저장소(Redis/DB)에 저장해서 "재발급 권한"을 통제.
 *
 * ✅ 왜 RT를 서버에 저장하나?
 * - JWT는 기본적으로 stateless라 서버가 토큰을 기억하지 않음.
 * - 하지만 "로그아웃/강제 로그아웃/탈취 대응"을 하려면 RT는 서버에 저장해 비교해야 함.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomersService customersService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * ✅ RefreshToken 저장소 (Redis)
     *
     * 현재 설계:
     * - key   : email
     * - value : refreshToken 문자열
     * - TTL   : 20일
     *
     * ⚠️ 실무 팁:
     * - key를 "refresh:{email}" 같은 prefix로 두는 게 안전(다른 데이터와 충돌 방지)
     * - 한 계정의 동시 로그인 정책(여러 디바이스 허용/불허)에 따라 key 설계가 달라짐
     */


    private  final RedisTemplate<String, String> redisTemplate;

//    public AuthController(MemberService memberService, JwtUtil jwtUtil,
//                          RedisTemplate<String, String> redisTemplate,
////                          PasswordEncoder passwordEncoder,
//                          AuthenticationManager authenticationManager) {
//        this.memberService = memberService;
//        this.jwtUtil = jwtUtil;
//        this.redisTemplate = redisTemplate;
////        this.passwordEncoder = passwordEncoder;
//        this.authenticationManager= authenticationManager;
//    }


    /**
     * ✅ 로그인
     *
     * 전체 흐름:
     * 1) Spring Security 인증(AuthenticationManager.authenticate)
     * 2) 인증 성공 시 AccessToken / RefreshToken 생성
     * 3) RefreshToken을 Redis에 저장(TTL 부여) → 서버가 RT를 "기억"하게 함
     * 4) 클라이언트에게 AT(+RT) 전달
     *
     * ⚠️ 실무에서 중요한 논점(토큰 전달 방식)
     * - 지금 코드는 AT/RT를 모두 JSON Body로 내려줌
     *   → XSS 취약점이 있으면 RT 탈취 위험이 큼(브라우저 localStorage 저장 시 특히 위험)
     * - 보안 강화:
     *   - RT는 HttpOnly Secure 쿠키로만 내려주고,
     *   - Body에는 AT만 내려주는 방식이 많이 쓰임
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto, HttpServletResponse res) {

        /**
         * (1) ✅ Spring Security 인증 수행
         *
         * UsernamePasswordAuthenticationToken(email, password)
         * - "로그인 시도" 토큰 객체
         * AuthenticationManager가 UserDetailsService + PasswordEncoder를 사용해 검증함
         *
         * 인증 실패 시:
         * - BadCredentialsException 등 예외가 터지고
         * - 기본적으로 401로 응답되도록 처리(전역 예외 처리 여부에 따라 다름)
         *
         * ✅ 이 단계가 "JWT 발급의 전제조건"임
         * - 즉, 비밀번호 검증을 통과해야만 아래에서 JWT 발급
         */
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        /**
         * (2) ✅ 사용자 정보 조회
         *
         * 주의:
         * - authenticationManager.authenticate()가 이미 인증을 했는데,
         *   여기서 memberService.login(dto)가 또 비밀번호 검증을 한다면 "중복 인증"이 될 수 있음.
         *
         * 실무 권장:
         * - authenticate() 결과(Authentication)에서 principal(UserDetails)을 꺼내서 사용자 정보를 쓰거나
         * - memberService.login(dto)는 "사용자 조회(role/id)"만 하도록 역할을 분리
         */
        CustomerDto customerDto = customersService.login(dto);

        /**
         * (3) ✅ 토큰 생성
         *
         * AccessToken:
         * - 보통 짧은 만료(예: 15분)
         * - 매 API 요청 시 Authorization: Bearer {AT} 로 사용
         *
         * RefreshToken:
         * - 보통 긴 만료(예: 14~30일)
         * - 오직 /auth/refresh 같은 재발급 요청에만 사용
         *
         * role을 토큰에 넣는 이유:
         * - Gateway/Resource Server에서 인가(ROLE) 판정에 사용할 수 있음
         *
         * ⚠️ 주의:
         * - role 클레임은 "토큰 발급 당시"의 role이므로,
         *   사용자의 권한이 변경되면 기존 토큰은 즉시 반영되지 않는다.
         *   (즉시 반영이 필요하면 블랙리스트/토큰 버전(version) 전략이 필요)
         */
        String accessToken = jwtUtil.createToken(dto.getEmail(), customerDto.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(dto.getEmail(), customerDto.getRole().name());

        /**
         * (4) ✅ RefreshToken을 Redis에 저장 + TTL 부여
         *
         * key: email
         * value: refreshToken
         * TTL: 20일
         *
         * 토큰 보안 효과:
         * - 클라이언트가 RT를 들고 와도,
         *   서버(Redis)에 저장된 RT와 "일치"해야만 재발급 허용
         * - 로그아웃 시 Redis에서 RT를 삭제하면,
         *   이후 재발급이 불가능해짐(강제 로그아웃 효과)
         *
         * ⚠️ 실무 주의:
         * - Redis TTL(20일)과 RT 자체 exp(만료시간)이 "반드시" 비슷해야 함
         *   - RT exp는 14일인데 Redis TTL이 20일이면: Redis에는 남아있어도 토큰은 이미 만료(재발급 실패)
         *   - RT exp는 30일인데 Redis TTL이 20일이면: RT는 유효한데 Redis에서 먼저 삭제됨(재발급 실패)
         *
         * ✅ 권장:
         * - RT 생성 시 만료시간을 받아서 TTL을 그 값으로 동일하게 설정하거나,
         * - 단일 설정값으로 RT exp와 Redis TTL을 같이 맞추기
         */
        redisTemplate.opsForValue().set(dto.getEmail(), refreshToken, 20, TimeUnit.DAYS);

        /**
         * (5) ✅ 토큰 응답
         *
         * 지금은 token(AT) + refreshToken(RT) 둘 다 Body로 내려줌
         *
         * ⚠️ 보안 강화 권장(브라우저 기반 프론트라면):
         * - RT는 HttpOnly Secure SameSite 쿠키로 전달
         * - Body에는 AT만 전달
         *
         * 그래야:
         * - XSS가 나도 JS로 RT를 읽을 수 없어서 탈취 위험이 줄어듦
         */
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", customerDto.getCustomerId());
        loginInfo.put("token", accessToken);
        loginInfo.put("refreshToken", refreshToken);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    /**
     * ✅ RefreshToken으로 새 AccessToken 발급
     *
     * 재발급 규칙(현재 구현):
     * 1) 요청 body의 refreshToken 존재 확인
     * 2) refreshToken을 파싱(서명/만료 검증) → Claims 추출
     * 3) Claims.subject(email)로 Redis에 저장된 RT 조회
     * 4) Redis RT와 요청 RT가 "일치"하면 새 AccessToken 발급
     *
     * ✅ 이 구조의 장점:
     * - "토큰 문자열"이 탈취되어도,
     *   서버에 저장된 RT와 비교하는 관문이 있어야 재발급 가능
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> generateNewRt(@RequestBody TokenRefreshDTO dto ) {

        // (0) 요청 유효성 검사
        if (dto == null || dto.getRefreshToken() == null || dto.getRefreshToken().isBlank()) {
            return new ResponseEntity<>("refreshToken is required", HttpStatus.BAD_REQUEST);
        }

        /**
         * (1) ✅ RefreshToken 파싱/검증
         *
         * parseRefreshClaims() 내부에서 기대되는 검증:
         * - 서명(signature) 검증 (secret key)
         * - 만료(exp) 검증
         * - 토큰 타입 구분(선택): "type=refresh" 같은 클레임을 넣고 체크하면 더 안전
         *
         * JwtException:
         * - 서명 틀림 / 만료 / 형식 오류 등
         */
        Claims claims;

        try {
            claims = jwtUtil.parseRefreshClaims(dto.getRefreshToken());

            Object typeObj = claims.get("type");

            // 🔥 null 방어 + 타입 검증
            if (!(typeObj instanceof String) || !"refresh".equals(typeObj)) {
                return new ResponseEntity<>("invalid token type", HttpStatus.UNAUTHORIZED);
            }

        } catch (JwtException e) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        // (2) ✅ subject(email) 추출 (refresh 토큰을 만들 때 subject를 email로 넣었다는 전제)
        String email = claims.getSubject();

        /**
         * (3) ✅ Redis에 저장된 RT와 비교(서버 측 "세션" 역할)
         *
         * Redis에 저장된 값이 없으면:
         * - 로그아웃했거나
         * - TTL 만료되었거나
         * - 서버가 RT를 인정하지 않는 상태
         *
         * "mismatch"이면:
         * - 다른 디바이스에서 로그인해서 RT가 갱신되었거나(단일 로그인 정책)
         * - 탈취된 토큰일 가능성
         */
        Object rt = redisTemplate.opsForValue().get(email);
        if (rt == null || !rt.toString().equals(dto.getRefreshToken())) {
            return new ResponseEntity<>("refresh token mismatch", HttpStatus.UNAUTHORIZED);
        }

        /**
         * (4) ✅ 새 AccessToken 발급
         *
         * role을 claims에서 꺼내오는 방식
         *
         * ⚠️ 보안/정합성 고려:
         * - role이 토큰에 박혀있어서 "발급 당시 role"이 계속 유지됨
         * - 사용자의 role이 바뀌었을 때 즉시 반영하려면:
         *   - 여기서 DB 조회로 최신 role을 가져와서 발급하는 방식이 더 정확함
         *
         * (지금 코드 주석에도 "subject에 id 넣고 싶으면 DB 조회"라고 적혀있음)
         */
        String role = String.valueOf(claims.get("role"));
        String newAccess = jwtUtil.createToken(email /* or memberId */, role);

        Map<String, Object> body = new HashMap<>();
        body.put("token", newAccess);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    /**
     * ✅ 로그아웃
     *
     * 현재 구현의 의미:
     * - Redis에서 refreshToken을 삭제 → 재발급 불가
     * - BUT 이미 발급된 AccessToken은 만료 전까지는 원칙적으로 계속 유효함(JWT 특성)
     *
     * 그래서 로그아웃을 "완벽하게" 하려면 선택지가 있음:
     * 1) AccessToken 만료를 아주 짧게(예: 5~10분) 가져가고 RT 재발급만 막아도 실무적으로 충분
     * 2) AccessToken 블랙리스트를 Redis에 저장(남은 만료시간 TTL)해서 즉시 차단
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenRefreshDTO dto) {

        if (dto == null || dto.getRefreshToken() == null || dto.getRefreshToken().isBlank()) {
            return new ResponseEntity<>("refreshToken is required", HttpStatus.BAD_REQUEST);
        }

        try {
            // (1) refreshToken 검증 + subject(email) 추출
            Claims claims = jwtUtil.parseRefreshClaims(dto.getRefreshToken());
            String subject = claims.getSubject();

            // (2) Redis에서 해당 사용자 RT 삭제 → 이후 refresh 불가(=로그아웃)
            redisTemplate.delete(subject);

        } catch (JwtException e) {
            /**
             * 토큰이 깨졌다면 서버에 저장된 RT 키(subject)를 특정할 수 없음
             *
             * 실무에서는 로그아웃을 "멱등(idempotent)"하게 처리하기도 함:
             * - 토큰이 깨져도 "어차피 로그아웃 시도"니까 200 OK 주고 끝
             *
             * 현재 코드는 명확하게 401을 반환
             */
            return new ResponseEntity<>("invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(Map.of("message", "logged out"), HttpStatus.OK);
    }
}