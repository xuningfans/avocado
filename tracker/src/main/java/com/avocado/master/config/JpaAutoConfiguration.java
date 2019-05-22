package com.avocado.master.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JpaConfig class
 *
 * @author xuning
 * @date 2019-05-21 20:50
 */

@Configuration
@EnableJpaAuditing//(auditorAwareRef = "auditorProvider")
public class JpaAutoConfiguration {

//    @Bean
//    public AuditorAware<String> auditorProvider() {
//
//        /*
//          if you are using spring security, you can get the currently logged username with following code segment.
//          SecurityContextHolder.getContext().getAuthentication().getName()
//         */
//        return () -> Optional.of("9999999");
//
//    }

}
