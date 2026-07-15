package com.app.mono.customers.security;// CustomUserDetails.java

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

/**
 * ✅ Spring Security에서 사용하는 "인증된 사용자 객체"
 *
 * 이 클래스는 JWT 검증이 끝난 뒤,
 * SecurityContextHolder 안에 저장되는 principal(사용자 정보)의 실제 구현체이다.
 *
 * 🔥 전체 인증 흐름에서의 위치
 *
 * 1) 로그인 성공
 *    → JWT 발급
 *
 * 2) 이후 요청에서 JwtFilter가 AccessToken 검증
 *
 * 3) JwtFilter에서:
 *      username(email) 추출
 *      ↓
 *      userDetailsService.loadUserByUsername(username)
 *      ↓
 *      CustomUserDetails 객체 생성
 *
 * 4) 이 객체가 Authentication 안에 들어가고,
 *    SecurityContextHolder에 저장됨
 *
 * 5) Controller에서 @AuthenticationPrincipal 로 꺼내 사용
 *
 * 즉, 이 클래스는 "토큰 인증이 끝난 사용자 정보"를 담는 그릇이다.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    /**
     * =========================
     * 🔐 UserDetails 필수 구현 필드
     * =========================
     */

    /**
     * username
     *
     * ⚠️ username은 꼭 "아이디"일 필요는 없다.
     * 현재 프로젝트에서는 email을 username으로 사용하는 구조일 가능성이 큼.
     *
     * JwtUtil에서 subject(email)를 넣었고,
     * JwtFilter에서 subject를 username으로 사용 중이므로
     * 여기 username은 실제로는 email 역할을 한다.
     */
    private String username;

    /**
     * password
     *
     * 로그인 시 AuthenticationManager가
     * PasswordEncoder.matches(raw, encoded) 비교할 때 사용됨.
     *
     * ⚠️ JWT 인증 단계에서는 비밀번호는 사용되지 않는다.
     * - AccessToken 검증 후에는 password는 null로 둬도 되지만
     * - 로그인 과정에서는 반드시 필요.
     */
    private String password;

    /**
     * authorities (권한 목록)
     *
     * Spring Security에서 "인가(Authorization)" 판단의 핵심.
     *
     * 예:
     * - @PreAuthorize("hasRole('ADMIN')")
     * - .requestMatchers("/admin/**").hasRole("ADMIN")
     *
     * 이 authorities 안에 ROLE_ADMIN 이 있어야 통과한다.
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * =========================
     * 🔎 추가 사용자 정보 필드
     * =========================
     */

    /**
     * role
     *
     * JWT에 넣었던 role과 동일 개념.
     *
     * ⚠️ 주의:
     * - 실제 인가 판단은 authorities를 사용한다.
     * - role 필드는 단순 보관용.
     */
    private String role;

    /**
     * address (실습용 필드)
     *
     * CustomUserDetails는
     * 단순 인증용 정보뿐 아니라,
     * 추가 사용자 정보도 담을 수 있다는 예시.
     *
     * ⚠️ 하지만 운영에서는:
     * - 민감 정보(주민번호 등)는 여기 담지 않는 게 좋다.
     * - 토큰 기반 인증에서 principal은 메모리에 유지되므로 최소 정보만 보관 권장.
     */
    private String address;


    /**
     * 생성자
     *
     * JwtFilter → UserDetailsService → 여기 생성자로 이어지는 구조.
     *
     * 🔥 중요한 부분: ROLE_ 접두사
     *
     * Spring Security는 내부적으로
     * "hasRole('ADMIN')" → "ROLE_ADMIN" 과 비교한다.
     *
     * 그래서 반드시 "ROLE_" 접두사를 붙여야 한다.
     *
     * 만약 안 붙이면:
     * - hasRole("ADMIN")는 실패
     * - hasAuthority("ADMIN")는 통과 (하지만 권장 패턴은 ROLE_)
     */
    public CustomUserDetails(String username, String password, String role, String address) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;

        /**
         * 권한 목록 생성
         *
         * 예:
         * role = "USER"
         * → authorities = [ "ROLE_USER" ]
         *
         * 이후:
         * @PreAuthorize("hasRole('USER')") 통과
         */
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    /**
     * =========================
     * 🔧 UserDetails 기본 메서드 구현
     * =========================
     *
     * 아래 메서드들은 계정 상태 관리용이다.
     * (현재는 기본값 true로 두는 것이 일반적)
     */

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠김 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부
    }
}