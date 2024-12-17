package es.iesjandula.reaktor.timetable_server.models.jpa;

import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "puntos_convivencia_alumno_curso")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuntosConvivenciaAlumnoCurso 
{
	/**Id emmebido que referencia a las tablas alumnos, curso y puntos convivencia */
	@EmbeddedId
	private PuntosConvivenciaAlumnoCursoId puntosConvivenciaAlumnoCursoId;
	
	/**Id del alumno que es sancionado/recompensado */
	@ManyToOne
	@MapsId("alumnoId")
	private StudentsEntity alumnoId;
	
	/**Nombre del curso que pertence el alumno*/
	@ManyToOne
	@MapsId("cursoId")
	private Curso cursoId;
	
	/**Id de los puntos y la sancion/recompensa que el alumno recibe */
	@ManyToOne
	@MapsId("puntosId")
	private PuntosConvivencia puntosId;
	
}
