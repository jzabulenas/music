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
- If above and below an `if` statement there is variable definition, you must add an empty line above and below the `if` statement
- Inside of methods, variable definitions should be grouped when it makes sense
- When writing comments, if you refer to code, encase it with ``

## Spring

- Use Spring Boot 4
  - Use Spring Framework 7
  - Use Spring Security 7
- Use JSpecify annotations. Packages must be `@NullMarked` by default
  - If anything else needs nullability, use `@Nullable`. Handle the nullability

### JPA

- Entities default constructor should also be package-private
- Only add `@Transactional` when it makes sense

## Maven

- Sources exist for all dependencies. Do not decompile .class files when researching

## Tests

- Every test name must be fully backed by its assertion — if the name claims "returnsSameRecs", the body must verify the actual data matches, not just the count
