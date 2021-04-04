import java.util.ArrayList;
import java.util.stream.IntStream;

public class Domain {
    public final String hostname;
    public final Integer port;

    public Domain(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void PrintDomains (ArrayList<Domain> domains) {
        IntStream.range(0, domains.size()).forEachOrdered(i -> {
            System.out.println(domains.get(i).hostname + " --- " + domains.get(i).port);
        });
    }
}