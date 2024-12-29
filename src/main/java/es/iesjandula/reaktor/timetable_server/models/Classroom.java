package es.iesjandula.reaktor.timetable_server.models;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author David Martinez
 *
 */
@Data
@NoArgsConstructor
public class Classroom
{
	/** Attribute number*/
	private String number;
	
	/** Attribute name classroom */
	private String name;
	
	/** Attribute floor*/
	private String floor;

	public Classroom(String number, String name, String floor) 
	{
		this.number = number;
		this.name = name;
		this.floor = floor;
	}

	public Classroom(String number, String floor) 
	{
		super();
		this.number = number;
		this.floor = floor;
	}
	
	
	
	
}
