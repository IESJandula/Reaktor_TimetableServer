package es.iesjandula.reaktor.timetable_server.models.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alumnos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alumnos 
{
	/**Id del alumno autoincremental */
	//GeneratedValue hace que el id vaya incrementandose uno a uno
	//GenerationType.Identity indica que el valor sera generado por la base de datos
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "alumno_id")
	private Long alumnoId;
	
	/**Nombre del alumno */
	@Column(length = 30,nullable = false)
	private String nombre;
	
	/**Apellidos del alumno */
	@Column(length = 50,nullable = false)
	private String apellidos;
	
	/**
	 * Constructor que instancia un alumno por su nombre y apellidos
	 * el id no se coloca porque se autogenera
	 * @param nombre
	 * @param apellidos
	 */
	public Alumnos(String nombre,String apellidos)
	{
		this.nombre = nombre;
		this.apellidos = apellidos;
	}
	
}
