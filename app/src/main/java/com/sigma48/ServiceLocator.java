package com.sigma48;

import com.sigma48.dao.*;
import com.sigma48.manager.*;

public final class ServiceLocator {

    private static final UserDao USER_DAO = new UserDao();
    private static final TargetDao TARGET_DAO = new TargetDao();
    private static final MissionDao MISSION_DAO = new MissionDao();
    private static final ReportDao REPORT_DAO = new ReportDao();
    private static final EvaluationDao EVALUATION_DAO = new EvaluationDao();

    private static final UserManager USER_MANAGER = new UserManager(USER_DAO);
    private static final AgentManager AGENT_MANAGER = new AgentManager(USER_DAO);
    private static final TargetManager TARGET_MANAGER = new TargetManager(TARGET_DAO);
    private static final MissionManager MISSION_MANAGER = new MissionManager(MISSION_DAO, TARGET_DAO, USER_DAO);
    private static final ReportManager REPORT_MANAGER = new ReportManager(REPORT_DAO, MISSION_MANAGER);
    private static final EvaluationManager EVALUATION_MANAGER = new EvaluationManager(EVALUATION_DAO, MISSION_MANAGER, USER_MANAGER);

    private ServiceLocator() {}

    public static UserManager getUserManager() {
        return USER_MANAGER;
    }

    public static AgentManager getAgentManager() {
        return AGENT_MANAGER;
    }

    public static TargetManager getTargetManager() {
        return TARGET_MANAGER;
    }

    public static MissionManager getMissionManager() {
        return MISSION_MANAGER;
    }

    public static ReportManager getReportManager() {
        return REPORT_MANAGER;
    }

    public static EvaluationManager getEvaluationManager() {
        return EVALUATION_MANAGER;
    }
}