## General best practices

- When planning new features, you must consider and include backend and frontend end to end tests in the plan, too
  - Do not write backend integration tests using Testcontainers, only write actual e2e tests in `e2e` package
