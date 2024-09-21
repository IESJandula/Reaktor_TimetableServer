package es.iesjandula.reaktor.timetable_server.models.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profesoresDto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesoresDto 
{

	/** Nombre del profesor */
	@Column(length = 30,nullable = false)
	private String nombre;
	/** Apellidos del profesor */
	@Column(length = 50,nullable = false)
	private String apellidos;
	/** Correo del profesor */
	@Column(length = 100,nullable = false)
	private String correo;
	/** Telefono del profesor */
	@Id
	@Column(name = "telefono_profesor")
	private String telefono;
	
}
