package org.example;

public class CategoryBudget {
    private String name;
    private double budget;

    public CategoryBudget() {}
    public CategoryBudget(String name, double budget) {
        this.name = name;
        this.budget = budget;
    }

    public String getName() { return name; }
    public double getBudget() { return budget; }
    public void setName(String name) { this.name = name; }
    public void setBudget(double budget) { this.budget = budget; }
}
