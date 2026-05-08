// Forwards matched paths to the Spring Boot backend during development.
// Not used in production — nginx handles routing there.
module.exports = {
  '/api': { target: 'http://localhost:8080' },
  '/ott/generate': { target: 'http://localhost:8080' },
  '/login/ott': {
    target: 'http://localhost:8080',
    // `bypass` is a fixed option name recognised by http-proxy-middleware.
    // Returning a path skips the proxy and serves that file instead.
    // GET /login/ott is the Angular route (magic link landing page), so serve
    // index.html and let Angular's router handle it. POST /login/ott is the
    // token submission and must reach the backend, so fall through to the proxy.
    // In other words, clicking on confirm link first does a GET request to
    // /login/ott then POST to same endpoint (POST being done in `LoginOttComponent`)
    bypass: function (req) {
      if (req.method !== 'POST') {
        return '/index.html';
      }
    },
  },
  '/logout': { target: 'http://localhost:8080' },
};
