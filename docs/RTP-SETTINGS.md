Scope: these settings apply to **Super Ace only**. Dragon Tiger uses its published fixed payout table; see DRAGON-TIGER.md.

# Independent Lobby and Club settings

Creator sees two settings cards in Game: LOBBY / Gold and LUCKY SEVEN / Club
chips. Each card has its own input and Save button; changing play mode is not
required. Other roles cannot read or edit these settings.

Save persists to rtp_settings using mode LOBBY or CLUB as the primary key.
For example, 97.55% is stored as target_bps=9755. Revision increments protect
against overwriting another administrator's edit. An audit entry records edits.
Restarting does not reset saved settings. Fresh defaults are 100% for Lobby and 97.5% for Club (V11). V11 upgrades untouched V9 defaults only; settings explicitly saved by Creator remain unchanged. Existing free-spin sessions retain their original payout profile.

Target RTP is the desired long-run payout relative to paid wagers, using the
game's existing calibration. It is not the probability that a spin wins and
does not guarantee an individual Player's result. Observed RTP is recorded
payouts divided by recorded paid wagers, multiplied by 100. Free Spin payouts
contribute to payouts; they do not add paid wagers. Current statistics are
separate per mode and filtered to the current payout profile. If no wagers
exist for that profile, the UI shows a dash. Returning to a previously used
target includes its earlier matching-profile rounds; this is not a new reset.

Changes apply to new paid spins. An existing Free Spin sequence retains its
original profile. Balances, old results and the other mode are not changed.
Targets support 1–100% with at most two decimal places.

No new schema migration is required beyond V9. This update changes the UI and
explicit request routing only; it does not change the payout engine or backend.

Validation: live HTTP and jsdom checks passed against the updated release JAR
with disposable H2. Tested Lobby 97.55% while Club stays 97%; then Club 96.25%
while Lobby stays 97.55%; then changing Lobby to 98.25% while playing in Club.
Reloading both settings returned the independent saved values. Existing
registration, login/logout, wallet, transfer and role-visibility checks passed.
