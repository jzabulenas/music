## Java

- Use package by feature, not package by layer
- Everything should be package private by default
  - Increase visibility only when required
  - Fields should be private
  - Methods used within same class should be private
- There should be an empty space before any return keyword
- Do not use `var` - always specify type
- Always refer to fields with `this`, even when not required
- Use text blocks

## Spring

- Use Spring Boot 4

### JPA

- Entities default constructor should also be package-private
- Only add `@Transactional` when it makes sense

## Maven

- Sources exist for all dependencies. Do not decompile .class files when researching
