# V13 RTP simulation

Date: 2026-09-14. Java 21, `tools/Simulate.java`, bet 50 virtual chips.
Each sample is a paid spin followed by all free spins and retriggers it awards.
Balances never stop the simulation. No deposits enter the RTP denominator.

| Run | Paid spins | Seed | Free spins | Observed RTP | Approximate 95% confidence interval |
|---|---:|---:|---:|---:|---:|
| Legacy calibration | 10,000,000 | 20260914 | 1,049,048 | 96.712358% | 96.545966–96.878750% |
| INTRO_97 validation | 10,000,000 | 771 | 1,046,856 | 96.962409% | 96.795370–97.129447% |
| STANDARD_96 validation | 10,000,000 | 991 | 1,047,344 | 96.135036% | 95.969859–96.300213% |

Payout scales: 0.97 / 0.96712357778 and 0.96 / 0.96712357778, applied before rounding
each cascade to the nearest hundredth of a chip. Symbol weights, Wild, Scatter,
bonus awards and cascade multipliers remain fixed. Hit rates for the validation
samples were 55.545% and 55.538%; these count paid cycles with any payout, including bonuses.

The independent validation samples are statistically compatible with their respective
targets. This is empirical calibration, not an exact mathematical proof or certification.
Confidence intervals use sample variance of complete paid cycles and a normal approximation.
Different bets can have small rounding differences. A week's observed RTP can be outside
these intervals because it has a different sample size and possibly unfinished bonuses.

Reproduce from project root (Java 21):

```sh
mkdir -p /tmp/ace-sim
javac -d /tmp/ace-sim server/src/main/java/philip/emerald/ace/superace/GameEngine.java tools/Simulate.java
java -Xmx256m -cp /tmp/ace-sim Simulate 10000000 771 INTRO_97
java -Xmx256m -cp /tmp/ace-sim Simulate 10000000 991 STANDARD_96
```

Production uses SecureRandom; reproducible simulation uses SplittableRandom.
