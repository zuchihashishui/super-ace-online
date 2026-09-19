# V41: Scatter bonus after cascades
Super Ace now checks Scatter before each cascade evaluation, including the final board. Three or more visible Scatter award 10 free spins, once per spin. The same rule applies in a free spin. The client announces the server award only when the displayed board reaches the trigger; old saved results without an award do not gain a client-generated bonus.

Java 21 compiled GameEngine and its nested records are included in the release JAR. Other backend classes and migrations are unchanged. Server wallet transactions already add outcome.freeAward(), subtract one only when using a free spin, and retain request idempotency. No database migration or retroactive award for old rounds.

Validation: ten deterministic Java engine assertions and four UI sequencing scenarios passed using classes/assets extracted from the packaged JAR. Covers initial trigger, insufficient Scatter, first/later cascades, repeated triggers, 4 Scatter, configured profiles and old results. JavaScript syntax checked. Full Spring/MySQL integration not run in this environment.

RTP: extending bonus eligibility increases expected bonus frequency. The configured payout scale is unchanged; the prior theoretical RTP calibration has not been revalidated for this expanded bonus rule. Do not treat configured RTP as a newly verified measured return.

Install: replace the application with this complete package, restart start.bat and refresh the browser. No SQL update.
