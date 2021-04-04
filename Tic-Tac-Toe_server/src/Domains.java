import java.util.ArrayList;
import java.util.stream.IntStream;

public class Domains {
    public static final Integer amountDomains = 3;
    public static ArrayList<Domain> domains = new ArrayList<Domain>();

    public static void AddDomains(Integer amount) {
        IntStream.range(0, amount).forEachOrdered(i -> {
            domains.add(new Domain("localhost", 6100 + i));
        });
    }
}
