package es.iesjandula.reaktor.timetable_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;



/**
 * @author David Martinez
 */
@SpringBootApplication
@ComponentScan( basePackages = "es.iesjandula.reaktor.timetable_server")
public class TimetableApplication
{
	/**
	 * Method main to run spring app
	 * @param args main arguments
	 */
	public static void main(String[] args)
	{
		SpringApplication.run(TimetableApplication.class, args);
	}
}
