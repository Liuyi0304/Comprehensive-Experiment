package com.example.labequipment.common.interceptor;

import com.example.labequipment.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {
    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String token = request.getHeader("Authorization");
        if (!StringUtils.hasLength(token)) {
            response.setStatus(401);
            return false;
        }

        Claims claims = jwtUtils.parseToken(token);
        if (claims == null) {
            response.setStatus(401);
            return false;
        }

        // 存入 request，方便 Controller 使用
        request.setAttribute("userId", claims.get("userId"));
        return true;
    }
}
