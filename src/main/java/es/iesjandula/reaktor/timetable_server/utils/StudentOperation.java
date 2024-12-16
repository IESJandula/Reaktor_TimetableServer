package es.iesjandula.reaktor.timetable_server.utils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.Student;
import lombok.NoArgsConstructor;

@Service
public class StudentOperation 
{
	/**Logger de la clase */
	private static Logger log = LogManager.getLogger();
	
	/**
	 * Metodo que recibe el contenido de un fichero en bytes y lo parsea para obtener
	 * el nombre, apellidos y curso del alumnado
	 * @param content contenido en bytes
	 * @return lista de alumnos parseados
	 * @throws HorariosError
	 */
	public List<Student> parseStudent(byte [] content) throws HorariosError
	{
		
		List<Student> students = new LinkedList<Student>();
		
		String stringContent = new String(content,Charset.forName("ISO-8859-1"));
		
		String [] split = stringContent.split("\n");

		for(int i = 1;i<split.length;i++)
		{
			String [] splitDatos = split[i].split(",");
			//Operaciones con el nombre del alumno
			splitDatos[0] = splitDatos[0].substring(1);
			splitDatos[0] = splitDatos[0].trim();
			//Operaciones con el apellido del alumno
			splitDatos[1] = splitDatos[1].trim();
			splitDatos[1] = splitDatos[1].substring(0, splitDatos[1].length()-1);
			//Operaciones con el curso del alumno
			splitDatos[2] = splitDatos[2].trim();
			splitDatos[2] = splitDatos[2].substring(1, splitDatos[2].length()-1);
			//Operaciones con el curso academico 
			splitDatos[3] = splitDatos[3].trim();
			splitDatos[3] = splitDatos[3].substring(1, splitDatos[3].length()-1);
			//Operaciones con el primer apellido del tutor en caso de que este vacio lo establecemos a nulo
			if(!splitDatos[4].isEmpty() && !splitDatos[4].equals("\"\""))
			{
				splitDatos[4] = splitDatos[4].trim();
				splitDatos[4] = splitDatos[4].substring(1, splitDatos[4].length()-1);
			}
			else
			{
				splitDatos[4] = null;
			}
			//Operaciones con el segundo apellido primer del tutor en caso de que este vacio lo establecemos a nulo
			if(!splitDatos[5].isEmpty() && !splitDatos[5].equals("\"\""))
			{
				splitDatos[5] = splitDatos[5].trim();
				splitDatos[5] = splitDatos[5].substring(1, splitDatos[5].length()-1);
			}
			else
			{
				splitDatos[5] = null;
			}
			//Operaciones con el nombre del primer tutor en caso de que este vacio lo establecemos a nulo
			if(!splitDatos[6].isEmpty() && !splitDatos[6].equals("\"\""))
			{
				splitDatos[6] = splitDatos[6].trim();
				splitDatos[6] = splitDatos[6].substring(1, splitDatos[6].length()-1);
			}
			else
			{
				splitDatos[6] = null;
			}
			//Operaciones con el telefono del primer tutor en caso de que este vacio lo establecemos a nulo
			if(!splitDatos[7].isEmpty() && !splitDatos[7].equals("\"\""))
			{
				splitDatos[7] = splitDatos[7].trim();
				splitDatos[7] = splitDatos[7].substring(1, splitDatos[7].length()-1);
			}
			else
			{
				splitDatos[7] = null;
			}
			//Operaciones con el correo electronico del primer tutor
			if(!splitDatos[8].isEmpty() && !splitDatos[8].equals("\"\"\r"))
			{
				splitDatos[8] = splitDatos[8].trim();
				splitDatos[8] = splitDatos[8].substring(1, splitDatos[8].length()-1);
			}
			else
			{
				splitDatos[8] = null;
			}
			
			students.add(new Student(splitDatos[1],splitDatos[0],splitDatos[2],splitDatos[3],splitDatos[4],splitDatos[5],splitDatos[6],splitDatos[7],splitDatos[8]));
		}
		return students;
	}
	/**
	 * Metodo que busca alumnos por el curso y los ordena por apellido
	 * @param course
	 * @param students
	 * @return lista de alumnos ordenada
	 * @throws HorariosError
	 */
	public Student [] sortStudentCourse(String course,List<Student> students) throws HorariosError
	{
		//Array para ordenar los alumnos
 		Student [] sortStudent = new Student[0];
		
 		//Busqueda de alumnos por curso
		for(Student student : students)
		{
			if(student.getCourse().equals(course))
			{
				sortStudent = Arrays.copyOf(sortStudent, sortStudent.length+1);
				sortStudent[sortStudent.length-1] = student;
			}
		}
		
		//Si no existen devolvemos un error
		if(sortStudent.length==0)
		{
			log.error("El curso "+course+" no se encuentra en ningun alumno");
			throw new HorariosError(404,"El curso intreoducido no coincide con ningun alumno");
		}
		
		//Si no hay error ordenamos los alumnos por apellido
		Arrays.sort(sortStudent);
		
		return sortStudent;
	}
	
	/**
	 * Metodo que busca a un estudiante por su nombre,apellidos y curso
	 * @param name
	 * @param lastName
	 * @param course
	 * @param students
	 * @return estudiante encontrado
	 */
	public Student findStudent (String name,String lastName,String course,List<Student> students)
	{
		Student student = null;
		int index = 0;
		boolean out = false;
		
		while(index<students.size() && !out)
		{
			Student item = students.get(index);
			
			if(item.getName().equals(name) && item.getLastName().equals(lastName) && item.getCourse().equals(course))
			{
				student = item;
				out = true;
			}
			
			index++;
		}
		return student;
	}
}
