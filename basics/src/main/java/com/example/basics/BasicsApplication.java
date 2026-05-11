package com.example.basics;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;

@SpringBootApplication
public class BasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicsApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(DogRepository repository) {
        return _ -> IO.println(repository.findAll());
    }

    @Bean
    Customizer<HttpSecurity> httpSecurityCustomizer() {
        return http -> http
                .webAuthn(a -> a.rpId("localhost")
                        .rpName("bootiful")
                        .allowedOrigins("http://localhost:8080")
                )
                .oneTimeTokenLogin(httpSecurityOneTimeTokenLoginConfigurer -> httpSecurityOneTimeTokenLoginConfigurer
                        .tokenGenerationSuccessHandler((_, response, oneTimeToken) -> {
                            response.getWriter().println("you've got console mail!");
                            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                            IO.println("please go to http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue());
                        }));
    }

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        var u = new JdbcUserDetailsManager(dataSource);
        u.setEnableUpdatePassword(true);
        return u;
    }
}

@Controller
@ResponseBody
class MeController {

    @GetMapping("/")
    Map<String, String> me(Principal principal) {
        return Map.of("name", principal.getName());
    }
}

// rsocket
// grpc
// websockets
// html (htmx, turbo, etc.)
// rest
// graphql

@Controller
class DogsGraphqlController {

    private final DogRepository repository;

    DogsGraphqlController(DogRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    Collection<Dog> dogsByName(@Argument String name) {
        return repository.findByName(name);
    }

    @QueryMapping
    Collection<Dog> dogs() {
        return repository.findAll();
    }
//    @MutationMapping
//    @SubscriptionMapping

}


@Controller
@ResponseBody
class DogsController {

    private final DogRepository repository;

    DogsController(DogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/dogs")
    Collection<Dog> dogs() {
        return repository.findAll();
    }
}

interface DogRepository extends ListCrudRepository<Dog, String> {

    Collection<Dog> findByName(String name);

}

record Dog(@Id Integer id, String name, String description) {
}