package RoadReport;

import RoadReport.security.service.JwtService;
import RoadReport.services.map.GraphHopperService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"brevo.api.key=mock-api-key-for-testing",
		"app.frontend.url=http://localhost:3000",
		"sender=shuberta@gmail.com"
})
class RoadReportApplicationTests {

	@MockitoBean
	private JwtService jwtService;
	@MockitoBean
	private GraphHopperService graphHopperService;

	@Test
	void contextLoads() {
	}

}
