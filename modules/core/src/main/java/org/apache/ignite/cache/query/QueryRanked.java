package org.apache.ignite.cache.query;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class QueryRanked implements Comparable<QueryRanked>, Serializable {
    private static final long serialVersionUID = -1L;
    public static final String RANK_FIELD_NAME = "rank";
    private float rank;

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = rank;
    }

    /**
     *
     * @param that
     * @return
     */
    @Override
    public int compareTo(@NotNull QueryRanked that) {
        return Float.compare(this.rank, that.rank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryRanked that = (QueryRanked) o;
        return Float.compare(that.rank, rank) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank);
    }
}
