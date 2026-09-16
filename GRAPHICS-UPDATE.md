# Royale graphics update

Presentation-only update on top of V9:

- White card faces with larger K/Q/J illustrations across all five themes.
- New original SVG jester artwork for WILD, gold-edged Ace and violet Scatter.
- Gold combo rail above the board using the existing four multiplier elements.
- Brass cabinet, highlighted winning cards and round gold Spin button.
- Responsive layouts and reduced-motion support. Theme selection remains intact.

No game JavaScript, Java classes, RTP settings, multipliers, payouts, wallet
logic or database migrations changed. All non-static JAR entries were compared
byte-for-byte with V9 and match. Only packaged client resources were updated;
release/SHA256SUMS.txt contains the current checksum.

Verified in Chromium at widths 320, 390, 760 and 1440 across all five themes:
no document horizontal overflow, 20 cards, original ×1/×2/×3/×5 multipliers,
round Spin control and no JavaScript page errors. Registration and a real paid
Lobby spin against the packaged release passed with disposable H2. Desktop
and mobile screenshots were visually reviewed. Backend tests were not repeated
because backend code is unchanged; V9 results remain in BACKEND-TEST-RESULTS.md.

Replace the JAR and restart, then refresh the browser. For an existing V9
database this graphics update requires no database changes.
