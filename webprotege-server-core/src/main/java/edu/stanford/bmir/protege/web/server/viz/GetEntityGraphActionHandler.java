package edu.stanford.bmir.protege.web.server.viz;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractProjectActionHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.shared.match.criteria.MultiMatchType;
import edu.stanford.bmir.protege.web.shared.viz.CompositeEdgeCriteria;
import edu.stanford.bmir.protege.web.shared.viz.EdgeCriteria;
import edu.stanford.bmir.protege.web.shared.viz.GetEntityGraphAction;
import edu.stanford.bmir.protege.web.shared.viz.GetEntityGraphResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Oct 2018
 */
public class GetEntityGraphActionHandler extends AbstractProjectActionHandler<GetEntityGraphAction, GetEntityGraphResult> {

    private static Logger logger = LoggerFactory.getLogger(GetEntityGraphActionHandler.class);

    @Nonnull
    private final EntityGraphBuilderFactory graphBuilderFactory;

    @Nonnull
    private final EdgeMatcherFactory edgeMatcherFactory;

    @Nonnull
    private final EntityGraphSettingsRepository entityGraphSettingsRepository;

    @Inject
    public GetEntityGraphActionHandler(@Nonnull AccessManager accessManager,
                                       @Nonnull EntityGraphBuilderFactory graphBuilderFactory,
                                       @Nonnull EdgeMatcherFactory edgeMatcherFactory,
                                       @Nonnull EntityGraphSettingsRepository entityGraphSettingsRepository) {
        super(accessManager);
        this.graphBuilderFactory = checkNotNull(graphBuilderFactory);
        this.edgeMatcherFactory = checkNotNull(edgeMatcherFactory);
        this.entityGraphSettingsRepository = checkNotNull(entityGraphSettingsRepository);
    }

    @Nonnull
    @Override
    public Class<GetEntityGraphAction> getActionClass() {
        return GetEntityGraphAction.class;
    }

    @Nonnull
    @Override
    public GetEntityGraphResult execute(@Nonnull GetEntityGraphAction action, @Nonnull ExecutionContext executionContext) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        var projectId = action.getProjectId();
        var userId = executionContext.getUserId();
        var entityGraphSettings = entityGraphSettingsRepository.getSettingsForUserOrProjectDefault(projectId,
                                                                                                   userId);
        var criteria = entityGraphSettings.getCriteria();
        var edgeMatcher = edgeMatcherFactory.createMatcher(criteria);
        var graph = graphBuilderFactory.create(edgeMatcher)
                                       .createGraph(action.getEntity());
        stopwatch.stop();
        logger.debug("Created entity graph [{} nodes; edges {}] in {} ms",
                    graph.getNodes().size(),
                    graph.getEdges().size(),
                    stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return GetEntityGraphResult.get(graph);
    }
}
