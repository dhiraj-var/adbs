package com.nepalidate.ADBS.spring;

import com.nepalidate.ADBS.NepaliDateConverter.NDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires {@link NDC} as a Spring bean. adbs-core has no Spring dependency, so
 * import this configuration (or component-scan this package) to get NDC
 * injectable in a Spring application.
 */
@Configuration
public class NdcConfiguration {

    @Bean
    public NDC ndc() {
        return new NDC();
    }
}
