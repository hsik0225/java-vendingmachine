package utils;

import java.util.List;

public class Randoms {
    public static <T> T pick(final List<T> candidates) {
        return candidates.get(com.woowahan.techcourse.utils.Randoms.pick(0, candidates.size() - 1));
    }
}
