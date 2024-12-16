package es.iesjandula.reaktor.timetable_server.models.parse;

import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlotEntity;
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
public class TimeSlot
{
	/** Attribute numTr */
	private String numTr;

	/** Attribute numeroDia */
	private String dayNumber;

	/** Attribute horaInicio */
	private String startHour;

	/** Attribute horaFinal */
	private String endHour;

	public TimeSlot(TimeSlotEntity timeslotEntity)
	{
		this.numTr = timeslotEntity.getNumTr();
		this.dayNumber = timeslotEntity.getDayNumber();
		this.startHour = timeslotEntity.getStartHour();
		this.endHour = timeslotEntity.getEndHour();
	}

}