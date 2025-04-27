package io.shiftmanager.you;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("io.shiftmanager.you.mapper")
public class ShiftManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShiftManagerApplication.class, args);
	}
}