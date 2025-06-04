package com.sigma48.manager;

import com.sigma48.model.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

public class BriefingManager {
    private MissionManager missionManager;
    private TargetManager targetManager;
    private UserManager userManager;

    private static final String TEMPLATE_PATH = "/com/sigma48/briefing_templates/mission_briefing_template.txt";
    private static final String BRIEFING_OUTPUT_DIR = "data/briefings/";

    public BriefingManager(MissionManager missionManager, TargetManager targetManager, UserManager userManager) {
        this.missionManager = missionManager;
        this.targetManager = targetManager;
        this.userManager = userManager;
    }

    public Optional<String> generateAndSaveBriefing(String missionId) {
        Optional<Mission> missionOpt = missionManager.getMissionById(missionId);
        if (!missionOpt.isPresent()) {
            System.err.println("BriefingManager: Misi dengan ID " + missionId + " tidak ditemukan.");
            return Optional.empty();
        }
        Mission mission = missionOpt.get();

        Optional<Target> targetOpt = Optional.empty();
        if (mission.getTargetId() != null && !mission.getTargetId().isEmpty()) {
            targetOpt = targetManager.getTargetById(mission.getTargetId());
        }
        Target target = targetOpt.orElse(new Target("N/A", TargetType.LAINNYA, "N/A", ThreatLevel.TIDAK_DIKETAHUI)); // Target default jika tidak ada

        Optional<User> komandanOpt = Optional.empty();
        if (mission.getKomandanId() != null && !mission.getKomandanId().isEmpty()) {
            komandanOpt = userManager.findUserById(mission.getKomandanId());
        }
        User komandan = komandanOpt.orElse(new User("N/A", "", Role.KOMANDAN_OPERASI)); // Komandan default jika tidak ada

        // Muat template
        try {
            InputStream templateStream = BriefingManager.class.getResourceAsStream(TEMPLATE_PATH);
            if (templateStream == null) {
                System.err.println("BriefingManager: File template briefing tidak ditemukan di " + TEMPLATE_PATH);
                return Optional.empty();
            }
            String template;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(templateStream, StandardCharsets.UTF_8))) {
                template = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String briefingContent = template;
            briefingContent = briefingContent.replace("${mission.id}", defaultIfNull(mission.getId()));
            briefingContent = briefingContent.replace("${mission.judul}", defaultIfNull(mission.getJudul()));
            briefingContent = briefingContent.replace("${mission.status}", mission.getStatus() != null ? mission.getStatus().getDisplayName() : "N/A");
            briefingContent = briefingContent.replace("${mission.createdAt}", mission.getCreatedAt() != null ? mission.getCreatedAt().format(formatter) : "N/A");
            briefingContent = briefingContent.replace("${mission.updatedAt}", mission.getUpdatedAt() != null ? mission.getUpdatedAt().format(formatter) : "N/A");
            briefingContent = briefingContent.replace("${mission.tujuan}", defaultIfNull(mission.getTujuan()));
            briefingContent = briefingContent.replace("${mission.deskripsi}", defaultIfNull(mission.getDeskripsi()));

            briefingContent = briefingContent.replace("${target.nama}", defaultIfNull(target.getNama()));
            briefingContent = briefingContent.replace("${target.tipe}", target.getTipe() != null ? target.getTipe().getDisplayName() : "N/A");
            briefingContent = briefingContent.replace("${target.lokasi}", defaultIfNull(target.getLokasi()));
            briefingContent = briefingContent.replace("${target.ancaman}", target.getAncaman() != null ? target.getAncaman().getDisplayName() : "N/A");

            briefingContent = briefingContent.replace("${komandan.username}", defaultIfNull(komandan.getUsername()));
            briefingContent = briefingContent.replace("${komandan.id}", defaultIfNull(komandan.getId()));
            briefingContent = briefingContent.replace("${mission.jenisOperasi}", defaultIfNull(mission.getJenisOperasi()));
            briefingContent = briefingContent.replace("${mission.lokasi}", defaultIfNull(mission.getLokasi())); // Lokasi detail misi
            briefingContent = briefingContent.replace("${mission.strategi}", defaultIfNull(mission.getStrategi()));
            briefingContent = briefingContent.replace("${mission.protokol}", defaultIfNull(mission.getProtokol()));
            briefingContent = briefingContent.replace("${mission.analisisRisiko}", defaultIfNull(mission.getAnalisisRisiko()));

            StringBuilder agentsSection = new StringBuilder();
            if (mission.getAssignedAgents() != null && !mission.getAssignedAgents().isEmpty()) {
                for (String agentId : mission.getAssignedAgents()) {
                    User agentUser = userManager.findUserById(agentId).orElse(new User(agentId, "", Role.AGEN_LAPANGAN)); // Agen default
                    Agent agent = (agentUser instanceof Agent) ? (Agent) agentUser : new Agent(agentUser.getUsername(), agentUser.getPasswordHash(), Role.AGEN_LAPANGAN); // Buat Agent dummy jika perlu
                    
                    CoverIdentity ci = mission.getCoverIdentities() != null ? mission.getCoverIdentities().get(agentId) : new CoverIdentity("N/A", "N/A", "N/A");
                    if(ci == null) ci = new CoverIdentity("N/A", "N/A", "N/A"); // Pastikan CI tidak null

                    agentsSection.append("AGEN ID        : ").append(defaultIfNull(agent.getId())).append(System.lineSeparator());
                    agentsSection.append("NAMA SAMARAN   : ").append(defaultIfNull(ci.getCoverName())).append(System.lineSeparator());
                    agentsSection.append("PERAN SAMARAN  : ").append(defaultIfNull(ci.getCoverRole())).append(System.lineSeparator());
                    agentsSection.append("NO. DOKUMEN    : ").append(defaultIfNull(ci.getCoverPassport())).append(System.lineSeparator());
                    agentsSection.append("SPESIALISASI   : ").append(agent.getSpesialisasi() != null && !agent.getSpesialisasi().isEmpty() ? String.join(", ", agent.getSpesialisasi()) : "N/A").append(System.lineSeparator());
                    agentsSection.append("----------------").append(System.lineSeparator());
                }
                briefingContent = briefingContent.replaceFirst("(?s)\\$\\{foreach.agent\\}.*\\$\\{endforeach.agent\\}", agentsSection.toString());
                briefingContent = briefingContent.replaceFirst("(?s)\\$\\{if.noAgents\\}.*\\$\\{endif.noAgents\\}", "");

            } else {
                briefingContent = briefingContent.replaceFirst("(?s)\\$\\{foreach.agent\\}.*\\$\\{endforeach.agent\\}", "");
                briefingContent = briefingContent.replace("${if.noAgents}", "BELUM ADA AGEN YANG DITUGASKAN");
                briefingContent = briefingContent.replace("${endif.noAgents}", "");
            }
            briefingContent = briefingContent.replace("${if.noAgents}", "").replace("${endif.noAgents}", "");


            Path outputDirPath = Paths.get(BRIEFING_OUTPUT_DIR);
            if (!Files.exists(outputDirPath)) {
                Files.createDirectories(outputDirPath);
            }

            String outputFileName = "brief_" + mission.getId() + ".txt";
            Path outputFilePath = outputDirPath.resolve(outputFileName);
            Files.write(outputFilePath, briefingContent.getBytes(StandardCharsets.UTF_8));

            boolean updated = missionManager.updateMissionDokBriefingPath(missionId, outputFilePath.toString());
            if (!updated) {
                System.err.println("BriefingManager: Gagal mengupdate path dokumen briefing di misi " + missionId);
            }

            return Optional.of(outputFilePath.toString());

        } catch (IOException e) {
            System.err.println("BriefingManager: Terjadi error saat menggenerate briefing untuk misi ID " + missionId + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private String defaultIfNull(String value) {
        return value != null ? value : "N/A";
    }
}