package es.iesjandula.reaktor.timetable_server.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase profesoresDto
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfesorDto 
{
	/** Nombre del profesor */
	private String nombre;
	/** Apellidos del profesor */
	private String apellidos;
	/** Correo del profesor */
	private String correo;
	/** Telefono del profesor */
	private String telefono;
	
	/** Constructor copia*/
	public ProfesorDto(ProfesorDto otroProfesor) 
	{
        this.nombre = otroProfesor.getNombre();
        
        this.apellidos = otroProfesor.getApellidos();
        
        this.correo = otroProfesor.getCorreo();
        
        this.telefono = otroProfesor.getTelefono();   
    }
}
