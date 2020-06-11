package dk.skat.ict.sspts.batch;

/**
 * Created by mns on 02-03-2017.
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args).close(); //TODO: Close application
    }
}