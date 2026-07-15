package com.app.mono.customers.security;// MyUserDetailsService.java



import com.app.mono.customers.entity.Customer;
import com.app.mono.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

/**
 * ✅ Spring Security가 "사용자 정보(UserDetails)"를 필요로 할 때 호출하는 서비스
 *
 * 이 클래스가 호출되는 대표적인 2가지 상황
 *
 * 1) ✅ 로그인 시 (Form Login / API Login)
 *    AuthenticationManager.authenticate(...) 과정에서 내부적으로 호출됨
 *    - 입력 받은 email을 기준으로 사용자 조회
 *    - 저장된 암호화 비밀번호(BCrypt 해시)를 꺼냄
 *    - PasswordEncoder.matches(...)로 비밀번호 검증을 수행
 *
 * 2) ✅ JWT 요청 인증 시 (AccessToken 검증 이후)
 *    JwtFilter에서 토큰을 검증하고 subject(email)을 얻은 뒤,
 *    userDetailsService.loadUserByUsername(email)을 호출해서
 *    "권한(authorities)"을 구성한다.
 *
 * 🔥 즉, 이 클래스는
 * - 로그인 시: "비밀번호 검증에 필요한 사용자 정보 제공"
 * - 토큰 인증 시: "인가 판단(ROLE)에 필요한 권한 정보 제공"
 * 역할을 한다.
 *
 * ✅ 장점(보안 관점)
 * - 토큰 내부의 role 클레임을 100% 신뢰하지 않고,
 *   DB에서 최신 role을 읽어 authorities를 구성하는 구조가 가능해짐.
 * - 사용자의 role 변경이 즉시(다음 요청부터) 반영될 수 있음.
 */
@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    /**
     * 사용자 조회 Repository
     *
     * - loadUserByUsername(email) 호출 시마다 DB 조회가 발생할 수 있음
     * - 트래픽이 커지면 캐싱(예: Redis/Local cache)을 고려할 수 있음
     *   단, 캐싱하면 role 변경 반영이 느려질 수 있으니 정책에 맞게 선택
     */
    private final CustomerRepository customerRepository;

    /**
     * Spring Security가 정한 표준 메서드
     *
     * 메서드명은 username 이지만,
     * 실제로 어떤 값을 username으로 쓸지는 시스템 설계에 따라 결정됨.
     *
     * 현재 구현에서는:
     * - username = email
     *
     * 🔥 JWT와 연결:
     * - JwtUtil.createToken()에서 subject(email)를 넣었고
     * - JwtFilter에서 subject를 읽어서 여기로 전달한다.
     *
     * @param email 사용자 식별자(여기서는 이메일)
     * @return Spring Security가 이해하는 UserDetails(=CustomUserDetails)
     * @throws UsernameNotFoundException 사용자를 못 찾으면 인증 실패로 처리됨
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        /**
         * (1) DB에서 사용자 조회
         *
         * - 없으면 UsernameNotFoundException 던짐
         * - 이 예외는 Spring Security 인증 흐름에서 "인증 실패"로 처리된다.
         *
         * 로그인 시:
         * - 입력 email이 없으면 "아이디 없음"으로 실패
         *
         * JWT 인증 시:
         * - 토큰은 유효한데 DB에서 사용자가 삭제된 경우
         *   → 이 시점에서 인증을 실패시키는 효과가 있음(보안적으로 합리적)
         */
        Customer m = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("not found"));

        /**
         * (2) Member 엔티티 → UserDetails로 변환
         *
         * CustomUserDetails 생성자에서:
         * - username = m.getEmail()
         * - password = m.getPassword()  (BCrypt 해시값)
         * - role = m.getRole().name()
         * - authorities = ROLE_{role} 로 구성됨
         *
         * ✅ 로그인 시에는 password가 필요
         * ✅ JWT 인증 시에는 password가 보통 직접 쓰이지 않지만,
         *    UserDetails 계약상 포함되어도 무방
         */
        return new CustomUserDetails(
                m.getEmail(),
                m.getPassword(),
                m.getRole().name(),
                "서울" // 실습용: 추가 사용자 정보 예시
        );
    }
}