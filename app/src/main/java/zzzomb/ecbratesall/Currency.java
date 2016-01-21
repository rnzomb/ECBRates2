package zzzomb.ecbratesall;

import java.util.HashMap;

public class Currency extends HashMap<String, String> {
    public static final String NAME = "name";
    public static final String RATE = "rate";


    public Currency(String name, String rate) {
        super();
        super.put(NAME, name);
        super.put(RATE, rate);
    }
}
