package pservicebus.runtime;

public class ExceptionConstants {
	 public static final String EVENT_INVALID = "Topic[%s] is in invalid state and un-useable because it is currently not registered";
        public static final String SUBSCRIBER_INVALID = "Subscriber[%s] is in invalid state and un-useable because it does not exist";

        public static String topicExceptionStr(String topicName) {
            return String.format(EVENT_INVALID, topicName);
        }

        public static String subscriberExceptionStr(String subscriberName) {
            return String.format(SUBSCRIBER_INVALID, subscriberName);
        }
        public static String authenticationExceptionStr(String username) {
            return String.format("Authentication failed for User[%s]", username);
        }
        public static String AuthorizationExceptionStr(String username, String action) {
            return String.format("User[{%s}] Authorization failed for action[%s]", username, action);
        }
}
