
package com.yourname.mealplanner;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StatusController {

    @GetMapping(value = "/", produces = "text/html")
    @ResponseBody
    public String home() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Smart Meal Planner - Backend Status</title>
                <style>
                    body {
                        margin: 0;
                        font-family: Arial, sans-serif;
                        background: #f4f7fb;
                        color: #1f2937;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        padding: 20px;
                        box-sizing: border-box;
                    }
                    .container {
                        width: 100%;
                        max-width: 680px;
                        background: white;
                        padding: 36px;
                        border-radius: 16px;
                        box-shadow: 0 8px 30px rgba(0,0,0,0.08);
                        text-align: center;
                    }
                    h1 { margin-top: 0; color: #173b65; }
                    .description {
                        color: #64748b;
                        line-height: 1.6;
                    }
                    .status {
                        margin: 28px 0;
                        padding: 22px;
                        border: 1px solid #bbf7d0;
                        background: #f0fdf4;
                        border-radius: 12px;
                    }
                    .status h2 {
                        color: #15803d;
                        margin: 0 0 10px;
                    }
                    .dot {
                        display: inline-block;
                        width: 10px;
                        height: 10px;
                        background: #22c55e;
                        border-radius: 50%;
                        margin-right: 8px;
                    }
                    .status p { color: #166534; margin-bottom: 0; }
                    .source {
                        display: inline-block;
                        margin-top: 12px;
                        padding: 12px 20px;
                        background: #173b65;
                        color: white;
                        text-decoration: none;
                        border-radius: 8px;
                    }
                    .source:hover { background: #24558b; }
                    footer {
                        margin-top: 28px;
                        font-size: 13px;
                        color: #94a3b8;
                    }
                </style>
            </head>
            <body>
                <main class="container">
                    <h1>Smart Meal Planner</h1>
                    <p class="description">
                        Backend API service for the Smart Meal Planner
                        and Grocery List Generator.
                    </p>

                    <section class="status">
                        <h2><span class="dot"></span>Backend Status: Working</h2>
                        <p>Meal Planner backend is running successfully!</p>
                    </section>

                    <h2>Source Code</h2>
                    <p class="description">
                        View the complete backend source code on GitHub.
                    </p>
                    <a class="source"
                       href="https://github.com/Divyagulabnarwade/Meal_planner-backend"
                       target="_blank" rel="noopener noreferrer">
                        View Source Code on GitHub
                    </a>

                    <footer>Smart Meal Planner &amp; Grocery List Generator</footer>
                </main>
            </body>
            </html>
            """;
    }
}
