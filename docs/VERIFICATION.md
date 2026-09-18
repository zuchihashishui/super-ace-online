# V13 verification — 2026-09-14

Latest registration/login/logout changes: see AUTH-TEST-RESULTS.md. The results below
describe the previous V13 baseline, including its former Agent-code registration.

- Java 21 Maven verify: 21 tests, 0 failures/errors (5 game mathematics, 12 server, 4 upgrade).
- Flyway V1 + V2 applied in H2 MySQL compatibility mode.
- JWT signature rejection, refresh rotation/replay rejection, logout/password revocation.
- Six-digit registration, referral ownership, branch isolation, role promotion/moving.
- Concurrent chip transfer replay: one debit/credit; insufficient funds and unauthorized issuance rejected.
- RTP 7-day boundary and preservation of the initiating profile during Free Spins.
- Existing manual/auto spin idempotency, separate free/paid counters, refunds, rankings,
  weekly boundaries, commission approval and historical reports remain covered.
- Live HTTP smoke: login, hierarchy, ten spins with ten exact replays, withdrawal refund,
  ranking and report totals.
- DOM emulator + live server: guest restrictions, public registration with Agent code, login, five themes, EN/Filipino catalogs,
  real spin, lost-response recovery, report/history/ranking views; no JavaScript errors.
- Restart smoke: server autoplay resumes; wallet, JWT session and RTP epoch survive;
  refresh changes the CSRF token and keeps the authenticated account.
- RTP: 10 million calibration cycles plus two independent 10 million validation cycles.
  Details and statistical limits in RTP-REPORT.md.

Limits: V13 was tested against H2's MySQL compatibility mode; no fresh real-MySQL run
was possible in this environment. Docker/MySQL deployment files are supplied.
Real Chromium could not launch because the environment rejects its socket operation;
DOM tests do not establish pixel layout, frame rate or audible playback quality.
The real-browser test script is included for execution in an unrestricted local environment.

Registration UI correction: direct gold Register navigation button; DOM checks passed for guest visibility, direct registration opening, sign-in switching and Filipino translation. Release JAR rebuilt successfully.

Registration diagnostics update: DOM tests with mocked HTTP errors passed for inline invalid Agent and duplicate errors, whitespace normalization, password UTF-8 byte length, Filipino messages and successful submission/reset. Backend validation rules unchanged; the provided console trace alone does not identify which server validation rejected the user.
