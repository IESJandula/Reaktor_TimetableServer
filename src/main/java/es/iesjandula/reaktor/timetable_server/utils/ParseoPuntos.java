package es.iesjandula.reaktor.timetable_server.utils;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.timetable_server.models.jpa.PuntosConvivencia;
import es.iesjandula.reaktor.timetable_server.repository.IPuntosConvivenciaRepository;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Service
public class ParseoPuntos 
{
	
	@Autowired
	private IPuntosConvivenciaRepository iPuntosConvivenciaRepository;
	
	public void parseoPuntos(Scanner scanner) {
		
		scanner.nextLine();
		
		while(scanner.hasNext()) {
			String linea = scanner.nextLine();
			
			String[] valores = linea.split(Constants.CSV_DELIMITER);
			
			
			Integer valor = Integer.valueOf(valores[0]) ;
			
			String descripcion = valores[1];
			
			PuntosConvivencia puntosConvivencia = new PuntosConvivencia();
			
			puntosConvivencia.setValor(valor);
			puntosConvivencia.setDescripcion(descripcion);

			
			this.iPuntosConvivenciaRepository.saveAndFlush(puntosConvivencia);
			
		}
		
	}
}
