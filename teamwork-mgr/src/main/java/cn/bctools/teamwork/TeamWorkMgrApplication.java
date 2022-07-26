package cn.bctools.teamwork;

import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Administrator
 */
@EnableJvsMgrResourceServer
@EnableDiscoveryClient
@SpringBootApplication
@EnableAsync
public class TeamWorkMgrApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamWorkMgrApplication.class, args);
    }

}
