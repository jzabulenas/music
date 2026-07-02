// Forwards matched paths to the Spring Boot backend during E2E testing.
// Targets port 8081, which is where docker-compose.e2e.yml exposes the backend.
module.exports = {
  '/api': { target: 'http://localhost:8081' },
  '/ott/generate': { target: 'http://localhost:8081' },
  '/login/ott': {
    target: 'http://localhost:8081',
    bypass: function (req) {
      if (req.method !== 'POST') {
        return '/index.html';
      }
    },
  },
  '/logout': { target: 'http://localhost:8081' },
};
