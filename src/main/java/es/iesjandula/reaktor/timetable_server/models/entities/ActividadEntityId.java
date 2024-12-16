package es.iesjandula.reaktor.timetable_server.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActividadEntityId
{
	private TimeSlotEntity tramo;
	
	private AulaEntity aula;

	private ProfesorEntity profesor;
}
