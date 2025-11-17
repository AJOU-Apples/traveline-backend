package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.repository.UserRepository;
import org.apples.travelinebackend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider tokenProvider;
        private final AuthenticationManager authenticationManager;

        /**
         * 회원가입 (자동 로그인)
         */
        @Transactional
        public LoginResponse register(RegisterRequest request) {
                log.info("회원가입 시도: email={}", request.getEmail());

                // 이메일 중복 체크
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new BadRequestException("이미 사용 중인 이메일입니다.");
                }

                // User 생성
                User user = User.builder()
                                .email(request.getEmail())
                                .name(request.getName())
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .isActive(true)
                                .isVerified(false)
                                .build();

                User savedUser = userRepository.save(user);
                log.info("회원가입 완료: userId={}", savedUser.getId());

                // 자동 로그인 - JWT 토큰 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                savedUser, null, savedUser.getAuthorities());
                String accessToken = tokenProvider.generateAccessToken(authentication);
                String refreshToken = tokenProvider.generateRefreshToken(savedUser.getEmail());

                // lastLoginAt 업데이트
                savedUser.setLastLoginAt(LocalDateTime.now());
                userRepository.save(savedUser);

                log.info("회원가입 후 자동 로그인 완료: userId={}", savedUser.getId());

                return LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .user(mapToAuthUserDto(savedUser))
                                .build();
        }

        /**
         * 로그인 - 이메일/패스워드로 사용자 검증
         */
        @Transactional
        public LoginResponse login(LoginRequest request) {
                String email = request.getEmail();
                log.info("로그인 시도: email={}", email);

                // 이메일/패스워드로 인증
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                email,
                                                request.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // JWT 토큰 생성
                String accessToken = tokenProvider.generateAccessToken(authentication);
                String refreshToken = tokenProvider.generateRefreshToken(email);

                // lastLoginAt 업데이트
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BadRequestException("사용자를 찾을 수 없습니다."));
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);

                log.info("로그인 완료: userId={}", user.getId());

                return LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .user(mapToAuthUserDto(user))
                                .build();
        }

        /**
         * 토큰 갱신
         */
        public LoginResponse refreshToken(RefreshTokenRequest request) {
                String refreshToken = request.getRefreshToken();

                // Refresh Token 검증
                if (!tokenProvider.validateToken(refreshToken)) {
                        throw new BadRequestException("유효하지 않은 Refresh Token입니다.");
                }

                // 이메일 추출
                String email = tokenProvider.getEmailFromToken(refreshToken);

                // 사용자 조회
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BadRequestException("사용자를 찾을 수 없습니다."));

                // 새로운 Access Token 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities());
                String newAccessToken = tokenProvider.generateAccessToken(authentication);

                log.info("토큰 갱신 완료: userId={}", user.getId());

                return LoginResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(refreshToken)
                                .user(mapToAuthUserDto(user))
                                .build();
        }

        /**
         * User -> AuthUserDto 변환 (인증 응답용)
         */
        private AuthUserDto mapToAuthUserDto(User user) {
                return AuthUserDto.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .name(user.getName())
                                .username(user.getNickname()) // 닉네임 필드
                                .profileImageUrl(user.getProfileImage())
                                .build();
        }

        /**
         * User -> UserDto 변환
         */
        private UserDto mapToUserDto(User user) {
                return UserDto.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .name(user.getName())
                                .username(user.getNickname()) // 닉네임 필드
                                .profileImage(user.getProfileImage())
                                .bio(user.getBio())
                                .isActive(user.getIsActive())
                                .isVerified(user.getIsVerified())
                                .createdAt(user.getCreatedAt())
                                .lastLoginAt(user.getLastLoginAt())
                                .build();
        }
}
