package RoadReport;

import RoadReport.services.map.GraphHopperService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class RoadReportApplicationTests {

	@MockitoBean
	private GraphHopperService graphHopperService;

	@Test
	void contextLoads() {
	}

}
