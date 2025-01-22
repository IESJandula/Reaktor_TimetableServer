package es.iesjandula.reaktor.timetable_server.models.jpa;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuntosConvivenciaAlumnoCursoId implements Serializable
{
	private static final long serialVersionUID = -3829076593351286453L;

	/**Referencia a la clave primaria id de la entidad alumno */
	private Long alumnoId;
	
	/**Referencia a la clave primaria de la entidad curso */
	private CursoId cursoId;
	
	/**Referencia a la clave primaria de la entidad puntos convivencia  */
	private Long puntosId;
	
	/**Fecha en la que se impuso la sancion */
	private Date fecha;
}
