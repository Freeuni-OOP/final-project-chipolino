package RoadReport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RoadReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoadReportApplication.class, args);
	}

}
