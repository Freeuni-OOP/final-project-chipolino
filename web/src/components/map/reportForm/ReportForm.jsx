import { useState } from 'react';
import { Button } from '../../common/button/Button';
import { Spinner } from '../../common/spinner/Spinner';
import { Select } from '../../common/select/Select';
import { createReport } from '../../../api/reportApi.js';
import styles from './reportForm.module.css';

const ReportForm = ({ location, onClose, onSuccess }) => {
    const [reportType, setReportType] = useState('');
    const [description, setDescription] = useState('');
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);


    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors({});

        if (!reportType) {
            setErrors({ reportType: 'Please select a report type' });
            return;
        }

        if (!location || location.lat == null || location.lng == null) {
            setErrors({ general: 'Location is missing. Please try selecting on the map again.' });
            return;
        }
        setIsLoading(true);

        try {
            const reportData = {
                latitude: location.lat,
                longitude: location.lng,
                type: reportType,
                description: description
            };

            console.log('Sending report to backend:', reportData);

            await createReport(reportData);
            if (onSuccess) {
                onSuccess();
            }

            onClose();
        } catch {
            setErrors({ general: 'Failed to submit report' });
        } finally {
            setIsLoading(false);
        }
    };


    const reportOptions = [
        { value: 'POTHOLE', label: 'Pothole' },
        { value: 'ACCIDENT', label: 'Accident' },
        { value: 'HEAVY_TRAFFIC', label: 'Heavy Traffic' },
        { value: 'ROAD_CLOSURE', label: 'Road Closure' },
        { value: 'SPEED_CAMERA', label: 'Speed Camera' },
        { value: 'POLICE', label: 'Police' },
        { value: 'CUSTOM', label: 'Other' }
    ];


    return (
        <div className={styles.container}>
            <div className={styles.card}>
                <h2 className={styles.title}>Submit Report</h2>
                {errors.general && <div className={styles.errorBox}>{errors.general}</div>}

                <form onSubmit={handleSubmit}>
                    <div className={styles.inputGroup}>
                        <Select
                            label="Report Type"
                            placeholder="-- Select Type --"
                            options={reportOptions}
                            value={reportType}
                            onChange={(e) => setReportType(e.target.value)}
                            disabled={isLoading}
                        />
                        {errors.reportType && <span className={styles.errorText}>{errors.reportType}</span>}
                    </div>

                    <div className={styles.inputGroup}>
                        <label className={styles.label}>Description (optional)</label>
                        <textarea
                            className={styles.textarea}
                            placeholder="Add more details about the report..."
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            disabled={isLoading}
                        />
                    </div>

                    <div className={styles.buttonGroup}>
                        <button
                            type="button"
                            className={styles.cancelBtn}
                            onClick={onClose}
                            disabled={isLoading}
                        >
                            Cancel
                        </button>


                        <Button type="submit" disabled={isLoading}>
                            {isLoading ? <Spinner /> : 'Submit'}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ReportForm;