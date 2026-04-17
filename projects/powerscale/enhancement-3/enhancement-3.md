---
layout: default
title: Enhancement 3 – Databases
---

# Enhancement 3 – Databases

## Overview

The artifact selected for this enhancement is **PowerScale**, an Android weight-tracking mobile application originally developed in CS 360 Mobile Architecture and Programming. The application was built using Java, XML, and SQLite and allows users to create accounts, log in, record weight entries, set a goal weight, and track their progress over time.

This artifact was selected because it relies heavily on a local SQLite database to store user accounts, weight entries, goal weights, and SMS settings, making it a strong candidate for a database-focused enhancement.

---

## Artifact Links

- <a href="https://github.com/mannydasilvasnhu/mannydasilvasnhu.github.io/tree/main/projects/powerscale/enhancement-3/PowerScale" target="_blank" rel="noopener noreferrer">🔗 View Enhancement 3 Repository</a>
- [📦 Download Enhanced Artifact](CS%20499%20Enhancement%20Three%20PowerScale%20Manny%20DaSilva.zip)
- [📄 View Enhancement Narrative](CS%20499%20Enhancement%203%20Narrative%20Manny%20DaSilva.docx)

---

## Enhancement Description

This enhancement focused on improving the **database design, data integrity, performance, and security** of the PowerScale application.

In the original implementation, the database stored user and weight data but lacked enforced integrity rules, efficient indexing, and a safe schema upgrade process. To address these issues, several improvements were made to transform the database into a more robust and production-level data layer.

Key changes included:

- Enforcing **foreign key constraints** to maintain relational integrity
- Adding a **unique constraint** to prevent duplicate daily weight entries
- Implementing **indexes** to improve query performance
- Replacing unsafe and destructive upgrade logic with a **safe migration strategy**

These improvements ensure that the database enforces consistency, performs efficiently as data grows, and protects user data during schema changes.

---

## Key Improvements

- Enabled foreign key enforcement to maintain valid relationships between tables
- Added unique constraint on (username, date) to prevent duplicate entries
- Created indexes to improve query performance for weight logs and user data
- Implemented safe database migration using transactions
- Replaced destructive table drops with schema-preserving upgrade logic

---

## Database and Migration Screenshots

The following figures illustrate the design and implementation of the database enhancements introduced in Enhancement 3. These include the migration workflow, schema update process, and the logic used to safely upgrade the database while preserving existing user data.

<p align="center">
  <img src="{{ '/assets/images/enhancement-3-diagram.png' | relative_url }}" width="500">
</p>
<p align="center"><em>Figure 8. Enhancement 3 design diagram illustrating the database migration workflow, including schema updates, constraints, and indexing.</em></p>

<br>

<p align="center">
  <img src="{{ '/assets/images/migration.png' | relative_url }}" width="500">
</p>
<p align="center"><em>Figure 9. Migration logic demonstrating safe schema updates, including table renaming, data transfer, and constraint enforcement.</em></p>

<br>

<p align="center">
  <img src="{{ '/assets/images/onUpgrade.png' | relative_url }}" width="500">
</p>
<p align="center"><em>Figure 10. onUpgrade method controlling database version changes and triggering the appropriate migration process.</em></p>

---

## Skills Demonstrated

This enhancement demonstrates:

- Relational database design and integrity enforcement
- Use of constraints to maintain data consistency
- Performance optimization through indexing
- Database lifecycle management and migration strategies
- Application of secure data handling practices

---

## Course Outcomes Alignment

### Outcome 3
Design and evaluate computing solutions using computer science practices and standards while managing design trade-offs.

This enhancement demonstrates Outcome 3 by designing a database solution that balances integrity, performance, and maintainability. Decisions such as enforcing constraints at the database level instead of relying solely on application logic reflect careful evaluation of design trade-offs.

---

### Outcome 4
Demonstrate an ability to use well-founded techniques, skills, and tools in computing practices to implement solutions that deliver value.

This enhancement applies well-established database practices, including indexing, relational constraints, and transactional migrations, to improve system reliability and performance.

---

### Outcome 5
Develop a security mindset that anticipates adversarial exploits and mitigates design flaws to ensure privacy and security of data and resources.

This enhancement strongly demonstrates Outcome 5 by improving data protection and integrity. Enforcing foreign keys prevents invalid relationships, unique constraints prevent duplicate or conflicting data, and safe migration logic prevents data loss. These improvements ensure that the system is resilient against data corruption and maintains secure handling of user information.

---

## Challenges and Solutions

One of the main challenges during this enhancement was implementing a safe migration process that preserved existing data while introducing new constraints.

A specific issue involved handling duplicate weight entries that existed in the original dataset, which conflicted with the new unique constraint. This required designing a migration process that filtered and retained only valid records while preventing failures during schema updates.

Another challenge was ensuring that all related tables were properly migrated without losing user data. This required careful sequencing of migration steps, including renaming existing tables, recreating updated schemas, copying data, and removing temporary tables, all within a transaction.

In addition, these changes had to be integrated without breaking existing application functionality, requiring thorough validation and testing.

---

## Reflection

This enhancement strengthened my understanding of database design, data integrity, and long-term system maintenance. One of the most important lessons was the importance of enforcing rules at the database level rather than relying solely on application logic.

I also gained experience implementing safe schema migrations, which is critical in real-world applications where data must be preserved across updates. This reinforced the importance of designing systems that are not only functional, but also reliable, secure, and scalable over time.

---

## Summary

This enhancement transformed the PowerScale database from a basic storage system into a more secure, reliable, and efficient data layer.

By applying professional database practices such as constraints, indexing, and safe migrations, the application now better protects user data and performs more efficiently. This work demonstrates strong competency in database design, data integrity, and secure system development.