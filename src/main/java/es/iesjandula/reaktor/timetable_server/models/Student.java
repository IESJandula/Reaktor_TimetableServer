package es.iesjandula.reaktor.timetable_server.models;

import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;
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
public class Student implements Comparable<Student>
{
	/** Attribute name*/
	private String name;
	
	/** Attribute lastName*/
	private String lastName;
	
	/** Attribute course*/
	private String course;
	
	/**Year of school registration */
	private String matriculationYear;
	
	/**Fisrt tutor lastname */
	private String firstTutorLastName;
	
	/**Second tutor lastname */
	private String secondTutorLastName;
	
	/**Tutor name */
	private String tutorName;
	
	/**Tutor phone */
	private String tutorPhone;
	
	/**Tutor email */
	private String tutorEmail;
	

	// Constructor basado en entidad.
	public Student (StudentsEntity studentEntity)
	{
		this.name 				 = studentEntity.getName();
		this.lastName 			 = studentEntity.getLastName();
		this.course 			 = studentEntity.getCourse();
		this.matriculationYear   = studentEntity.getMatriculationYear();
		this.firstTutorLastName  = studentEntity.getFirstTutorLastName();
		this.secondTutorLastName = studentEntity.getSecondTutorLastName();
		this.tutorName 			 = studentEntity.getTutorName();
		this.tutorPhone 		 = studentEntity.getTutorPhone();
		this.tutorEmail 		 = studentEntity.getTutorEmail();
	}
	

	/**
	 * Method compareTo with personal preferences (by lastName)
	 * @param other
	 * @return int with preference
	 */
	@Override
	public int compareTo(Student other) 
	{
		if(this.lastName.compareTo(other.lastName)==0) 
		{
			return this.name.compareTo(other.name);
		}
		else 
		{
			return this.lastName.compareTo(other.lastName);
		}
	}
}
