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
public class AulaEntity
{
	@Id
	/** Attribute numIntAu*/
	private String numIntAu;
	
	/** Attribute abreviatura*/
	private String abreviatura;
	
	/** Attribute nombre*/
	private String nombre;
	
	/** Atributo de planta**/
    @Column
    private String planta;

	public AulaEntity(String numIntAu, String abreviatura, String nombre)
	{
		super();
		this.numIntAu = numIntAu;
		this.abreviatura = abreviatura;
		this.nombre = nombre;
	}


    
    
}
