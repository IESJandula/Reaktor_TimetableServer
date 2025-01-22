package es.iesjandula.reaktor.timetable_server;

import java.io.File;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import es.iesjandula.reaktor.timetable_server.utils.ParseoPuntos;



/**
 * @author David Martinez
 */
@SpringBootApplication
@ComponentScan( basePackages = "es.iesjandula.reaktor.timetable_server")
public class TimetableApplication implements CommandLineRunner
{
	@Autowired
	private ParseoPuntos parseoPuntos;
	/**
	 * Method main to run spring app
	 * @param args main arguments
	 */
	public static void main(String[] args)
	{
		SpringApplication.run(TimetableApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception
	{
		String ruta = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "puntos.csv";
		File file = new File(ruta);
		Scanner scanner = new Scanner(file);
		this.parseoPuntos.parseoPuntos(scanner);
		
	}
}
