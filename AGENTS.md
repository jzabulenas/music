## General best practices

- When planning new features, you must consider and include backend and frontend end to end tests in the plan, too. But you must first check if the test for the feature already exists
  - Only write actual e2e tests in `e2e` package
- The code you write must be defensively minded. I mean, write defensive code when it makes sense
- The system uses Podman - don't call Docker
  - You can run the provided Compose file for running tests, as the environment variables are already injected
- After implementing a feature, run backend and frontend tests
