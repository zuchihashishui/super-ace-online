# Simple authentication verification

Tested using Java 21, the updated release JAR, a fresh temporary H2 database in
MySQL compatibility mode, real HTTP requests and jsdom running the actual client.

Passed:

- Registration form has exactly Username and Password inputs.
- Five-character passwords are rejected; six-character passwords succeed.
- Passwords over 72 UTF-8 bytes are rejected.
- Register accepts only username/password; injected role fields are rejected.
- New accounts have PLAYER role, generated six-digit public ID, username as display name.
- No referral/Agent code is required. Direct players group is created once, commission 0%.
- Duplicate usernames (including case variants) are rejected.
- Successful registration logs in automatically.
- Incorrect password is rejected at login; correct six-character password logs in.
- Refresh preserves account identity and rotates the session.
- Logout clears both cookies; previous access and refresh tokens are rejected.
- UI logout clears the displayed balance and disables Spin.
- Inline duplicate-account error and Filipino password label work; no UI script errors.
- Complete JAR and all nested dependency CRCs validated.

The user's local MySQL server was NOT accessed. Windows start-local.bat is supplied
for their root/123456 localhost configuration; it was not executed on Windows here.
This update was compiled using javac 21 against the release dependencies. The previous
V13 JUnit suite was not rerun; the focused HTTP and DOM regression scripts above were run.

Reproduce with Java 21, H2 2.3.232 and Node + jsdom 26:

```sh
ACE_JAVA=/path/to/java ACE_H2=/path/to/h2-2.3.232.jar \
ACE_JSDOM=/path/to/node_modules/jsdom python3 tools/simple_auth_check.py
```

For a standard Maven build: `mvn -f server/pom.xml verify`.
