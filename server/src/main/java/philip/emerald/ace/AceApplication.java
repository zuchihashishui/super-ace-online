package philip.emerald.ace;
import philip.emerald.ace.superace.RtpSchedule;
import philip.emerald.ace.Utils.DirectRegistration;
import philip.emerald.ace.Utils.Accounts;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication @EnableScheduling
public class AceApplication {
 public static void main(String[] args){SpringApplication.run(AceApplication.class,args);}
 @Bean ApplicationRunner bootstrap(Accounts accounts,RtpSchedule rtp,DirectRegistration registration,@Value("${ace.creator-user}")String user,@Value("${ace.creator-password}")String password){return args->{accounts.bootstrap(user,password);rtp.initialize();registration.initialize();};}
}
