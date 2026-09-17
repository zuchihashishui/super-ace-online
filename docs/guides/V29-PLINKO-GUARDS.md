# V29 — Plinko frame bounds and expired-session polling

The reported `arcade.js?v=32-collection` is the old bundle. Its animation subtracts `performance.now()` from a requestAnimationFrame timestamp without clamping the lower bound. A frame timestamp can precede the sampled start within the current frame, producing a negative progress value and index -1; accessing that missing point's x throws and leaves the animation promise pending.

V28 changed the clock to nonnegative consecutive frame deltas and handles animation errors. V29 additionally clamps all arDraw input to [0,1], with nonfinite values reset to zero, so direct calls and restored UI state cannot address outside the path.

When refresh fails with 401, mark the session not ready and show login. Notifications now require a ready session, as auto polling already does. Pending bet IDs remain stored for same-account recovery. A first 401 can still appear when a session expires; it must not become an endless polling loop. Invalid login credentials and network errors retain their existing error handling.

Add an SVG favicon and change client cache tags to `35-plinko-fix`.

## Installation

Stop the old server in its console with Ctrl+C. Extract the full new ZIP to a new directory, preserve the existing application.properties settings, and run its start.bat. Reload the browser with Ctrl+F5. Network requests must use `arcade.js?v=35-plinko-fix`; seeing `32-collection` means an old page/server is still being used. Do not re-import SQL. No database or payout rule changes.

Guest browsing remains available without an unsolicited login dialog. Only an existing signed-in session that fails refresh opens login.

Validation: both plinko-motion and plinko browser suites passed against the actual release JAR on Java 21 with isolated H2. Coverage includes negative/NaN/infinite progress, repeated drops, slow response, hidden completion, expired-session polling stop, favicon HTTP 200, guest/register, five viewport widths, accepted-response loss/reload exactly-once accounting, history, wallet isolation, language and audio. Not tested against the user's MySQL or physical phones. No Java code changed.
