package com.capg.todo.controller;

import com.capg.todo.model.Task;
import com.capg.todo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TaskController {

    @Autowired
    private TaskRepository taskRepo;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", taskRepo.findAll());
        model.addAttribute("task", new Task());
        return "index";
    }

    @PostMapping("/add")
    public String addTask(@ModelAttribute Task task) {
        taskRepo.save(task);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskRepo.findById(id).orElseThrow());
        return "edit";
    }

    @PostMapping("/edit/{id}")
    public String editTask(@PathVariable Long id, @ModelAttribute Task task) {
        Task existing = taskRepo.findById(id).orElseThrow();
        existing.setDescription(task.getDescription());
        existing.setCompleted(task.isCompleted());
        taskRepo.save(existing);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskRepo.deleteById(id);
        return "redirect:/";
    }
}
