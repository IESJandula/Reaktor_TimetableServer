package es.iesjandula.reaktor.timetable_server.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author David Jason
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProfesorEntity implements Comparable<ProfesorEntity>
{
	
	@Id
	/** Attribute numIntPR*/
	private String numIntPR;
	
	private String abreviatura;
	
	/** Attribute nombre*/
	private String nombre;
	
	/** Attribute primerApellido*/
	private String primerApellido;
	
	/** Attribute segundoApellido*/
	private String segundoApellido;

	@Override
	public int compareTo(ProfesorEntity other)
	{
		if(this.primerApellido.compareTo(other.primerApellido)!=0)
		{
			return this.primerApellido.compareTo(other.primerApellido);
		}
		else if(this.segundoApellido.compareTo(other.segundoApellido)!=0)
		{
			return this.segundoApellido.compareTo(other.segundoApellido);
		}
		else
		{
			return this.nombre.compareTo(other.nombre);
		}
	}
}