package es.iesjandula.reaktor.timetable_server.models.parse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HorarioProfesorNuevo
{
	String nombreProfesor;
	
	String primerApellido;
	
	String segundoApellido;
	
	String asignatura;
	
	String diaSemanal;
	
	String horaInicio;
	
	String horaFin;
	
	String nombreAula;
	
}
