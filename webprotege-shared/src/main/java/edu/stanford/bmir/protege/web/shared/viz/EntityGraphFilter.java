package edu.stanford.bmir.protege.web.shared.viz;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.GwtCompatible;
import edu.stanford.bmir.protege.web.shared.match.criteria.MultiMatchType;

import javax.annotation.Nonnull;
import javax.validation.constraints.Null;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-11
 */
@AutoValue
@GwtCompatible(serializable = true)
public abstract class EntityGraphFilter {

    private static final String NAME = "name";

    private static final String DESCRIPTION = "description";

    private static final String INCLUSION_CRITERIA = "inclusionCriteria";

    private static final String EXCLUSION_CRITERIA = "exclusionCriteria";

    @JsonCreator
    public static EntityGraphFilter get(@Nonnull @JsonProperty(NAME) FilterName name,
                                        @Nonnull @JsonProperty(DESCRIPTION) String description,
                                        @Nonnull @JsonProperty(INCLUSION_CRITERIA) CompositeEdgeCriteria inclusionCriteria,
                                        @Nonnull @JsonProperty(EXCLUSION_CRITERIA) CompositeEdgeCriteria exclusionCriteria) {
        return new AutoValue_EntityGraphFilter(name, description, inclusionCriteria, exclusionCriteria);
    }

    @Null
    public static EntityGraphFilter getDefault() {
        return get(FilterName.get(""),
                   "",
                   CompositeEdgeCriteria.empty(),
                   CompositeEdgeCriteria.empty());
    }

    @Nonnull
    public CompositeEdgeCriteria getCombinedCriteria() {
        return CompositeEdgeCriteria.get(MultiMatchType.ALL,
                                         getInclusionCriteria(),
                                         NegatedEdgeCriteria.get(getExclusionCriteria()));
    }

    /**
     * Gets the filter name.  May be an empty string.
     */
    @Nonnull
    @JsonProperty(NAME)
    public abstract FilterName getName();

    @Nonnull
    @JsonProperty(DESCRIPTION)
    public abstract String getDescription();

    @Nonnull
    @JsonProperty(INCLUSION_CRITERIA)
    public abstract CompositeEdgeCriteria getInclusionCriteria();

    @Nonnull
    @JsonProperty(EXCLUSION_CRITERIA)
    public abstract CompositeEdgeCriteria getExclusionCriteria();
}