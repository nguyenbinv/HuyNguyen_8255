package utils;

public class Utilities {
    public static String getStackTrace(StackTraceElement[] stackTradeElements) {
        try {
            String stackTrade = "";
            for (StackTraceElement element : stackTradeElements) {
                stackTrade += element.toString() + "</br>";
                if (element.toString().startsWith("suites"))
                    break;
            }
            return stackTrade;
        } catch (Exception ex) {
            return "";
        }
    }
}
