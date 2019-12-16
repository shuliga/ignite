package org.apache.ignite.cache.query;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class extension for {@link TextQuery}
 *
 * Ranked class is supposed to hold doc score of lucene {@code org.apache.lucene.search.ScoreDoc}
 * If query should be populated with a relevant score then {@link Ranked}
 * class must be extended by type of {@link TextQuery} query
 *
 * For example:
 *
 * <code>
 *     public class SearchModel extends Ranked {
 *
 *          {@link org.apache.ignite.cache.query.annotations.QueryTextField}
 *          private String query;
 *
 *          public SearchModel(String query) {
 *              this.query = query;
 *          }
 *
 *          public String getQuery() {
 *              return query;
 *          }
 *
 *          public void setName(String query) {
 *              this.query = query;
 *          }
 *     }
 * </code>
 *
 * For getting rank value use get of a super class
 */
public class Ranked implements Comparable<Ranked>, Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * {@link java.lang.String} constat of the rank field name
     * For a simple access to the name instead of reflection
     */
    public static final String RANK_FIELD_NAME = "rank";
    private float rank;

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = rank;
    }


    /**
     * Compares the two specified values by score
     *
     * @param that rank to compare
     * @return 0 if ranks are equal, (-1 or less) if this rank less
     *         than that rank (1 or greater), if this rank greater than that rank
     */
    @Override
    public final int compareTo(@NotNull Ranked that) {
        return Float.compare(this.rank, that.rank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ranked that = (Ranked) o;
        return Float.compare(that.rank, rank) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank);
    }
}
