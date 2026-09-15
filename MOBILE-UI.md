# Mobile UI update

- At widths up to 760px, the hamburger opens the existing navigation inside a native modal drawer. Browser-provided modal focus containment, Escape dismissal, explicit close, backdrop dismissal, current-page indication and scroll locking are supported.
- Desktop keeps the original horizontal navigation. Resizing closes the drawer and moves the same buttons, retaining role-based visibility and handlers.
- Theme choices scroll horizontally. The five-column/four-row board scales for small screens. Free spins have a separate compact counter on mobile.
- The betting panel sticks to the bottom while scrolling through the game, with safe-area padding. Landscape short-height layouts use a non-sticky panel.
- Larger touch controls, 16px form inputs, single-column forms, scrollable report tables, constrained dialog height, focus outlines and reduced-motion handling.
- English remains default; menu labels also support Filipino.
- No backend, game odds, payout or RTP changes.

## Verification

Passed JavaScript syntax checking, disposable-server HTTP registration/login/logout tests, and DOM integration tests including mobile/desktop navigation transitions, scroll lock, modal signup navigation, duplicate IDs, two-field registration and Filipino labels.

DOM tests do not validate visual rendering, native focus trapping or device performance. Real iPhone Safari / Android Chrome visual verification remains required. Suggested widths: 320, 360, 390, 430, 768 and 1440px; also test landscape and the on-screen keyboard.

## Install

Back up your deployment, then replace client files and the release JAR with this package. Keep your existing local environment configuration and MySQL data. Restart the server and refresh the page to load the versioned assets. No database migration is introduced by this UI update.
