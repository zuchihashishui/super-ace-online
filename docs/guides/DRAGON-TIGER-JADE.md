# Dragon Tiger Jade table — release V16 (database V15)

New rounds deal one card to Dragon and one to Tiger from a fresh 52-card deck, without replacement. Ace is 1, J is 11, Q is 12 and K is 13. The higher rank wins. Equal ranks tie regardless of suit.

Existing payout rules remain: Dragon/Tiger gross return 1.95x on a win; Tie gross return 9x; a tie refunds Dragon/Tiger stakes. These are the project's displayed rules, not a claim that every operator uses the same rules. Super Ace RTP settings do not apply. Changing the card rules changes the outcome probabilities. For a fresh single deck, tie probability is 3/51; theoretical gross return is approximately 97.647% for Dragon/Tiger and 52.941% for Tie with the unchanged 9x return. No payout tuning is applied based on a player's history.

The shared outcome is persisted on the first accepted wager and remains hidden until the ten-second betting window closes. Persisted outcomes and old two-card history are kept. An old pending round marked DT_TWO_CARD_V2 without a stored outcome is resolved with the legacy deal. New one-card wagers use DT_ONE_CARD_V3. Stop all old server instances before running the new release; mixed old/new rule engines are not supported during rollout. No schema change is needed beyond V15.

The new temple illustration is decorative; betting panels, countdown, cards, stacked chips, totals and controls remain live HTML. Blue Dragon, green Tie and red Tiger panels match the reference layout. The chip console sits below the panels without obscuring them. Denominations: 5, 10, 15, 20, 30, 50 and 100. Small screens can scroll the chip strip horizontally. Desktop and portrait/landscape mobile layouts are supported. Audio, reduced motion, wallet separation, daily Gold and receipt notifications remain available.

The original illustration is in client/assets/dragon-tiger-jade.png, generated using the built-in image generation tool. Prompt: Original stylized jade Chinese dragon at left and orange tiger at right on gold-trimmed red temple rooftops, bamboo forest, dark purple empty central space for live HTML cards, no text, cards, chips or interface. Landscape 3:1 composition. The generated image is an asset, not a screenshot of another app.

Validation: rank comparisons for all 169 rank pairs; 10,000 seeded deals with two distinct cards; legacy settlement; existing auth/wallet/idempotency tests; Chromium tests against the packaged JAR. Tests use H2 MySQL compatibility mode, not a live MySQL server or physical mobile devices.
