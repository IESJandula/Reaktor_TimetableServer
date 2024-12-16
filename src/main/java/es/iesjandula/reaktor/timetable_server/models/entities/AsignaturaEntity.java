package es.iesjandula.reaktor.timetable_server.models.entities;

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
public class AsignaturaEntity
{
	@Id
	/** Attribute numIntAs */
	private String numIntAs;
	
	/** Attribute abreviatura */
	private String abreviatura;

	/** Attribute nombre */
	private String nombre;
}