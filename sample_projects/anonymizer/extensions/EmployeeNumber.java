import org.apache.commons.lang3.RandomUtils;
public class EmployeeNumber {
    public static String randomNumber() {
        return String.format("%086d", RandomUtils.nextInt(0, 1000000) * 18);
    }
}
