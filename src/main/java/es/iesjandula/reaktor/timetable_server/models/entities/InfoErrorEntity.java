package es.iesjandula.reaktor.timetable_server.models.entities;

import java.time.LocalDateTime;
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
@Table(name = "infoErrorEntity")
@Entity
public class InfoErrorEntity 
{
	  	@Id
	    private Long id;  

	    @Column(nullable = false)  // es obligatorio
	    private String headerInfo;

	    @Column(nullable = false)  // es obligatorio
	    private String infoError;

	    private Boolean wait;

	    @Column(nullable = false)
	    private LocalDateTime timestamp;  // Para almacenar la fecha y hora del error

}
