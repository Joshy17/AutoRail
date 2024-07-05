package autorizacion20.autorizacion20;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

        @GetMapping("/prueba")
    public String mensaje(){
        return "hola";
    }
    
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
