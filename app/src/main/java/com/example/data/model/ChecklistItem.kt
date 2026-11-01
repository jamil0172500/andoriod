package com.example.data.model

data class ChecklistCategory(
    val name: String,
    val iconName: String,
    val items: List<ChecklistItem>
)

data class ChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val bengaliExplanation: String,
    val severity: VulnerabilitySeverity,
    val swcReference: String,
    val isChecked: Boolean = false
)
