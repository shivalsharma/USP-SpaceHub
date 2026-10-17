package fj.ac.usp.spacehub.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@RequiredArgsConstructor
public class TemporaryPasswordWebConfig
        implements WebMvcConfigurer {


    private final TemporaryPasswordInterceptor
            temporaryPasswordInterceptor;



    /* =========================================================
       REGISTER TEMPORARY PASSWORD INTERCEPTOR
       ========================================================= */

    @Override
    public void addInterceptors(
            InterceptorRegistry registry
    ) {


        registry
                .addInterceptor(
                        temporaryPasswordInterceptor
                )

                .addPathPatterns(
                        "/**"
                )

                .excludePathPatterns(

                        /* LOGIN */
                        "/login",

                        /* ERROR PAGE */
                        "/error",

                        /* STATIC CSS */
                        "/css/**",

                        /* STATIC JAVASCRIPT */
                        "/js/**",

                        /* STATIC IMAGES */
                        "/images/**",

                        /* PROFILE PICTURES */
                        "/uploads/**",

                        /* COMMON BROWSER REQUEST */
                        "/favicon.ico"
                );

    }

}