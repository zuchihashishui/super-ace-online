# Windows startup configuration

Both start.bat and start-local.bat now load this external file explicitly:

`server/src/main/resources/application.properties`

They no longer read or create .env/.env.local. Existing files are left untouched.
Edit application.properties, stop the server and launch start.bat again; no
rebuild is needed. Keep the server and release directories beside start.bat.

For your local MySQL root account, you can set these values in that file:

```properties
spring.datasource.username=root
spring.datasource.password=123456
```

The shipped username/password fallbacks are now `root` / `123456`, matching
the local account previously configured through .env.local. Other values
previously set only in .env/.env.local, including a custom JWT secret or DB URL,
must also be moved into application.properties if you want to keep them.
Do not overwrite your existing database or reset credentials.

`${DB_PASSWORD:123456}` means an existing Windows DB_PASSWORD environment
variable takes precedence over the fallback 123456. These placeholders remain
supported. Set a literal property value if you do not want that placeholder.
Standard Spring environment/system-property overrides still apply; this change
removes dotenv loading, not Spring's environment support.

The packaged username fallback is also updated to root. UI, game logic and
database schema are unchanged. Linux and Docker launch scripts retain their
existing environment-loading behavior.

Validation: the actual JAR successfully loaded an external properties file
whose path contains spaces, applied its port override and passed health check
with disposable H2 on Java 21. Windows batch files were reviewed but not
executed on Windows in this environment.
