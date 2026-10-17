
package com.yourname.mealplanner;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatusController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "project", "Smart Meal Planner and Grocery List Generator",
            "status", "Working",
            "message", "Meal Planner backend is running successfully!",
            "sourceCode", "https://github.com/Divyagulabnarwade/Meal_planner-backend"
        );
    }
}
