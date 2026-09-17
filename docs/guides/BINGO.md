# Bingo — V26

An independent 75-ball quick-ticket variant. A single stake buys one fresh 5×5 card; its center is FREE and each column uses unique numbers in its standard 15-number range. The server independently draws exactly 30 of 75 balls without replacement. At least one complete horizontal, vertical or diagonal line returns 6.75 times the stake, including the stake. Multiple winning lines still pay once. No completed line returns zero. There is no shared jackpot or purchase of extra balls. Matching and payment are automatic.

The standard card/pattern concept follows the 75-ball description at https://www.bingoblitz.com/news/how-many-numbers-in-bingo/ . The 30-call limit and fixed 6.75× return are explicit rules of this implementation, displayed in How to play, rather than a claim to copy that app's commercial rules.

The exact probability of at least one of 12 lines after 30 calls is 0.1435394694879549. This is calculated by inclusion–exclusion across the 4095 nonempty subsets of lines. For a subset with `u` distinct non-FREE cells, the probability is `C(75-u,30-u)/C(75,30)`. The gross theoretical return is 96.88914190436955% before cent rounding. A unit test independently enumerates all subsets to verify the paytable. Super Ace's configurable RTP does not apply.

Ticket and 30 calls are generated on the server, stored with the receipt and settled in one wallet transaction before the animation. Reload/retry uses the original request ID, without regenerating a ticket or charging again. Lobby/Club wallets and histories are separate. The UI automatically marks cells, highlights every completed line, shows the latest ball and all 30 calls, and celebrates only a positive net return.

Bingo needs no additional schema beyond V19. Java code is in `philip.emerald.ace.bingo`; shared ticket payment, receipts and history remain in `Utils`.
