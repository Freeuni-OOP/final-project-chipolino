package RoadReport.TestServices.TestCore;

import RoadReport.enums.ReportType;
import RoadReport.exceptions.special.BadRequestException;
import RoadReport.services.core.ReportAttributesValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestReportAttributesValidator {

    private ReportAttributesValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ReportAttributesValidator();
    }

    @Test
    void testNullAttributesReturnsNull() {
        assertNull(validator.validateAndSanitize(ReportType.SPEED_CAMERA, null));
    }

    @Test
    void testEmptyAttributesReturnsNull() {
        assertNull(validator.validateAndSanitize(ReportType.SPEED_CAMERA, Map.of()));
    }

    @Test
    void testTooManyAttributesThrows() {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("a", "1");
        raw.put("b", "2");
        raw.put("c", "3");
        raw.put("d", "4");
        raw.put("e", "5");
        raw.put("f", "6");

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.SPEED_CAMERA, raw));
    }

    @Test
    void testUnknownAttributeKeyThrows() {
        Map<String, Object> raw = Map.of("notARealAttribute", "value");

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.SPEED_CAMERA, raw));
    }

    @Test
    void testPotholeSeverityValid() {
        Map<String, Object> raw = Map.of("severity", "MODERATE");

        Map<String, Object> result = validator.validateAndSanitize(ReportType.POTHOLE, raw);

        assertEquals("MODERATE", result.get("severity"));
    }

    @Test
    void testSpeedCameraValidAttributes() {
        Map<String, Object> raw = Map.of(
                "speedLimit", 90,
                "direction", "northbound"
        );

        Map<String, Object> result = validator.validateAndSanitize(ReportType.SPEED_CAMERA, raw);

        assertNotNull(result);
        assertEquals(90, result.get("speedLimit"));
        assertEquals("northbound", result.get("direction"));
    }

    @Test
    void testSpeedLimitBelowRangeThrows() {
        Map<String, Object> raw = Map.of("speedLimit", 0);

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.SPEED_CAMERA, raw));
    }

    @Test
    void testSpeedLimitAboveRangeThrows() {
        Map<String, Object> raw = Map.of("speedLimit", 301);

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.SPEED_CAMERA, raw));
    }

    @Test
    void testSpeedLimitBoundaryValuesAreValid() {
        Map<String, Object> lower = Map.of("speedLimit", 1);
        Map<String, Object> upper = Map.of("speedLimit", 300);

        assertEquals(1, validator.validateAndSanitize(ReportType.SPEED_CAMERA, lower).get("speedLimit"));
        assertEquals(300, validator.validateAndSanitize(ReportType.SPEED_CAMERA, upper).get("speedLimit"));
    }

    @Test
    void testSpeedLimitWrongTypeThrows() {
        Map<String, Object> raw = Map.of("speedLimit", "fast");

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.SPEED_CAMERA, raw));
    }

    @Test
    void testDirectionWrongTypeThrows() {
        Map<String, Object> raw = Map.of("direction", 123);

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.SPEED_CAMERA, raw));
    }

    @Test
    void testPoliceVisibleBooleanValid() {
        Map<String, Object> raw = Map.of("visible", true);

        Map<String, Object> result = validator.validateAndSanitize(ReportType.POLICE, raw);

        assertEquals(true, result.get("visible"));
    }

    @Test
    void testPoliceVisibleWrongTypeThrows() {
        Map<String, Object> raw = Map.of("visible", "yes");

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.POLICE, raw));
    }

    @Test
    void testAccidentValidAttributes() {
        Map<String, Object> raw = Map.of(
                "severity", "SEVERE",
                "lanesBlocked", 2
        );

        Map<String, Object> result = validator.validateAndSanitize(ReportType.ACCIDENT, raw);

        assertEquals("SEVERE", result.get("severity"));
        assertEquals(2, result.get("lanesBlocked"));
    }

    @Test
    void testAccidentSeverityIsCaseInsensitiveButStoredAsCleaned() {
        Map<String, Object> raw = Map.of("severity", "minor");

        Map<String, Object> result = validator.validateAndSanitize(ReportType.ACCIDENT, raw);

        assertEquals("minor", result.get("severity"));
    }

    @Test
    void testAccidentInvalidSeverityThrows() {
        Map<String, Object> raw = Map.of("severity", "CATASTROPHIC");

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.ACCIDENT, raw));
    }

    @Test
    void testAccidentLanesBlockedOutOfRangeThrows() {
        Map<String, Object> tooLow = Map.of("lanesBlocked", -1);
        Map<String, Object> tooHigh = Map.of("lanesBlocked", 21);

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.ACCIDENT, tooLow));
        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.ACCIDENT, tooHigh));
    }

    @Test
    void testAccidentLanesBlockedBoundaryValuesAreValid() {
        Map<String, Object> lower = Map.of("lanesBlocked", 0);
        Map<String, Object> upper = Map.of("lanesBlocked", 20);

        assertEquals(0, validator.validateAndSanitize(ReportType.ACCIDENT, lower).get("lanesBlocked"));
        assertEquals(20, validator.validateAndSanitize(ReportType.ACCIDENT, upper).get("lanesBlocked"));
    }

    @Test
    void testRoadClosureValidAttributes() {
        Map<String, Object> raw = Map.of(
                "reason", "Water main repair",
                "detourAvailable", true
        );

        Map<String, Object> result = validator.validateAndSanitize(ReportType.ROAD_CLOSURE, raw);

        assertEquals("Water main repair", result.get("reason"));
        assertEquals(true, result.get("detourAvailable"));
    }

    @Test
    void testCustomLabelValid() {
        Map<String, Object> raw = Map.of("label", "Fallen tree");

        Map<String, Object> result = validator.validateAndSanitize(ReportType.CUSTOM, raw);

        assertEquals("Fallen tree", result.get("label"));
    }

    @Test
    void testStringAttributeIsSanitizedOfHtml() {
        Map<String, Object> raw = Map.of("label", "<script>alert('x')</script>Fallen tree");

        Map<String, Object> result = validator.validateAndSanitize(ReportType.CUSTOM, raw);

        assertEquals("Fallen tree", result.get("label"));
    }

    @Test
    void testStringAttributeEmptyAfterSanitizeThrows() {
        Map<String, Object> raw = Map.of("label", "<script>alert('x')</script>");

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.CUSTOM, raw));
    }

    @Test
    void testStringAttributeTooLongThrows() {
        String longLabel = "a".repeat(151);
        Map<String, Object> raw = Map.of("label", longLabel);

        assertThrows(BadRequestException.class,
                () -> validator.validateAndSanitize(ReportType.CUSTOM, raw));
    }

    @Test
    void testStringAttributeAtMaxLengthIsValid() {
        String maxLabel = "a".repeat(150);
        Map<String, Object> raw = Map.of("label", maxLabel);

        Map<String, Object> result = validator.validateAndSanitize(ReportType.CUSTOM, raw);

        assertEquals(maxLabel, result.get("label"));
    }

    @Test
    void testNullValueForKeyIsSkipped() {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("label", null);

        Map<String, Object> result = validator.validateAndSanitize(ReportType.CUSTOM, raw);

        assertNull(result);
    }
}