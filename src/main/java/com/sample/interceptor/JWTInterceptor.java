package com.sample.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.security.JwtSubject;
import com.sample.util.AuthUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.sample.security.SecurityManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JWTInterceptor implements HandlerInterceptor {

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Validates that the jwt token passed in matches  */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String jwtToken = request.getHeader("x-access-token");

        System.out.println(jwtToken);
        System.out.println("pre handle Request URL::" + request.getRequestURL().toString() + " | token: " + jwtToken);

        // validate the token here
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new ForbiddenAccessException();
        }
        String jwsSubject = null;
        try {
            jwsSubject = Jwts.parser().setSigningKey(securityManager.getSecurityKey()).parseClaimsJws(jwtToken).getBody().getSubject();
        }
        catch (MalformedJwtException mje) {
            throw new ForbiddenAccessException();
        }
        System.out.println("subject: " + jwsSubject);

        ObjectMapper objectMapper = new ObjectMapper();
        JwtSubject subject = objectMapper.readValue(jwsSubject, JwtSubject.class);

        if (!subject.isValid()) {
            throw new ForbiddenAccessException();
        }

        User user = userRepository.findOne(subject.getUserId());
        if (user == null) {
            throw new ForbiddenAccessException();
        }

        request.setAttribute(AuthUtils.JWT_TOKEN_NAME, subject);
        request.setAttribute(AuthUtils.LOGGED_IN_USER, user);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {
//        System.out.println("post handle Request URL::" + request.getRequestURL().toString());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
//        System.out.println("after completion Request URL::" + request.getRequestURL().toString());
    }

}
