/**
 * @fileoverview Report Attributes Schema
 * Single source of truth describing the extra, type-specific fields a report can
 * carry in its `attributes` map. Mirrors the schema enforced server-side by
 * `ReportAttributesValidator`, so keep the two in sync.
 */

const SEVERITY_OPTIONS = [
    { value: 'MINOR', label: 'Minor' },
    { value: 'MODERATE', label: 'Moderate' },
    { value: 'SEVERE', label: 'Severe' },
];

/**
 * Field definitions keyed by report type.
 * @type {Object<string, Array<{key: string, label: string, type: 'text'|'number'|'select'|'boolean', options?: Array, min?: number, max?: number, maxLength?: number, unit?: string}>>}
 */
export const REPORT_ATTRIBUTE_FIELDS = {
    SPEED_CAMERA: [
        { key: 'speedLimit', label: 'Speed Limit', type: 'number', min: 1, max: 300, unit: 'km/h' },
        { key: 'direction', label: 'Direction', type: 'text', maxLength: 150 },
    ],
    POLICE: [
        { key: 'visible', label: 'Clearly visible', type: 'boolean' },
    ],
    ACCIDENT: [
        { key: 'severity', label: 'Severity', type: 'select', options: SEVERITY_OPTIONS },
        { key: 'lanesBlocked', label: 'Lanes Blocked', type: 'number', min: 0, max: 20 },
    ],
    HEAVY_TRAFFIC: [
        { key: 'severity', label: 'Severity', type: 'select', options: SEVERITY_OPTIONS },
    ],
    ROAD_CLOSURE: [
        { key: 'reason', label: 'Reason', type: 'text', maxLength: 150 },
        { key: 'detourAvailable', label: 'Detour available', type: 'boolean' },
    ],
    POTHOLE: [
        { key: 'severity', label: 'Severity', type: 'select', options: SEVERITY_OPTIONS },
    ],
    CUSTOM: [
        { key: 'label', label: 'Label', type: 'text', maxLength: 150 },
    ],
};

/**
 * Builds a clean attributes payload ready to send to the backend, based on the
 * raw (string-based) form values collected from inputs/selects/checkboxes.
 * Empty/undefined values are dropped, numbers are coerced from strings.
 * @param {string} reportType
 * @param {Object<string, *>} rawValues
 * @returns {Object<string, *>|undefined} the payload, or undefined if empty
 */
export const buildAttributesPayload = (reportType, rawValues = {}) => {
    const fields = REPORT_ATTRIBUTE_FIELDS[reportType] || [];
    const payload = {};

    fields.forEach((field) => {
        const raw = rawValues[field.key];

        if (raw === undefined || raw === null || raw === '') {
            return;
        }

        if (field.type === 'number') {
            const num = Number(raw);
            if (!Number.isNaN(num)) {
                payload[field.key] = num;
            }
        } else if (field.type === 'boolean') {
            payload[field.key] = Boolean(raw);
        } else {
            payload[field.key] = raw;
        }
    });

    return Object.keys(payload).length > 0 ? payload : undefined;
};

/**
 * Formats a single attribute value for display (e.g. in the report popup).
 * @param {Object} field - the field definition from REPORT_ATTRIBUTE_FIELDS
 * @param {*} value - the raw stored value
 * @returns {string}
 */
export const formatAttributeValue = (field, value) => {
    if (field.type === 'boolean') {
        return value ? 'Yes' : 'No';
    }
    if (field.type === 'select') {
        const match = field.options?.find((opt) => opt.value === value);
        return match ? match.label : String(value);
    }
    if (field.type === 'number' && field.unit) {
        return `${value} ${field.unit}`;
    }
    return String(value);
};
