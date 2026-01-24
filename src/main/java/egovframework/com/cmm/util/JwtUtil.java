package egovframework.com.cmm.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;

public class JwtUtil {

	private static String SECRET_KEY;
	
	// 성능 최적화: Key를 static final로 한 번만 생성하여 재사용
	private static final Key SIGNING_KEY;
	
	// 클래스가 로드될 때 설정 파일에서 키를 읽어옵니다.
	static {
		String propPath = "egovframework/egovProps/globals.properties";
		Key tempKey = null;
        
        try (InputStream input = JwtUtil.class.getClassLoader().getResourceAsStream(propPath)) {
            Properties prop = new Properties();
            if (input == null) {
                System.err.println("[Critical Error] Cannot find property file: " + propPath);
            } else {
                prop.load(input);
                SECRET_KEY = prop.getProperty("jwt.secret");
                
                if (SECRET_KEY == null || SECRET_KEY.trim().isEmpty()) {
                    System.err.println("[Critical Error] 'jwt.secret' is not configured in globals.properties.");
                } else {
                    System.out.println("[Init] JwtUtil: Secret key loaded successfully.");
                    // Key를 한 번만 생성하여 static final로 저장
                    tempKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        SIGNING_KEY = tempKey;
    }
	
	/**
     * 쿠키에서 토큰 추출
     */
    public static String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * 토큰 검증 및 파싱
     * 
     * @param token JWT 토큰 문자열
     * @return Claims 토큰에서 추출한 클레임 정보
     * @throws ExpiredJwtException 토큰이 만료된 경우
     * @throws MalformedJwtException 토큰 형식이 잘못된 경우
     * @throws JwtException 토큰 서명이 유효하지 않거나 기타 JWT 관련 오류
     * @throws IllegalArgumentException 토큰이 null이거나 빈 문자열인 경우
     */
    public static Claims validateAndParseToken(String token) 
            throws ExpiredJwtException, MalformedJwtException, JwtException, IllegalArgumentException {
        
        if (SIGNING_KEY == null) {
            throw new IllegalStateException("JWT signing key is not initialized. Check globals.properties configuration.");
        }
        
        // 성능 최적화: static final로 관리되는 Key 재사용
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
	
}
