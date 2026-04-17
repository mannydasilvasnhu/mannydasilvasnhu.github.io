---
layout: default
title: Enhancement 1 – Software Engineering and Design
---

# Enhancement 1 – Software Engineering and Design

## Overview

The artifact selected for this enhancement is **PowerScale**, an Android weight-tracking mobile application originally developed in CS 360 Mobile Architecture and Programming. The application was built using Java, XML, and SQLite and allows users to create accounts, log in, record weight entries, set a goal weight, and track their progress over time.

This artifact was chosen because it demonstrates core software engineering concepts, including user interaction, data persistence, and full application flow from the user interface to the database layer.

---

## Artifact Links

- <a href="https://github.com/mannydasilvasnhu/mannydasilvasnhu.github.io/tree/main/projects/powerscale/enhancement-1/PowerScale" target="_blank" rel="noopener noreferrer">🔗 View Enhancement 1 Repository</a>
- [📦 Download Enhanced Artifact](CS%20499%20Enhancement%20One%20PowerScale%20Manny%20DaSilva.zip)
- [📄 View Enhancement Narrative](CS%20499%20Enhancement%201%20Narrative%20Manny%20DaSilva.docx)

---

## Enhancement Description

The original PowerScale application used a tightly coupled design where Activities handled user interface logic, business logic, and database operations. This made the application harder to maintain, test, and expand.

To improve the structure of the application, it was refactored to follow the **Model-View-ViewModel (MVVM)** architecture. This introduced a clear separation between the UI, business logic, and data access layers.

A **Repository layer** was also implemented to act as a single source of truth for data operations. This removed direct database access from Activities and centralized data handling logic.

In addition, **DAO-style separation** was introduced for database operations, and input validation and error handling were standardized across the application.

---

## Key Improvements

- Refactored application to follow **MVVM** architecture
- Introduced **ViewModel** layer for managing UI data
- Implemented **Repository** pattern for centralized data access
- Separated database operations into structured data layer components
- Removed business logic from Activities
- Standardized validation and error handling
- Improved code readability, scalability, and maintainability

---

## Architectural and Design Screenshots

The following figures illustrate the design and implementation of the software engineering improvements introduced in Enhancement 1. These include the planned system architecture and the resulting project structure after applying the Model-View-ViewModel (MVVM) pattern.

<p align="center">
  <img src="{{ '/assets/images/enhancement-1-diagram.png' | relative_url }}" width="500">
</p>
<p align="center"><em>Figure 3. Enhancement 1 design diagram illustrating the planned MVVM architecture and separation of concerns.</em></p>

<br>

<p align="center">
  <img src="{{ '/assets/images/mvvm.png' | relative_url }}" width="500">
</p>
<p align="center"><em>Figure 4. Refactored project structure demonstrating implementation of the MVVM architecture and layered design.</em></p>

---

## Skills Demonstrated

This enhancement demonstrates:

- Software architecture and design using MVVM
- Separation of concerns and modular design
- Refactoring an existing codebase into a scalable structure
- Clean data flow between UI, logic, and database layers
- Professional documentation and communication of technical changes

---

## Course Outcomes Alignment

### Outcome 1
Employ strategies for building collaborative environments that enable diverse audiences to support organizational decision making in the field of computer science.

This enhancement improves collaboration by restructuring the codebase into clearly defined layers, making it easier for other developers to understand, maintain, and extend the application.

---

### Outcome 2
Design, develop, and deliver professional-quality oral, written, and visual communications that are coherent, technically sound, and appropriately adapted to specific audiences and contexts.

This enhancement is supported through clear documentation, structured narratives, and organized presentation within the ePortfolio, allowing both technical and non-technical audiences to understand the improvements made.

---

### Outcome 4
Demonstrate an ability to use well-founded and innovative techniques, skills, and tools in computing practices to implement solutions that deliver value and accomplish industry-specific goals.

This enhancement applies industry-standard practices such as MVVM architecture, Repository pattern, and structured data handling, improving the overall quality and long-term value of the application.

---

## Challenges and Solutions

One of the main challenges during this enhancement was ensuring that all existing functionality continued to work after refactoring the application.

A specific issue occurred with weight entry and goal evaluation logic. Because LiveData updates asynchronously, the application sometimes evaluated goal completion before the latest data was fully updated, leading to inconsistent behavior with notifications and UI feedback.

This was resolved by restructuring when and where goal evaluation logic occurred, ensuring it was based on the most current observed data from the ViewModel.

---

## Reflection

This enhancement significantly improved the structure and quality of the PowerScale application. It reinforced the importance of designing software with maintainability and scalability in mind rather than focusing only on functionality.

By applying MVVM and separating responsibilities across layers, the application now reflects a more professional and industry-standard approach to software development. This experience has influenced how I approach system design moving forward, especially in professional environments where clean architecture and maintainability are critical.

---

## Summary

This enhancement transformed PowerScale from a tightly coupled application into a modular, maintainable system aligned with modern development practices. It demonstrates growth in software design, architecture, and professional development standards, and provides a strong foundation for future enhancements.