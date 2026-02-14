package com.aivle.project.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * API 요청/응답 로깅을 위한 Aspect.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiLoggingAspect {

	private static final String MASKED_VALUE = "\"****\"";
	private static final String COOKIE_MASKED_VALUE = "\"[COOKIE_MASKED]\"";
	private static final Pattern JWT_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");
	private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)^Bearer\\s+.+$");
	private static final Pattern COOKIE_PAIR_PATTERN = Pattern.compile("(?i)\\b([a-z0-9_-]+)\\s*=\\s*[^;]+");
	private static final Pattern SENSITIVE_COOKIE_KEY_PATTERN = Pattern.compile("(?i).*(token|session|auth|jwt|csrf|cookie).*");
	private static final Pattern SENSITIVE_JSON_PATTERN = Pattern.compile(
		"\"(?i)([^\"\\\\]*(password|token|secret|credential|authorization|cookie|session|jwt|csrf|name|phone|ssn|creditcard)[^\"\\\\]*)\"\\s*:\\s*\"[^\"]*\""
	);

	private final ObjectMapper objectMapper;

	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	public void restController() {}

	@Around("restController()")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);

		long start = System.currentTimeMillis();
		try {
			Object result = joinPoint.proceed();
			long end = System.currentTimeMillis();

			log.info("API Request: [{} {}] | Method: {}.{} | Args: {} | Time: {}ms | RequestId: {}",
				request.getMethod(), request.getRequestURI(),
				joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
				getMaskedArgs(joinPoint, joinPoint.getArgs()), (end - start), requestId);

			return result;
		} catch (Throwable e) {
			long end = System.currentTimeMillis();
			log.error("API Error: [{} {}] | Method: {}.{} | Error: {} | Time: {}ms | RequestId: {}",
				request.getMethod(), request.getRequestURI(),
				joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
				e.getMessage(), (end - start), requestId);
			throw e;
		} finally {
			MDC.remove("requestId");
		}
	}

	private String getMaskedArgs(ProceedingJoinPoint joinPoint, Object[] args) {
		if (args == null || args.length == 0) {
			return "[]";
		}

		Method method = resolveMethod(joinPoint);
		return IntStream.range(0, args.length)
			.mapToObj(index -> mask(args[index], isCookieValueParameter(method, index)))
			.collect(Collectors.joining(", ", "[", "]"));
	}

	String maskArgsForLog(Method method, Object[] args) {
		if (args == null || args.length == 0) {
			return "[]";
		}
		return IntStream.range(0, args.length)
			.mapToObj(index -> mask(args[index], isCookieValueParameter(method, index)))
			.collect(Collectors.joining(", ", "[", "]"));
	}

	private Method resolveMethod(ProceedingJoinPoint joinPoint) {
		if (!(joinPoint.getSignature() instanceof MethodSignature methodSignature)) {
			return null;
		}
		return methodSignature.getMethod();
	}

	private boolean isCookieValueParameter(Method method, int parameterIndex) {
		if (method == null) {
			return false;
		}
		Parameter[] parameters = method.getParameters();
		if (parameterIndex < 0 || parameterIndex >= parameters.length) {
			return false;
		}
		return parameters[parameterIndex].isAnnotationPresent(CookieValue.class);
	}

	String mask(Object arg, boolean cookieValueParameter) {
		if (cookieValueParameter) {
			return COOKIE_MASKED_VALUE;
		}
		return mask(arg);
	}

	String mask(Object arg) {
		if (arg == null) return "null";

		// HTTP 관련 객체 처리
		if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
			return arg.getClass().getSimpleName();
		}

		// MultipartFile 처리 (파일 내용은 로깅하지 않음)
		if (arg instanceof org.springframework.web.multipart.MultipartFile file) {
			return String.format("MultipartFile(name=%s, size=%d)", file.getOriginalFilename(), file.getSize());
		}

		// Spring 에러 바인딩 처리
		if (arg instanceof org.springframework.validation.BindingResult) {
			return "BindingResult";
		}

		// InputStream/OutputStream 처리
		if (arg instanceof java.io.InputStream || arg instanceof java.io.OutputStream) {
			return arg.getClass().getSimpleName();
		}

		// String 타입 처리 (이메일, 비밀번호 등)
		if (arg instanceof String str) {
			return maskString(str);
		}

		// 숫자 타입 처리
		if (arg instanceof Number) {
			return arg.toString();
		}

		// JSON 객체 마스킹
		try {
			String json = objectMapper.writeValueAsString(arg);
			return maskJson(json);
		} catch (Exception e) {
			log.debug("Skip masking for complex object: {}. Reason: {}", arg.getClass().getName(), e.getMessage());
			return "[COMPLEX_OBJECT]";
		}
	}

	private String maskString(String str) {
		String normalized = str.trim();

		if (BEARER_PATTERN.matcher(normalized).matches()) {
			return "\"Bearer ****\"";
		}

		if (JWT_PATTERN.matcher(normalized).matches()) {
			return MASKED_VALUE;
		}

		if (containsSensitiveCookiePair(normalized)) {
			return COOKIE_MASKED_VALUE;
		}

		// 이메일 형식 마스킹
		if (normalized.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
			return "\"" + maskEmail(normalized) + "\"";
		}

		// 비밀번호 패턴 마스킹 (길이 8 이상, 특수문자 포함)
		if (normalized.length() > 8 && normalized.matches(".*[!@#$%^&*].*")) {
			return MASKED_VALUE;
		}

		// 일반 문자열은 JSON 문자열로 변환하여 반환
		try {
			return objectMapper.writeValueAsString(str);
		} catch (Exception e) {
			log.warn("Failed to convert string to JSON: {}", e.getMessage());
			return "\"[MASKED]\"";
		}
	}

	private String maskEmail(String email) {
		int atIndex = email.indexOf('@');
		if (atIndex > 2) {
			return email.substring(0, 2) + "***" + email.substring(atIndex);
		} else if (atIndex > 0) {
			return email.charAt(0) + "***" + email.substring(atIndex);
		}
		return "***@***";
	}

	private String maskJson(String json) {
		// 이메일 필드는 특별히 마스킹 (형식 유지)
		Pattern emailPattern = Pattern.compile("\"(?i)email\"\\s*:\\s*\"([^\"]+)\"");
		Matcher emailMatcher = emailPattern.matcher(json);
		StringBuffer sb = new StringBuffer();
		while (emailMatcher.find()) {
			String email = emailMatcher.group(1);
			emailMatcher.appendReplacement(sb, "\"email\":\"" + maskEmail(email) + "\"");
		}
		emailMatcher.appendTail(sb);
		json = sb.toString();

		// 대소문자 구분 없이 민감한 필드 마스킹
		Matcher sensitiveMatcher = SENSITIVE_JSON_PATTERN.matcher(json);
		StringBuffer result = new StringBuffer();
		while (sensitiveMatcher.find()) {
			String replacement = "\"" + sensitiveMatcher.group(1) + "\":\"****\"";
			sensitiveMatcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		sensitiveMatcher.appendTail(result);
		return result.toString();
	}

	private boolean containsSensitiveCookiePair(String value) {
		Matcher matcher = COOKIE_PAIR_PATTERN.matcher(value);
		while (matcher.find()) {
			String key = matcher.group(1);
			if (SENSITIVE_COOKIE_KEY_PATTERN.matcher(key).matches()) {
				return true;
			}
		}
		return false;
	}
}
