package com.example.jwtauth.authtokenfilter;

import com.example.jwtauth.jwtutil.JwtUtil;
import com.example.jwtauth.userdetails.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;

@Component //Spring terefinden avtomatik idare olunur
public class AuthTokenFilter extends OncePerRequestFilter {
    //OncePerRequestFilter yeni yalniz her sorguda bir defe isleyir ve bir jwt tokeni qaytarir
    //Her request etdiyimizde Authorizationde tokeni kontrol eliyir.
    @Autowired
    private JwtUtil jwtUtils;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain //Burada biz Authorization başlığında token var ya yox, onu tapırıq və doğrulayırıq.
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request); //Request-in Authorization başlığını oxuyur → "Bearer eyJhbGciOiJIUzI1NiIsInR.Bearer sözünü çıxarır, təkcə tokeni saxlayır.
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) { //Əgər token mövcuddursa və jwtUtils ilə doğrulanırsa (etibarlıdırsa): jwtUtils.getUsernameFromToken(jwt) → Tokenin içindən istifadəçi adını çıxarırıq.
                String username = jwtUtils.getUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username); //Verilən istifadəçi adı ilə istifadəçinin detalları databasedən gətirilir.
                UsernamePasswordAuthenticationToken authentication = //UsernamePasswordAuthenticationToken ilə bir Authentication obyekti yaradılır:
                        new UsernamePasswordAuthenticationToken(
                                userDetails,//Kim olduğunu bildirir.
                                null, // Şifrə daxil edilmir, çünki biz tokenlə login edirik.
                                userDetails.getAuthorities() //userDetails.getAuthorities(): İstifadəçinin rolları (ROLE_USER, ROLE_ADMIN və s.)
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // İstifadəçini hazırda sistemə daxil olmuş authenticated user kimi qeyd edirik.
                SecurityContextHolder.getContext().setAuthentication(authentication); // Artıq bu istifadəçi sistemdə login olmuş kimi davranacaq və müdafiə olunan endpoint-lərə girə biləcək.
            }
        } catch (Exception e) {
            System.out.println("Cannot set user authentication: " + e); //Əgər hansısa problem olsa (token düzgün deyilsə, istifadəçi tapılmırsa və s.) → sadəcə konsola yazdırılır.
        }
        filterChain.doFilter(request, response); //Bu filter işini bitirdikdən sonra növbəti filterə (və ya controllerə) keçməyə icazə verir.
    }
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization"); //Authorization: Bearer eyJhb... formasında gələn JWT tokenin yalnız əsas hissəsini çıxarır (Bearer olmadan).
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null; //Əgər token yoxdursa və ya düzgün formatda deyilsə → null qaytarır.
    }
}