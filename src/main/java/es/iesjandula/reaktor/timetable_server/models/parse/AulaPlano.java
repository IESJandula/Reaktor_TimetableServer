package es.iesjandula.reaktor.timetable_server.models.parse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Pablo Ruiz Canovas

 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AulaPlano 
{
	/**Largo del aula*/
	private double height;
	
	/**Ancho del aula*/
	private double width;
	
	/**Medida en el eje y*/
	private double top;
	
	/**Medida en el eje x derecho*/
	private double right;
	
	/**Medida en el eje x izquierdo*/
	private double left;
	
	/**Planta en la que se encuentra el aula*/
	private String planta;
	
	/**Aula que referencia*/
	private Aula aula;
}
