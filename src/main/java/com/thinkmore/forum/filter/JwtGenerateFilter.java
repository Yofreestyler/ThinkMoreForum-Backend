package com.thinkmore.forum.filter;

import com.thinkmore.forum.entity.JwtUser;
import com.thinkmore.forum.configuration.Config;
import com.thinkmore.forum.service.UsersService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RequiredArgsConstructor
public class JwtGenerateFilter extends UsernamePasswordAuthenticationFilter {
    private final UsersService usersService;
    private final AuthenticationManager authenticationManager;
    private final SecretKey secretKey;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        //check username and password
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        if (username == null) {
            username = "";
        }
        if (password == null) {
            password = "";
        }
        username = username.trim();
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authentication);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) {

        //update last login timestamp
        usersService.updateLastLoginTimestamp(authResult.getName());

        //generate jwt
        JwtUser jwtUser = (JwtUser) authResult.getPrincipal();

        String jwtToken = Jwts.builder()
                .setId(jwtUser.getId() + "")
                .setSubject(jwtUser.getRoleName())
                .setAudience(jwtUser.getPermission())
                .setIssuedAt(new Date())
                .setExpiration(java.sql.Date.valueOf(Config.ExpireTime))
                .signWith(secretKey)
                .compact();

        response.addHeader(HttpHeaders.AUTHORIZATION, Config.JwtPrefix + jwtToken);
    }
}
