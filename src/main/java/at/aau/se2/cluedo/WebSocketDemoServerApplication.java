package at.aau.se2.cluedo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class WebSocketDemoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebSocketDemoServerApplication.class, args);
    }

}
