package com.sigma48.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sigma48.dao.base.GenericDao;
import com.sigma48.model.Report;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportDao extends GenericDao<Report> {

    public ReportDao() {
        super("data/reports.json", new TypeReference<List<Report>>() {});
    }


    public Optional<Report> findById(String reportId) {
        return find(report -> report.getReportId().equals(reportId));
    }

    public List<Report> findReportsByMissionId(String missionId) {
        return getAll().stream()
                .filter(report -> missionId.equals(report.getMissionId()))
                .collect(Collectors.toList());
    }

    public List<Report> findReportsByUserId(String userId) {
        return getAll().stream()
                .filter(report -> userId.equals(report.getUserId()))
                .collect(Collectors.toList());
    }

   
    public boolean save(Report reportToSave) {
        if (reportToSave == null) return false;
        save(reportToSave, r -> r.getReportId().equals(reportToSave.getReportId()));
        return true;
    }

    
    public boolean delete(String reportId) {
        return delete(report -> report.getReportId().equals(reportId));
    }
}