package RoadReport.exceptions.special;

public class ActionForbiddenException extends RuntimeException{
    public ActionForbiddenException(String message){
        super(message);
    }
}