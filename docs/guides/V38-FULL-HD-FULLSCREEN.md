# V38 — Color Game Full HD and automatic fullscreen

## Display

The WebGL density target increases from a 720px viewport short edge to 1080px. Native device density is retained when higher, up to 4×. Canvas allocation is bounded to 3 million pixels and the GPU texture-size limit. The dice scene occupies a portion of the viewport; its canvas is not a full-screen 1920×1080 video. CSS/SVG graphics use native browser rendering. Supersampling cannot increase a screen's physical resolution.

Measured packaged-renderer examples:

| CSS viewport | Device DPR | Scene render scale | Dice buffer |
| --- | ---: | ---: | --- |
| 390×844 | 3 | 3 | 738×453 |
| 844×390 | 3 | 3 | 738×453 |
| 360×800 | 2 | 3 | 696×399 |
| 320×568 | 1 | 3.375 | 523×438 |
| 1440×900 | 1 | 1.2 | 500×646 |

Phones already above the 1080px target keep their native density. V37's sharper shadows, clearer labels and V36's win-message rotation are retained.

## Fullscreen

Opening Color Game by tapping/clicking requests native fullscreen immediately, before waiting for game data, so the browser can use the original user activation. Phone orientation locking is attempted after fullscreen succeeds. CSS landscape remains the fallback if locking is unsupported.

The game also fills the browser viewport on entry, on phone and desktop. If native fullscreen is denied or unavailable, the browser's own bars may remain visible. The ↗ button allows another manual attempt. Direct/programmatic entry without user activation does not force native fullscreen.

Games exits Color Game and releases fullscreen/orientation acquired by the game. Fullscreen that already belonged to the user is preserved. Exiting fullscreen manually does not immediately trigger another request. A request that completes after leaving the game is cleaned up. Blocked game navigation never opens fullscreen.

## Validation

Tests load the actual release JAR's client files in Chromium. Fullscreen checks exercise native browser fullscreen and controlled denied/unavailable/delayed/preexisting states. UI results and navigation state use controlled fixtures without connecting to a database.

The real WebGL renderer/physics test passed at five viewport/DPR combinations: buffer density/budget, collisions and movement, final colors, resize, cancellation and GPU-context-loss fallback. Notification orientation checks cover five phone sizes, normal/reduced motion, award/receipt/toast geometry, acknowledgment, leaving Color Game and desktop behavior.

No physical phone, Safari or device FPS/battery benchmark was performed. Browser support and user settings determine whether address/navigation bars can be hidden.

## Update

Restart start.bat with the new JAR/source and retain your existing database configuration. Reload the phone page; client cache tag is 44-full-hd. No SQL update: schema, Java classes, dependencies, payouts and physics worker remain unchanged from V37.

## Reproduce browser checks

Set ACE_PLAYWRIGHT and ACE_CHROMIUM, then run the shared packaged-client fixture runner:

- `ACE_UI_TEST_SUITE=color_fullscreen_browser.cjs python3 tools/run_color_win_orientation.py`
- `ACE_UI_TEST_SUITE=color_hd_browser.cjs python3 tools/run_color_win_orientation.py`
- `python3 tools/run_color_win_orientation.py`
