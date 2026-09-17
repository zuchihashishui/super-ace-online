# Client

HTML/CSS/JavaScript with English (default), Filipino, five themes and synthesized sound.
The server Maven build copies this directory into the release JAR; source stays here.
Open http://localhost:8080 after starting the server. Do not open index.html as a file.
For separate hosting, serve this directory and reverse-proxy `/api/*` to Spring Boot under
the same public origin. JWT cookies and CSRF protection require that arrangement.
