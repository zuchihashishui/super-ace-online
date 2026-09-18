# V39: Color Game menu
Games, LOBBY, LUCKY SEVEN, fullscreen, sound and rules now live in a floating hamburger menu within Color Game. Original controls and action handlers are retained. The mobile board uses the former toolbar space. Menu follows CSS landscape rotation, closes on action, Escape or backdrop, traps keyboard focus and prevents betting through the overlay.

No database or backend changes. Run start.bat from this complete release and reload the page.

Validation: Chromium tests against assets extracted from the release JAR cover desktop and phone fullscreen, denied/unsupported fullscreen, delayed exit, existing fullscreen ownership, menu visibility, control availability, focus dismissal and board height. UI fixtures do not exercise live wallet changes. Physical iPhone/Safari not tested.
