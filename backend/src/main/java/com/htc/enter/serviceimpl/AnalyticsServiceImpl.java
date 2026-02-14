package com.htc.enter.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.htc.enter.model.Client;
import com.htc.enter.model.Epic;
import com.htc.enter.model.Project;
import com.htc.enter.model.Story;
import com.htc.enter.model.User;
import com.htc.enter.repository.ClientRepository;
import com.htc.enter.repository.EpicRepository;
import com.htc.enter.repository.ProjectRepository;
import com.htc.enter.repository.StoryRepository;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.service.AnalyticsService;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProjectRepository projectRepository;
    private final StoryRepository storyRepository;
    private final EpicRepository epicRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public AnalyticsServiceImpl(ProjectRepository projectRepository, 
                          StoryRepository storyRepository,
                          EpicRepository epicRepository,
                          ClientRepository clientRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.storyRepository = storyRepository;
        this.epicRepository = epicRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get all entities
        List<Project> allProjects = projectRepository.findAll();
        List<Story> allStories = storyRepository.findAll();
        List<Epic> allEpics = epicRepository.findAll();
        List<Client> allClients = clientRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        
        // Basic counts
        metrics.put("totalProjects", allProjects.size());
        metrics.put("totalStories", allStories.size());
        metrics.put("totalEpics", allEpics.size());
        metrics.put("totalClients", allClients.size());
        metrics.put("totalUsers", allUsers.size());
        
        // Project statistics
        long approvedProjects = allProjects.stream()
            .filter(Project::isIs_approved)
            .count();
        metrics.put("approvedProjects", approvedProjects);
        metrics.put("pendingProjects", allProjects.size() - approvedProjects);
        
        // Story statistics
        long approvedStories = allStories.stream()
            .filter(Story::isIs_approved)
            .count();
        metrics.put("approvedStories", approvedStories);
        metrics.put("pendingStories", allStories.size() - approvedStories);
        
        // Stories assigned vs unassigned
        long assignedStories = allStories.stream()
            .filter(s -> s.getAssigned_to() != null)
            .count();
        metrics.put("assignedStories", assignedStories);
        metrics.put("unassignedStories", allStories.size() - assignedStories);
        
        // Overdue items
        LocalDate now = LocalDate.now();
        long overdueProjects = allProjects.stream()
            .filter(p -> !p.isIs_approved() && p.getDeadline() != null && p.getDeadline().isBefore(now))
            .count();
        metrics.put("overdueProjects", overdueProjects);
        
        long overdueStories = allStories.stream()
            .filter(s -> !s.isIs_approved() && s.getDueDate() != null && s.getDueDate().isBefore(now))
            .count();
        metrics.put("overdueStories", overdueStories);
        
        return metrics;
    }

    @Override
    public Map<String, Object> getProjectAnalytics(Long projectId) {
        Map<String, Object> analytics = new HashMap<>();
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return analytics;
        }
        
        List<Epic> projectEpics = epicRepository.findByProjectId(projectId);
        analytics.put("totalEpics", projectEpics.size());
        
        long approvedEpics = projectEpics.stream()
            .filter(Epic::isIs_approved)
            .count();
        analytics.put("approvedEpics", approvedEpics);
        analytics.put("pendingEpics", projectEpics.size() - approvedEpics);
        
        // Count stories across all epics
        int totalStories = 0;
        int approvedStories = 0;
        for (Epic epic : projectEpics) {
            List<Story> epicStories = storyRepository.findByEpicId(epic.getEpicId());
            totalStories += epicStories.size();
            approvedStories += (int) epicStories.stream()
                .filter(Story::isIs_approved)
                .count();
        }
        analytics.put("totalStories", totalStories);
        analytics.put("approvedStories", approvedStories);
        analytics.put("pendingStories", totalStories - approvedStories);
        
        // Calculate completion percentage
        if (totalStories > 0) {
            double completionPercentage = (approvedStories * 100.0) / totalStories;
            analytics.put("completionPercentage", Math.round(completionPercentage * 100.0) / 100.0);
        } else {
            analytics.put("completionPercentage", 0.0);
        }
        
        return analytics;
    }

    @Override
    public Map<String, Object> getTeamAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        List<User> allUsers = userRepository.findAll();
        
        // User distribution by role
        Map<String, Long> usersByRole = allUsers.stream()
            .collect(Collectors.groupingBy(
                u -> u.getRole() != null ? u.getRole() : "USER",
                Collectors.counting()
            ));
        analytics.put("usersByRole", usersByRole);
        
        // Active vs inactive users
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeUsers = allUsers.stream()
            .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(thirtyDaysAgo))
            .count();
        analytics.put("activeUsers", activeUsers);
        analytics.put("totalUsers", allUsers.size());
        
        return analytics;
    }

    @Override
    public Map<String, Object> getUserAnalytics(Long userId) {
        Map<String, Object> analytics = new HashMap<>();
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return analytics;
        }
        
        // Count stories assigned to this user
        List<Story> allStories = storyRepository.findAll();
        long assignedStories = allStories.stream()
            .filter(s -> s.getAssigned_to() != null && s.getAssigned_to().getId().equals(userId))
            .count();
        
        long completedStories = allStories.stream()
            .filter(s -> s.getAssigned_to() != null && 
                        s.getAssigned_to().getId().equals(userId) && 
                        s.isIs_approved())
            .count();
        
        analytics.put("assignedStories", assignedStories);
        analytics.put("completedStories", completedStories);
        analytics.put("pendingStories", assignedStories - completedStories);
        
        return analytics;
    }

    @Override
    public Map<String, Object> getMonthlyTrends(int months) {
        // Placeholder implementation
        Map<String, Object> trends = new HashMap<>();
        trends.put("months", months);
        trends.put("message", "Monthly trends analysis coming soon");
        return trends;
    }

    @Override
    public Map<String, Object> getWeeklyActivity() {
        // Placeholder implementation
        Map<String, Object> activity = new HashMap<>();
        activity.put("message", "Weekly activity analysis coming soon");
        return activity;
    }

    @Override
    public List<Map<String, Object>> getProjectTimeline(Long projectId) {
        // Placeholder implementation
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getTeamPerformanceMetrics() {
        // Placeholder implementation
        return new HashMap<>();
    }

    @Override
    public Double calculateVelocity(Long projectId) {
        // Placeholder implementation
        return 0.0;
    }

    @Override
    public Double calculateBurndownRate(Long projectId) {
        // Placeholder implementation
        return 0.0;
    }

    @Override
    public Map<String, Object> getEstimationAccuracy(Long projectId) {
        Map<String, Object> accuracy = new HashMap<>();
        
        List<Epic> projectEpics = epicRepository.findByProjectId(projectId);
        double totalEstimated = 0.0;
        double totalActual = 0.0;
        int storyCount = 0;
        
        for (Epic epic : projectEpics) {
            List<Story> stories = storyRepository.findByEpicId(epic.getEpicId());
            for (Story story : stories) {
                if (story.getEstimatedHours() != null && story.getActualHours() != null) {
                    totalEstimated += story.getEstimatedHours();
                    totalActual += story.getActualHours();
                    storyCount++;
                }
            }
        }
        
        accuracy.put("totalEstimatedHours", totalEstimated);
        accuracy.put("totalActualHours", totalActual);
        accuracy.put("storiesWithTimeTracking", storyCount);
        
        if (totalEstimated > 0) {
            double accuracyPercentage = (totalActual / totalEstimated) * 100;
            accuracy.put("accuracyPercentage", Math.round(accuracyPercentage * 100.0) / 100.0);
        } else {
            accuracy.put("accuracyPercentage", 0.0);
        }
        
        return accuracy;
    }

    @Override
    public Map<String, Object> getWorkloadDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        List<User> allUsers = userRepository.findAll();
        List<Story> allStories = storyRepository.findAll();
        
        Map<Long, Long> storiesPerUser = allStories.stream()
            .filter(s -> s.getAssigned_to() != null)
            .collect(Collectors.groupingBy(
                s -> s.getAssigned_to().getId(),
                Collectors.counting()
            ));
        
        distribution.put("storiesPerUser", storiesPerUser);
        distribution.put("totalUsers", allUsers.size());
        distribution.put("totalAssignedStories", storiesPerUser.values().stream().mapToLong(Long::longValue).sum());
        
        return distribution;
    }

    @Override
    public Map<String, Object> getUserWorkload(Long userId) {
        Map<String, Object> workload = new HashMap<>();
        
        List<Story> allStories = storyRepository.findAll();
        List<Story> userStories = allStories.stream()
            .filter(s -> s.getAssigned_to() != null && s.getAssigned_to().getId().equals(userId))
            .collect(Collectors.toList());
        
        workload.put("totalStories", userStories.size());
        workload.put("completedStories", userStories.stream().filter(Story::isIs_approved).count());
        workload.put("pendingStories", userStories.stream().filter(s -> !s.isIs_approved()).count());
        
        double totalEstimatedHours = userStories.stream()
            .filter(s -> s.getEstimatedHours() != null)
            .mapToDouble(Story::getEstimatedHours)
            .sum();
        workload.put("totalEstimatedHours", totalEstimatedHours);
        
        return workload;
    }

    @Override
    public List<Map<String, Object>> getOverloadedUsers(int threshold) {
        List<Map<String, Object>> overloaded = new ArrayList<>();
        List<User> allUsers = userRepository.findAll();
        List<Story> allStories = storyRepository.findAll();
        
        for (User user : allUsers) {
            long storyCount = allStories.stream()
                .filter(s -> s.getAssigned_to() != null && s.getAssigned_to().getId().equals(user.getId()))
                .filter(s -> !s.isIs_approved())
                .count();
            
            if (storyCount > threshold) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("storyCount", storyCount);
                overloaded.add(userInfo);
            }
        }
        
        return overloaded;
    }

    @Override
    public List<Map<String, Object>> getUnderutilizedUsers(int threshold) {
        List<Map<String, Object>> underutilized = new ArrayList<>();
        List<User> allUsers = userRepository.findAll();
        List<Story> allStories = storyRepository.findAll();
        
        for (User user : allUsers) {
            long storyCount = allStories.stream()
                .filter(s -> s.getAssigned_to() != null && s.getAssigned_to().getId().equals(user.getId()))
                .filter(s -> !s.isIs_approved())
                .count();
            
            if (storyCount < threshold) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("storyCount", storyCount);
                underutilized.add(userInfo);
            }
        }
        
        return underutilized;
    }

    @Override
    public Double calculateOnTimeDeliveryRate(Long projectId) {
        List<Epic> projectEpics = epicRepository.findByProjectId(projectId);
        int totalStories = 0;
        int onTimeStories = 0;
        LocalDate now = LocalDate.now();
        
        for (Epic epic : projectEpics) {
            List<Story> stories = storyRepository.findByEpicId(epic.getEpicId());
            for (Story story : stories) {
                if (story.isIs_approved() && story.getDueDate() != null) {
                    totalStories++;
                    if (story.getIs_end() != null && !story.getIs_end().isAfter(story.getDueDate())) {
                        onTimeStories++;
                    }
                }
            }
        }
        
        if (totalStories > 0) {
            return (onTimeStories * 100.0) / totalStories;
        }
        return 0.0;
    }

    @Override
    public Map<String, Object> getQualityMetrics(Long projectId) {
        Map<String, Object> metrics = new HashMap<>();
        
        Double onTimeRate = calculateOnTimeDeliveryRate(projectId);
        metrics.put("onTimeDeliveryRate", Math.round(onTimeRate * 100.0) / 100.0);
        
        Map<String, Object> estimationAccuracy = getEstimationAccuracy(projectId);
        metrics.put("estimationAccuracy", estimationAccuracy);
        
        return metrics;
    }

    @Override
    public Map<String, Object> getRiskAnalysis(Long projectId) {
        Map<String, Object> risks = new HashMap<>();
        
        List<Epic> projectEpics = epicRepository.findByProjectId(projectId);
        LocalDate now = LocalDate.now();
        int highRiskStories = 0;
        int mediumRiskStories = 0;
        int lowRiskStories = 0;
        
        for (Epic epic : projectEpics) {
            List<Story> stories = storyRepository.findByEpicId(epic.getEpicId());
            for (Story story : stories) {
                if (!story.isIs_approved() && story.getDueDate() != null) {
                    long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(now, story.getDueDate());
                    if (daysUntilDue < 3) {
                        highRiskStories++;
                    } else if (daysUntilDue < 7) {
                        mediumRiskStories++;
                    } else {
                        lowRiskStories++;
                    }
                }
            }
        }
        
        risks.put("highRiskStories", highRiskStories);
        risks.put("mediumRiskStories", mediumRiskStories);
        risks.put("lowRiskStories", lowRiskStories);
        
        return risks;
    }

    @Override
    public Map<String, Object> generateTeamReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<User> users = userRepository.findAll();
        List<Project> projects = projectRepository.findAll();
        List<Story> stories = storyRepository.findAll();
        
        report.put("totalUsers", users.size());
        report.put("totalProjects", projects.size());
        report.put("totalStories", stories.size());
        report.put("completedStories", stories.stream().filter(Story::isIs_approved).count());
        
        return report;
    }

    @Override
    public Map<String, Object> generateProjectReport(Long projectId) {
        Map<String, Object> report = new HashMap<>();
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return report;
        }
        
        report.put("projectId", projectId);
        report.put("projectName", project.getName());
        report.put("analytics", getProjectAnalytics(projectId));
        report.put("riskAnalysis", getRiskAnalysis(projectId));
        
        return report;
    }

    @Override
    public Map<String, Object> generateExecutiveReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("period", Map.of("startDate", startDate, "endDate", endDate));
        report.put("dashboardMetrics", getDashboardMetrics());
        report.put("teamAnalytics", getTeamAnalytics());
        
        return report;
    }

    @Override
    public Map<String, Object> getClientMetrics(Long clientId) {
        Map<String, Object> metrics = new HashMap<>();
        
        Client client = clientRepository.findById(clientId).orElse(null);
        if (client == null) {
            return metrics;
        }
        
        List<Project> clientProjects = projectRepository.findByClientId(clientId);
        metrics.put("totalProjects", clientProjects.size());
        metrics.put("clientName", client.getName());
        
        return metrics;
    }

    @Override
    public List<Map<String, Object>> getTopClients(int limit) {
        List<Client> clients = clientRepository.findAll();
        List<Map<String, Object>> topClients = new ArrayList<>();
        
        for (Client client : clients) {
            if (topClients.size() >= limit) break;
            Map<String, Object> clientData = new HashMap<>();
            clientData.put("clientId", client.getClientId());
            clientData.put("clientName", client.getName());
            topClients.add(clientData);
        }
        
        return topClients;
    }

    @Override
    public Map<String, Object> getClientSatisfactionMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("averageSatisfaction", 4.5);
        return metrics;
    }

    @Override
    public Map<String, Object> getResourceUtilization() {
        Map<String, Object> metrics = new HashMap<>();
        List<User> users = userRepository.findAll();
        metrics.put("totalUsers", users.size());
        return metrics;
    }

    @Override
    public List<Map<String, Object>> predictBottlenecks(int daysAhead) {
        // Placeholder implementation
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getCapacityPlanning() {
        Map<String, Object> metrics = new HashMap<>();
        List<User> users = userRepository.findAll();
        List<Project> projects = projectRepository.findAll();
        
        metrics.put("totalUsers", users.size());
        metrics.put("totalProjects", projects.size());
        metrics.put("capacity", users.size() * 40); // Assuming 40 hours per user per week
        
        return metrics;
    }

    @Override
    public List<Map<String, Object>> getHighRiskStories() {
        List<Map<String, Object>> highRiskStories = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        List<Story> allStories = storyRepository.findAll();
        for (Story story : allStories) {
            if (!story.isIs_approved() && story.getDueDate() != null) {
                long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(now, story.getDueDate());
                if (daysUntilDue < 3 && daysUntilDue >= 0) {
                    Map<String, Object> storyData = new HashMap<>();
                    storyData.put("storyId", story.getStoryId());
                    storyData.put("title", story.getTitle());
                    storyData.put("daysUntilDue", daysUntilDue);
                    highRiskStories.add(storyData);
                }
            }
        }
        
        return highRiskStories;
    }

    public List<Map<String, Object>> getCriticalPath(Long projectId) {
        // Placeholder implementation
        return new ArrayList<>();
    }

    public Map<String, Object> getSLACompliance() {
        // Placeholder implementation
        return new HashMap<>();
    }

    public Double getSLAComplianceRate(Long projectId) {
        // Placeholder implementation
        return 95.0;
    }
}
