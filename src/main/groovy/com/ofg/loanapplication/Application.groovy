package com.ofg.loanapplication
import com.ofg.infrastructure.environment.EnvironmentSetupVerifier
import com.ofg.infrastructure.web.correlationid.EnableCorrelationId
import groovy.transform.TypeChecked
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

import static com.ofg.config.BasicProfiles.*

@TypeChecked
@SpringBootApplication
@EnableCorrelationId
class Application {

    static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application)
        application.addListeners(new EnvironmentSetupVerifier([DEVELOPMENT, PRODUCTION, TEST]))
        application.run(args)
    }

}
