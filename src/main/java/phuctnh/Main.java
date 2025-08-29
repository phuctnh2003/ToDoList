package phuctnh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.ArrayList;

@SpringBootApplication
public class Main extends SpringBootServletInitializer {
    private static sql_function sql_function = new sql_function();
    public static void main(String[] args) {
      SpringApplication.run(Main.class, args);

    }

}
