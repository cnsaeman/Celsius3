package celsius;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecentLibraryCache extends LinkedHashMap<String,String> {

    private static final int MAX_ENTRIES = 10;

    public RecentLibraryCache() {
        super();
    }

    @Override
     protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > MAX_ENTRIES;
     }
}