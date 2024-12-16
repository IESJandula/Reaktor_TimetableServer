package es.iesjandula.reaktor.timetable_server.models.parse;

import es.iesjandula.reaktor.timetable_server.models.entities.ActividadEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author David Martinez
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Actividad implements Comparable<Actividad>
{
	/** Attribute gruposActividad */
	private GruposActividad gruposActividad;

	/** Attribute numAct */
	private String numAct;

	/** Attribute numUn */
	private String numUn;

	/** Attribute tramo */
	private String tramo;

	/** Attribute aula */
	private String aula;

	/** Attribute profesor */
	private String profesor;

	/** Attribute asignatura */
	private String asignatura;
	
	public Actividad( ActividadEntity actividadEntidad  )
	{
		this.numAct = actividadEntidad.getNumAct();
		this.numUn = actividadEntidad.getNumUn();
		this.tramo = actividadEntidad.getTramo().getNumTr();
		this.aula = actividadEntidad.getAula().getNumIntAu();
		this.profesor = actividadEntidad.getProfesor().getNumIntPR();
		this.asignatura = actividadEntidad.getAsignatura().getNumIntAs();
	}

	/**
	 * Method compareTo
	 * @param other
	 * @return
	 */
	@Override
	public int compareTo(Actividad other)
	{
		// -- USED FOR SORT ACTIVIDAD BY TRAMO ID ORDER ---
		int thisNumber = Integer.parseInt(this.tramo);
		int otherNumber = Integer.parseInt(other.tramo);
		return thisNumber-otherNumber;
	}
}