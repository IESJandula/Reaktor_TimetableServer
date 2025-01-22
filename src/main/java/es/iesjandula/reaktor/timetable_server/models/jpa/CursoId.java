package es.iesjandula.reaktor.timetable_server.models.jpa;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursoId 
{
	/**Id que corresponde al nombre del curso */
	private String nombre;
	
	/**Año academico del curso  */
	private String anioAcademico;
}
