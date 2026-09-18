# V37 — Color Game mobile HD

## Rendering

The old WebGL renderer capped pixel density at 1.5. V37 chooses the larger of native device density and 720 / viewport short edge, capped at 3. The canvas uses the dimensions of the dice scene, not an artificially stretched full-screen video frame. CSS/SVG panels and text remain vector-based at native screen density.

A 390×844 CSS-pixel phone at DPR 3 now renders the measured 246×151 dice scene at 738×453 pixels, compared with 369×226 previously: twice the resolution in each dimension. The dice occupy only part of the screen; their canvas is not claimed to be a full 1280×720 frame. Low-DPR devices also benefit from supersampling, although software cannot add physical pixels to a low-resolution display.

The renderer limits each scene buffer to 1.5 million pixels and the GPU texture-size limit. A size/density change is required before reallocating the canvas. Shadows use a 1024×1024 map instead of 512×512. Plain face colors use solid materials rather than unnecessary small bitmap textures. Physics, final colors and payouts are unchanged.

Mobile labels are slightly larger. The case's extra filter layer is removed on phones. The V36 win/receipt orientation fix is retained.

## Validation

Real bundled WebGL renderer and physics worker, loaded from the release JAR's static assets in Chromium with software WebGL:

| Viewport (CSS pixels) | DPR | Render scale | Dice buffer |
| --- | ---: | ---: | --- |
| 390×844 | 3 | 3 | 738×453 |
| 844×390 | 3 | 3 | 738×453 |
| 360×800 | 2 | 2 | 464×266 |
| 320×568 | 1 | 2.25 | 348×292 |
| 1440×900 | 1 | 1 | 416×539 |

Passed: actual buffer dimensions, memory budget, correct result colors/top faces, physics collisions and movement, resize/orientation, cancellation and lost-context fallback. No JavaScript errors. The V36 win/receipt orientation checks were rerun on the V37 packaged client.

Tests use controlled UI results, without database changes. No physical phone FPS or battery measurement was performed; 60 FPS is not guaranteed. Backend classes, migrations and dependencies are unchanged from V36.

## Running the checks

Install the exact renderer dependencies with `npm ci --prefix tools/dice-renderer`, then `npm run build --prefix tools/dice-renderer` and `python3 tools/build_client_release.py`.

Set ACE_PLAYWRIGHT and ACE_CHROMIUM for the local browser installation. Run `ACE_UI_TEST_SUITE=color_hd_browser.cjs python3 tools/run_color_win_orientation.py` for HD/physics, and the same Python command without ACE_UI_TEST_SUITE for notification orientation.

## Update

Use the new source/JAR with your existing database configuration and restart start.bat. Reload the phone page. Client cache tag: 43-mobile-hd. No SQL update is needed; schema remains V20.
