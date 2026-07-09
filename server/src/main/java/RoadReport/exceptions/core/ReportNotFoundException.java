package RoadReport.exceptions.core;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(String message){
        super(message);
    }
}
