package de.doubleslash.spring.introduction.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "handler")
@ConfigurationPropertiesScan
@Getter
@Setter
public class FileHandlerConfiguration {
    private String endpoint;
    private String username;
    private String password;
}
