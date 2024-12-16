package es.iesjandula.reaktor.timetable_server.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
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
@Entity
@IdClass(ActividadEntityId.class)
public class ActividadEntity implements Comparable<ActividadEntity>
{
	
	/** Attribute numAct */
	private String numAct;

	/** Attribute numUn */
	private String numUn;

	@Id
	@ManyToOne
	/** Attribute tramo */
	private TimeSlotEntity tramo;

	@Id
	@ManyToOne
	/** Attribute aula */
	private AulaEntity aula;

	@Id
	@ManyToOne
	/** Attribute profesor */
	private ProfesorEntity profesor;

	/** Attribute asignatura */
	@ManyToOne
	private AsignaturaEntity asignatura;
	
	
	@ManyToOne
	private GrupoEntity grupo;
		
	/**
	 * Method compareTo
	 * @param other
	 * @return
	 */
	@Override
	public int compareTo(ActividadEntity other)
	{
		// -- USED FOR SORT ACTIVIDAD BY TRAMO ID ORDER ---
		int thisNumber = Integer.parseInt(this.tramo.getNumTr());
		int otherNumber = Integer.parseInt(other.tramo.getNumTr());
		return thisNumber-otherNumber;
	}
}