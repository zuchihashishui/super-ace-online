import philip.emerald.ace.superace.GameEngine;
import java.util.SplittableRandom;
public class Simulate {
 public static void main(String[] args){long n=Long.parseLong(args[0]),seed=Long.parseLong(args[1]);var rng=new SplittableRandom(seed);var e=new GameEngine(rng::nextInt,args.length>2?args[2]:"LEGACY");long payout=0,freeCount=0,hits=0;double sum=0,squares=0;for(long i=0;i<n;i++){long cycle=0;int remaining=0;do{var o=e.spin(5000);cycle+=o.winCents();if(remaining>0){remaining--;freeCount++;}remaining+=o.freeAward();}while(remaining>0);if(cycle>0)hits++;payout+=cycle;double x=cycle/5000.0;sum+=x;squares+=x*x;}double mean=sum/n,se=Math.sqrt((squares-n*mean*mean)/(n-1)/n);System.out.printf(java.util.Locale.ROOT,"paid=%d seed=%d free=%d payout=%d RTP=%.6f%% CI95=[%.6f, %.6f] hit=%.3f%%%n",n,seed,freeCount,payout,100*mean,100*(mean-1.96*se),100*(mean+1.96*se),100.0*hits/n);}
}
