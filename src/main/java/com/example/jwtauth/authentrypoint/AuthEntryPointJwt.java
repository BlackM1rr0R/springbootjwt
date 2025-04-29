package com.example.jwtauth.authentrypoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component //Spring bean olarag yaranir ve spring project basliyanda bunu avtomatik taniyir ve idare edir
public class AuthEntryPointJwt  implements AuthenticationEntryPoint {
    //401 xetasi vermeyi ucundur.Eger token islemirse 401 xetasi burada gelir
    @Override
    public void commence(
            HttpServletRequest request, //Hansi request atilib IP,endpoit ve s.
            HttpServletResponse response, //Bunu biz deyirik burda
            AuthenticationException authException //Xeta melumati burda olur.
    ) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized"); //Burada 401 gonderiririk
    }
}