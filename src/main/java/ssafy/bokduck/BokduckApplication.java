package ssafy.bokduck;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("ssafy.bokduck.mapper")
public class BokduckApplication {

	public static void main(String[] args) {
		SpringApplication.run(BokduckApplication.class, args);
	}

}
