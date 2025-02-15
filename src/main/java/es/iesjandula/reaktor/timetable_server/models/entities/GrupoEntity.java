package es.iesjandula.reaktor.timetable_server.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class GrupoEntity implements Comparable<GrupoEntity>
{
	/** Attribute numIntGr */
	@Id
	private String numIntGr;
	
	/** Attribute abreviatura */
	@Column
	private String abreviatura;
	
	/** Attribute nombre */
	@Column
	private String nombre;
		
	@Override
	public int compareTo(GrupoEntity other)
	{
		return this.nombre.compareTo(other.nombre);
	}
}