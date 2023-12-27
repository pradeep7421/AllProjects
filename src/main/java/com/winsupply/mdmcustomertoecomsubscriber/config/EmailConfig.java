package com.winsupply.mdmcustomertoecomsubscriber.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Email Configuration
 *
 * @author Ankit Jain
 *
 */
@Configuration
@Getter
@Setter
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${winsupply.environment}")
    private String environment;

    @Value("${winsupply.mail.batch.size}")
    private int batchSize;

    @Value("${winsupply.mail.time.delay}")
    private long timePeriod;

    @Value("${winsupply.mail.failure.threshold}")
    private long threshold;

    @Value("${winsupply.mail.failure.second.threshold}")
    private long secondThreshold;

    @Value("${winsupply.mail.failure.third.threshold}")
    private long thirdThreshold;

    @Value("${winsupply.mail.success.threshold}")
    private long successThreshold;

    @Value("#{'${winsupply.mail.tos}'.split(',')}")
    private List<String> mailTos;

    @Value("${winsupply.mail.from}")
    private String mailFrom;
}
