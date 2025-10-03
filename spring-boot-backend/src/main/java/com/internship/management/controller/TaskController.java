package com.internship.management.controller;

import com.internship.management.dto.ApiResponse;
import com.internship.management.dto.TaskDTO;
import com.internship.management.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getAllTasks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status
    ) {
        if (projectId != null) {
            return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByProject(projectId)));
        }
        if (userId != null) {
            return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByUser(userId)));
        }
        if (status != null) {
            return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByStatus(status)));
        }
        return ResponseEntity.ok(ApiResponse.success(taskService.getAllTasks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> getTaskById(@PathVariable Long id) {
        try {
            TaskDTO task = taskService.getTaskById(id);
            return ResponseEntity.ok(ApiResponse.success(task));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            TaskDTO createdTask = taskService.createTask(taskDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tâche créée avec succès", createdTask));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(
            @PathVariable Long id,
            @RequestBody TaskDTO taskDTO
    ) {
        try {
            TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
            return ResponseEntity.ok(ApiResponse.success("Tâche mise à jour avec succès", updatedTask));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        try {
            String statusStr = request.get("status");
            TaskDTO updatedTask = taskService.updateTaskStatus(id, statusStr);
            return ResponseEntity.ok(ApiResponse.success("Statut mis à jour avec succès", updatedTask));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok(ApiResponse.success("Tâche supprimée avec succès", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
