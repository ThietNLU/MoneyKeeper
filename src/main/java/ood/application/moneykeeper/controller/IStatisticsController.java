package ood.application.moneykeeper.controller;

import ood.application.moneykeeper.model.Category;

import java.time.LocalDateTime;
import java.util.Map;

public interface  IStatisticsController {
    Map<Category, Double> generateExpenseReportByCategory(LocalDateTime start, LocalDateTime end);
}
