---
layout: default
title: Enhancement 2 – Algorithms and Data Structures
---

# Enhancement 2 – Algorithms and Data Structures

## Overview

The artifact selected for this enhancement is **PowerScale**, an Android weight-tracking mobile application originally developed in CS 360 Mobile Architecture and Programming. The application was built using Java, XML, and SQLite and allows users to create accounts, log in, record weight entries, set a goal weight, and track their progress over time.

This artifact was selected because it provides a strong foundation for demonstrating algorithmic thinking and data structure usage in a real-world application. The system already stores weight entries as a time-series dataset using an ordered list, making it well suited for implementing analytical algorithms.

---

## Artifact Links

- <a href="https://github.com/mannydasilvasnhu/mannydasilvasnhu.github.io/tree/main/projects/powerscale/enhancement-2/PowerScale" target="_blank" rel="noopener noreferrer">🔗 View Enhancement 2 Repository</a>
- [📦 Download Enhanced Artifact](CS%20499%20Enhancement%20Two%20PowerScale%20Manny%20DaSilva.zip)
- [📄 View Enhancement Narrative](CS%20499%20Enhancement%202%20Narrative%20Manny%20DaSilva.docx)

---

## Enhancement Description

This enhancement introduced **weight trend analysis** by implementing a **sliding window moving average algorithm** along with **trend direction detection**.

In the original application, weight entries were stored and displayed but not analyzed. This enhancement transformed the data from a passive dataset into an active source of insight for the user.

A 7-day moving average was implemented using a **single-pass sliding window algorithm**, allowing efficient computation without recalculating sums repeatedly. In addition, trend direction logic was introduced to classify user progress as:

- Up
- Down
- Stable

This functionality provides meaningful feedback to users based on their weight trends over time.

---

## Key Improvements

- Implemented a 7-day moving average algorithm using a sliding window approach
- Achieved **O(n)** time complexity through use of a running sum
- Added trend direction detection based on calculated averages
- Structured weight entries as an ordered dataset for processing
- Transformed raw data into actionable insights for users

---

## Algorithm and Feature Screenshots

The following figures illustrate the design, implementation, and user-facing functionality of the algorithmic enhancements introduced in Enhancement 2. These include the system design, the moving average algorithm implementation, and the resulting trend analysis feature within the application.

<p align="center">
  <img src="{{ '/assets/images/enhancement-2-diagram.png' | relative_url }}" width="500">
</p>
<p align="center"><em>Figure 5. Enhancement 2 design diagram illustrating the integration of the moving average algorithm and trend analysis workflow.</em></p>

<br>

<p align="center">
  <img src="{{ '/assets/images/algorithm.png' | relative_url }}" width="500">
</p>
<p align="center"><em>Figure 6. Sliding window moving average algorithm implementation used to efficiently compute weight trends with O(n) time complexity.</em></p>

<br>

<p align="center">
  <img src="{{ '/assets/images/trends.png' | relative_url }}" width="250">
</p>
<p align="center"><em>Figure 7. Trends screen displaying calculated moving average results and trend direction for user weight data.</em></p>

---

## Skills Demonstrated

This enhancement demonstrates:

- Application of algorithms to real-world datasets
- Use of data structures (ordered lists) for time-series processing
- Optimization of algorithm performance through efficient design
- Evaluation of trade-offs between simplicity and complexity
- Integration of algorithmic logic into an existing application

---

## Course Outcomes Alignment

### Outcome 3
Design and evaluate computing solutions that solve a given problem using algorithmic principles and computer science practices while managing design trade-offs.

This enhancement demonstrates Outcome 3 by designing and implementing a moving average algorithm to analyze weight data. The decision to use a simple 7-day moving average instead of a more complex statistical model reflects a conscious trade-off between efficiency, usability, and complexity.

---

### Outcome 4
Demonstrate an ability to use well-founded techniques, skills, and tools in computing practices to implement solutions that deliver value and accomplish industry-specific goals.

This enhancement applies well-founded algorithmic techniques to improve application functionality. By introducing efficient data processing and meaningful analytics, the application now delivers increased value to the user.

---

## Challenges and Solutions

One of the main challenges during this enhancement was ensuring that the algorithm handled edge cases correctly. This included situations where there were fewer than seven weight entries or where data showed minimal fluctuations.

To address this, validation logic was implemented to prevent invalid calculations and ensure accurate trend results.

Another challenge was integrating the algorithm into the existing application flow without disrupting current functionality. This required aligning the computation with the data retrieval process and ensuring results updated consistently as new entries were added.

A specific issue occurred with LiveData updates, where trend results were not being reflected properly in the UI. This was due to improper observation of the data source. The issue was resolved by restructuring how LiveData was observed between the Repository, ViewModel, and UI layers, ensuring updates were triggered correctly.

---

## Reflection

This enhancement strengthened my understanding of how algorithms and data structures can be applied to real application features. One of the most important lessons was learning to treat stored data as a structured dataset rather than simple display data.

I also gained a deeper understanding of algorithm efficiency, particularly how techniques such as maintaining a running sum can significantly reduce computational overhead.

This experience reinforced the importance of designing algorithms that are not only correct, but also efficient and practical for real-world applications.

---

## Summary

This enhancement transformed PowerScale from a basic data-tracking application into a more intelligent system capable of analyzing user data and providing meaningful insights.

By applying algorithmic principles and efficient data processing techniques, the application now delivers greater value to users while demonstrating strong competency in algorithms and data structures.