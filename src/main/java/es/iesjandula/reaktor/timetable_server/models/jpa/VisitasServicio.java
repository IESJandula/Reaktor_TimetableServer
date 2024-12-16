package es.iesjandula.reaktor.timetable_server.models.jpa;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visitas_servicio")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitasServicio 
{
	/**Id emmebido que referencia a las tablas alumnos y curso */
	@EmbeddedId
	private VisitasServicioId visitasServicioId;
	
	/**Id del alumno que es sancionado/recompensado */
	@ManyToOne
	@MapsId("alumnoId")
	private Alumnos alumnoId;
	
	/**Nombre del curso que pertence el alumno*/
	@ManyToOne
	@MapsId("cursoId")
	private Curso cursoId;
	
	/**Fecha en la que el alumno ha vuelto del servicio */
	@Column(name = "fecha_vuelta", nullable = true)
	private Date fechaVuelta;
}
