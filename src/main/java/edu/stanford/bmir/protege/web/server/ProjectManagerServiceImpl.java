package edu.stanford.bmir.protege.web.server;

import edu.stanford.bmir.protege.web.server.metaproject.MetaProjectManager;
import edu.stanford.bmir.protege.web.client.rpc.ProjectManagerService;
import edu.stanford.bmir.protege.web.client.rpc.data.*;
import edu.stanford.bmir.protege.web.server.logging.WebProtegeLogger;
import edu.stanford.bmir.protege.web.server.logging.WebProtegeLoggerManager;
import edu.stanford.bmir.protege.web.server.owlapi.*;
import edu.stanford.bmir.protege.web.shared.project.*;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 19/01/2012
 */
public class ProjectManagerServiceImpl extends WebProtegeRemoteServiceServlet implements ProjectManagerService {


    private static final WebProtegeLogger LOGGER = WebProtegeLoggerManager.get(ProjectManagerServiceImpl.class);

    public ProjectManagerServiceImpl() {
    }

    public synchronized boolean isRegisteredProject(ProjectId projectId) {
        if (projectId == null) {
            throw new NullPointerException("projectId must not be null");
        }
        return MetaProjectManager.getManager().isExistingProject(projectId);
    }

    public synchronized ProjectDetails createNewProject(NewProjectSettings newProjectSettings) throws NotSignedInException, ProjectAlreadyRegisteredException, ProjectDocumentExistsException {
        checkNotNull(newProjectSettings);

        ensureSignedIn();

//        ProjectId projectId = ProjectIdFactory.getFreshProjectId();
//
//        if(isRegisteredProject(projectId)) {
//            // Not allowed to overwrite
//            if(isProjectExistsOnDisk(projectId)) {
//                throw new ProjectAlreadyRegisteredException(projectId);
//            }
//            // For the time being, allow owners to put new sources in place
//            else if(!isSignedInUserAllowedToOverwriteProjectSources(projectId)) {
//                throw new ProjectAlreadyRegisteredException(projectId);
//            }
//        }
//        else if(isProjectExistsOnDisk(projectId)) {
//            // Too dangerous to do anything here.
//            throw new ProjectDocumentExistsException(projectId);
//        }
//


        OWLAPIProjectManager pm = OWLAPIProjectManager.getProjectManager();
        OWLAPIProject project = pm.createNewProject(newProjectSettings);
        ProjectId projectId = project.getProjectId();
        if (!isRegisteredProject(projectId)) {
            getMetaProjectManager().registerProject(projectId, newProjectSettings);
            applyDefaultSharingSettings(projectId);
            LOGGER.info("Created new project: %s", newProjectSettings.toString());
        }
        return getMetaProjectManager().getProjectDetails(projectId);
    }

    private boolean isProjectExistsOnDisk(ProjectId projectId) {
        OWLAPIProjectDocumentStore docStore = OWLAPIProjectDocumentStore.getProjectDocumentStore(projectId);
        return docStore.exists();
    }

    /**
     * Detemines whether or not the signed in user is allowed to overwrite project source.  In this implementation, the
     * signed in user can overwrite project sources if they are either the project owner, or they are an admin.
     * @param projectId The project id that identifies the project sources to be overwritten.
     * @return <code>true</code> if the signed in user can overwrite project sources, otherwise <code>false</code>.
     */
    private boolean isSignedInUserAllowedToOverwriteProjectSources(ProjectId projectId) {
        return isSignedInUserProjectOwner(projectId) || isSignedInUserAdmin();
    }


    /**
     * Applies the default sharing setting to a project.  The default sharing settings are that the project is private,
     * but the signed in user is an editor.
     * @param projectId The project id that identifies the project to apply sharing settings to.
     */
    private void applyDefaultSharingSettings(ProjectId projectId) {
        List<UserSharingSetting> userSharingSettings = new ArrayList<UserSharingSetting>();
        UserId userInSession = getUserInSession();
        if (!userInSession.isGuest()) {
            userSharingSettings.add(new UserSharingSetting(userInSession, SharingSetting.EDIT));
        }
        ProjectSharingSettings sharingSettings = new ProjectSharingSettings(projectId, SharingSetting.NONE, userSharingSettings);
        getMetaProjectManager().setProjectSharingSettings(sharingSettings);
    }


    public long getLastAccessTime(ProjectId projectId) {
        return OWLAPIProjectManager.getProjectManager().getLastAccessTime(projectId);
    }

    /**
     * Gets the list of available project types.
     * @return A list of project types.  Not null.
     * @see edu.stanford.bmir.protege.web.client.rpc.data.ProjectType
     */
    public List<ProjectType> getAvailableProjectTypes() {
        List<ProjectType> projectTypes = new ArrayList<ProjectType>();
        projectTypes.add(new ProjectType(OWLAPIProjectType.getDefaultProjectType().getProjectTypeName()));
        projectTypes.add(new ProjectType(OWLAPIProjectType.getOBOProjectType().getProjectTypeName()));
        return projectTypes;
    }

    public ProjectType getProjectType(ProjectId projectId) throws ProjectNotRegisteredException {
        OWLAPIProjectMetadataManager mdm = OWLAPIProjectMetadataManager.getManager();
        OWLAPIProjectType projectType = mdm.getType(projectId);
        return new ProjectType(projectType.getProjectTypeName());
    }

    public void setProjectType(ProjectId projectId, ProjectType projectType) throws NotProjectOwnerException, ProjectNotRegisteredException {
        if(!isSignedInUserProjectOwner(projectId)) {
            throw new NotProjectOwnerException(projectId);
        }
        OWLAPIProjectMetadataManager mdm = OWLAPIProjectMetadataManager.getManager();
        mdm.setProjectType(projectId, new OWLAPIProjectType(projectType.getName()));
    }
}
