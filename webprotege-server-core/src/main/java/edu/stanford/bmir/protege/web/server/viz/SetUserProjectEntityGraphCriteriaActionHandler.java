package edu.stanford.bmir.protege.web.server.viz;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractProjectActionHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.shared.access.BuiltInAction;
import edu.stanford.bmir.protege.web.shared.viz.SetUserProjectEntityGraphCriteriaAction;
import edu.stanford.bmir.protege.web.shared.viz.SetUserProjectEntityGraphCriteriaResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-10
 */
public class SetUserProjectEntityGraphCriteriaActionHandler extends AbstractProjectActionHandler<SetUserProjectEntityGraphCriteriaAction, SetUserProjectEntityGraphCriteriaResult> {

    @Nonnull
    private final EntityGraphSettingsRepository repository;

    @Inject
    public SetUserProjectEntityGraphCriteriaActionHandler(@Nonnull AccessManager accessManager,
                                                          @Nonnull EntityGraphSettingsRepository repository) {
        super(accessManager);
        this.repository = checkNotNull(repository);
    }


    @Nonnull
    @Override
    public Class<SetUserProjectEntityGraphCriteriaAction> getActionClass() {
        return SetUserProjectEntityGraphCriteriaAction.class;
    }

    @Nullable
    @Override
    protected BuiltInAction getRequiredExecutableBuiltInAction() {
        return BuiltInAction.VIEW_PROJECT;
    }

    @Nonnull
    @Override
    public SetUserProjectEntityGraphCriteriaResult execute(@Nonnull SetUserProjectEntityGraphCriteriaAction action,
                                                           @Nonnull ExecutionContext executionContext) {
        var projectId = action.getProjectId();
        var userId = executionContext.getUserId();
        var criteria = action.getEdgeCriteria();
        repository.saveSettings(EntityGraphSettings.get(projectId, userId, criteria));
        return new SetUserProjectEntityGraphCriteriaResult();
    }
}
