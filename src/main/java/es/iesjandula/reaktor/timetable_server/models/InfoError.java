package es.iesjandula.reaktor.timetable_server.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoError 
{
	/**ID del error**/
	 private Long id;
	/**Cabecera del error */
	private String headerInfo;
	
	/**Informacion del error */
	private String infoError;
	
	/**Se requiere esperar para que el error cese */
	private Boolean wait;
}
