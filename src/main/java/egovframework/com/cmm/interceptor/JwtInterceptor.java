package egovframework.com.cmm.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

import egovframework.com.cmm.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;

public class JwtInterceptor implements HandlerInterceptor{
	
	/**
	 * globals.properties에서 로그인 URL 조회
	 */
	private String getLoginUrlFromProperties() {
        Properties props = new Properties();    
        String propPath = "egovframework/egovProps/globals.properties";
        
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(propPath)) {
            if (is == null) {
                System.err.println("[Error] Cannot find property file: " + propPath);
                return null;
            }
            props.load(is);
            return props.getProperty("next.login.url");
        } catch (Exception e) {
            System.err.println("[Error] Exception occurred while loading properties: " + e.getMessage());
            return null;
        }
    }
	
	/**
	 * AJAX 요청인지 판별
	 * X-Requested-With: XMLHttpRequest 또는 Accept: application/json 헤더 확인
	 */
	private boolean isAjaxRequest(HttpServletRequest request) {
		String requestedWith = request.getHeader("X-Requested-With");
		String accept = request.getHeader("Accept");
		
		boolean isXmlHttpRequest = "XMLHttpRequest".equalsIgnoreCase(requestedWith);
		boolean isJsonAccept = accept != null && accept.contains("application/json");
		
		return isXmlHttpRequest || isJsonAccept;
	}
	
	/**
	 * JSON 문자열에서 특수 문자 이스케이프 처리
	 */
	private String escapeJsonString(String str) {
		if (str == null) {
			return "null";
		}
		return str.replace("\\", "\\\\")
		          .replace("\"", "\\\"")
		          .replace("\n", "\\n")
		          .replace("\r", "\\r")
		          .replace("\t", "\\t");
	}
	
	/**
	 * AJAX 요청에 대한 JSON 에러 응답
	 * Jackson 의존성 없이 직접 JSON 문자열 생성
	 */
	private void sendJsonErrorResponse(HttpServletResponse response, String code, String message, int statusCode) throws IOException {
		response.setStatus(statusCode);
		response.setContentType("application/json;charset=UTF-8");
		
		PrintWriter out = response.getWriter();
		try {
			// JSON 문자열 직접 생성 (Jackson 의존성 없이)
			String jsonResponse = String.format(
				"{\"status\":\"error\",\"code\":\"%s\",\"message\":\"%s\"}",
				escapeJsonString(code),
				escapeJsonString(message)
			);
			out.print(jsonResponse);
			out.flush();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * 일반 요청에 대한 리다이렉트 처리
	 */
	private void handleAuthenticationFailure(HttpServletResponse response) throws IOException {
		String loginUrl = getLoginUrlFromProperties();
		
		if(loginUrl == null || loginUrl.trim().isEmpty()) {
			System.err.println("[Critical Error] 'next.login.url'이 globals.properties에 설정되지 않았습니다.");
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "System configuration error: Login URL missing.");
			return;
		}
		
		System.out.println("[Auth Failure] Redirecting to: " + loginUrl);
		response.sendRedirect(loginUrl);
	}
	
	
	/**
	 * 컨트롤러 실행 전 JWT 토큰 검증
	 * false 반환 시 요청이 중단되고 사용자는 튕겨 나감
	 */
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. 요청(Cookie)에서 JWT 토큰 추출
        String token = JwtUtil.resolveToken(request);

        // 2. 토큰이 아예 없는 경우
        if (token == null || token.isEmpty()) {
            System.out.println("[Auth Failure] Token not found.");
            
            if (isAjaxRequest(request)) {
                sendJsonErrorResponse(response, "TOKEN_NOT_FOUND", "인증 토큰이 없습니다.", HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                handleAuthenticationFailure(response);
            }
            return false;
        }

        // 3. 토큰 검증 및 데이터(Claims) 추출
        try {
            Claims claims = JwtUtil.validateAndParseToken(token);
            
            // 4. 토큰 만료 시간 검증 (추가 안전장치)
            if (claims.getExpiration().before(new Date())) {
                System.out.println("[Auth Failure] Token has expired.");
                
                if (isAjaxRequest(request)) {
                    sendJsonErrorResponse(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다.", HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    handleAuthenticationFailure(response);
                }
                return false;
            }

            // 5. 인증 성공 (추후 권한 제어 시 사용하기 위해 request에 정보 저장)
            String role = claims.get("role", String.class);
            request.setAttribute("userRole", role);
            
            System.out.println("[Auth Success] Access Granted. User Role: " + role);
            return true; // 컨트롤러로 진입 허용
            
        } catch (ExpiredJwtException e) {
            // 토큰 만료 예외 처리
            System.out.println("[Auth Failure] Token expired: " + e.getMessage());
            
            if (isAjaxRequest(request)) {
                sendJsonErrorResponse(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다.", HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                handleAuthenticationFailure(response);
            }
            return false;
            
        } catch (MalformedJwtException e) {
            // 토큰 형식 오류
            System.out.println("[Auth Failure] Malformed token: " + e.getMessage());
            
            if (isAjaxRequest(request)) {
                sendJsonErrorResponse(response, "TOKEN_INVALID", "유효하지 않은 토큰 형식입니다.", HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                handleAuthenticationFailure(response);
            }
            return false;
            
        } catch (JwtException e) {
            // 토큰 서명 오류 또는 기타 JWT 관련 오류
            System.out.println("[Auth Failure] JWT error: " + e.getMessage());
            
            if (isAjaxRequest(request)) {
                sendJsonErrorResponse(response, "TOKEN_INVALID", "토큰이 유효하지 않습니다.", HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                handleAuthenticationFailure(response);
            }
            return false;
            
        } catch (IllegalArgumentException e) {
            // 토큰이 null이거나 빈 문자열
            System.out.println("[Auth Failure] Invalid token argument: " + e.getMessage());
            
            if (isAjaxRequest(request)) {
                sendJsonErrorResponse(response, "TOKEN_INVALID", "토큰이 제공되지 않았습니다.", HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                handleAuthenticationFailure(response);
            }
            return false;
            
        } catch (Exception e) {
            // 기타 예외
            System.err.println("[Auth Failure] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            
            if (isAjaxRequest(request)) {
                sendJsonErrorResponse(response, "AUTH_ERROR", "인증 처리 중 오류가 발생했습니다.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다.");
            }
            return false;
        }
    }
}
