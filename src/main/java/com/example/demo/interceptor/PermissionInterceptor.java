package com.example.demo.interceptor;

import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.service.PermissionCacheService;
import com.example.demo.domain.permission.valueobject.ActionType;
import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.security.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;

/**
 * Permission interceptor for API-level permission verification
 * Uses Ant path pattern matching for resource resolution
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionCacheService permissionCache;
    private final ResourceRepository resourceRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            // Unauthenticated requests are handled by AuthInterceptor
            return true;
        }

        PermissionBitmap bitmap = permissionCache.getPermissionBitmap(userId);

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        Long resourceId = matchResource(requestPath, method);
        if (resourceId == null) {
            // Unconfigured resources are allowed (fail-open for backward compatibility)
            log.debug("No matching resource for path: {} {}", method, requestPath);
            return true;
        }

        ActionType action = determineAction(method, requestPath);
        if (!bitmap.hasAction(resourceId, action)) {
            log.warn("Permission denied: userId={}, resourceId={}, action={}, path={}",
                    userId, resourceId, action, requestPath);
            sendError(response, 403, "无访问权限");
            return false;
        }

        log.debug("Permission granted: userId={}, resourceId={}, action={}, path={}",
                userId, resourceId, action, requestPath);
        return true;
    }

    /**
     * Ant path pattern matching
     */
    private Long matchResource(String path, String method) {
        List<Resource> apis = resourceRepository.findAllApis();

        for (Resource api : apis) {
            if (api.getPathPattern() != null &&
                pathMatcher.match(api.getPathPattern(), path) &&
                api.getMethod() != null &&
                api.getMethod().equalsIgnoreCase(method)) {
                return api.getId();
            }
        }
        return null;
    }

    /**
     * Determine action type based on HTTP method and path
     */
    private ActionType determineAction(String method, String path) {
        return switch (method.toUpperCase()) {
            case "GET" -> ActionType.VIEW;
            case "POST" -> ActionType.CREATE;
            case "PUT", "PATCH" -> ActionType.UPDATE;
            case "DELETE" -> ActionType.DELETE;
            default -> ActionType.EXECUTE;
        };
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
            Map.of("code", code, "message", message, "data", null)
        ));
    }
}