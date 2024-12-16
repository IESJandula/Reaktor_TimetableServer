package es.iesjandula.reaktor.timetable_server.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="actitudePointsEntity")
public class ActitudePointsEntity 
{
	   	@Id
	    private Long id; // Identificador único para la entidad
	   	
	   	@Column
	    private int points; // Puntos de actitud
	   	
	   	@Column
	    private String description; // Descripción de los puntos
	   	
	    // Constructor con parámetros para la query
	    public ActitudePointsEntity(int points, String description) 
	    {
	        this.points = points;
	        this.description = description;
	    }
}
