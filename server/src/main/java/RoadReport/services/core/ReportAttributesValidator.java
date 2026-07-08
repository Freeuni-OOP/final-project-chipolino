package RoadReport.services.core;

import RoadReport.enums.ReportType;
import RoadReport.exceptions.special.BadRequestException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Validates and sanitizes the free-form {@code attributes} map that can be attached
 * to a {@link RoadReport.entities.Report}.
 * <p>
 * Each {@link ReportType} exposes a small, explicit schema of the extra fields it
 * accepts (e.g. a speed limit for {@link ReportType#SPEED_CAMERA}, or a severity for
 * {@link ReportType#ACCIDENT}). This keeps the JSON column predictable, prevents
 * arbitrary/oversized payloads from being stored, and sanitizes string values the
 * same way {@link ReportService} already sanitizes the report description.
 */
@Component
public class ReportAttributesValidator {

    private static final int MAX_ATTRIBUTES = 5;
    private static final int MAX_STRING_LENGTH = 150;

    private static final Set<String> ALLOWED_SEVERITIES = Set.of("MINOR", "MODERATE", "SEVERE");

    private enum AttributeType { STRING, INTEGER, BOOLEAN }

    private record AttributeSpec(AttributeType type) {}

    private static final Map<ReportType, Map<String, AttributeSpec>> SCHEMAS = Map.of(
            ReportType.SPEED_CAMERA, Map.of(
                    "speedLimit", new AttributeSpec(AttributeType.INTEGER),
                    "direction", new AttributeSpec(AttributeType.STRING)
            ),
            ReportType.POLICE, Map.of(
                    "visible", new AttributeSpec(AttributeType.BOOLEAN)
            ),
            ReportType.ACCIDENT, Map.of(
                    "severity", new AttributeSpec(AttributeType.STRING),
                    "lanesBlocked", new AttributeSpec(AttributeType.INTEGER)
            ),
            ReportType.HEAVY_TRAFFIC, Map.of(
                    "severity", new AttributeSpec(AttributeType.STRING)
            ),
            ReportType.ROAD_CLOSURE, Map.of(
                    "reason", new AttributeSpec(AttributeType.STRING),
                    "detourAvailable", new AttributeSpec(AttributeType.BOOLEAN)
            ),
            ReportType.POTHOLE, Map.of(
                    "severity", new AttributeSpec(AttributeType.STRING)
            ),
            ReportType.CUSTOM, Map.of(
                    "label", new AttributeSpec(AttributeType.STRING)
            )
    );

    /**
     * Validates the raw attributes supplied for a given report type and returns a
     * sanitized copy that is safe to persist. Unknown keys, wrong value types, and
     * out-of-range values are all rejected.
     *
     * @param type          the report type the attributes belong to
     * @param rawAttributes the client-supplied attributes (may be null/empty)
     * @return a sanitized map, or {@code null} if no usable attributes were supplied
     * @throws BadRequestException if the payload does not match the type's schema
     */
    public Map<String, Object> validateAndSanitize(ReportType type, Map<String, Object> rawAttributes) {
        if (rawAttributes == null || rawAttributes.isEmpty()) {
            return null;
        }

        if (rawAttributes.size() > MAX_ATTRIBUTES) {
            throw new BadRequestException("Too many attributes supplied for report type " + type + ".");
        }

        Map<String, AttributeSpec> schema = SCHEMAS.getOrDefault(type, Map.of());
        Map<String, Object> sanitized = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : rawAttributes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) continue;

            AttributeSpec spec = schema.get(key);
            if (spec == null) {
                throw new BadRequestException("Attribute '" + key + "' is not supported for report type " + type + ".");
            }

            sanitized.put(key, sanitizeValue(key, value, spec));
        }

        return sanitized.isEmpty() ? null : sanitized;
    }

    private Object sanitizeValue(String key, Object value, AttributeSpec spec) {
        switch (spec.type()) {
            case STRING:
                if (!(value instanceof String)) {
                    throw new BadRequestException("Attribute '" + key + "' must be a string.");
                }
                String cleaned = Jsoup.clean((String) value, Safelist.none()).trim();
                if (cleaned.isEmpty()) {
                    throw new BadRequestException("Attribute '" + key + "' cannot be empty.");
                }
                if (cleaned.length() > MAX_STRING_LENGTH) {
                    throw new BadRequestException("Attribute '" + key + "' exceeds max length of " + MAX_STRING_LENGTH + ".");
                }
                if (key.equals("severity") && !ALLOWED_SEVERITIES.contains(cleaned.toUpperCase())) {
                    throw new BadRequestException("Attribute 'severity' must be one of " + ALLOWED_SEVERITIES + ".");
                }
                return cleaned;

            case INTEGER:
                if (!(value instanceof Number)) {
                    throw new BadRequestException("Attribute '" + key + "' must be a number.");
                }
                int intValue = ((Number) value).intValue();
                if (key.equals("speedLimit") && (intValue < 1 || intValue > 300)) {
                    throw new BadRequestException("Attribute 'speedLimit' must be between 1 and 300.");
                }
                if (key.equals("lanesBlocked") && (intValue < 0 || intValue > 20)) {
                    throw new BadRequestException("Attribute 'lanesBlocked' must be between 0 and 20.");
                }
                return intValue;

            case BOOLEAN:
                if (!(value instanceof Boolean)) {
                    throw new BadRequestException("Attribute '" + key + "' must be a boolean.");
                }
                return value;

            default:
                throw new BadRequestException("Unsupported attribute type for '" + key + "'.");
        }
    }
}
