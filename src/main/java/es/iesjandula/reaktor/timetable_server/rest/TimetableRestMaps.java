package es.iesjandula.reaktor.timetable_server.rest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.entities.AsignaturaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaPlanoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;
import es.iesjandula.reaktor.timetable_server.repository.IAulaRepository;
import es.iesjandula.reaktor.timetable_server.repository.IStudentsRepository;
import es.iesjandula.reaktor.timetable_server.utils.ApplicationPdf;
import es.iesjandula.reaktor.timetable_server.utils.TimeTableUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author David Martinez, Pablo Ruiz Canovas
 */
@RestController
@RequestMapping("/horarios/maps")
@Slf4j
public class TimetableRestMaps {
	

	/** Clase que se encarga de las operaciones logicas del servidor */
	@Autowired
	TimeTableUtils util;
	
	@Autowired
	ApplicationPdf applicationPdf;

	
	/** Lista de los planos de las aulas */
	private List<AulaPlanoEntity> aulas;

	// --------------------------- JAYDEE

	@Autowired
	private IAulaRepository aulaRepo;



	@Autowired
	private IStudentsRepository iStudentsRepo;



	
	@RequestMapping(value = "/get/classroom-planos", produces = "application/json")
	public ResponseEntity<?> getAllClassroom(@RequestParam(required = false) String planta)
	{
		try
		{
			List<AulaEntity> aulas = null ;
			if (planta != null)
			{
				// Si se proporciona la planta, filtramos por planta.
				aulas = this.aulaRepo.findByPlanta(planta);
			}
			else
			{
				// Si no se proporciona planta, obtenemos todas las aulas.
				aulas = this.aulaRepo.findAll();
			}
			return ResponseEntity.ok().body(aulas); // devueve las aulas que encuentre
		}
		catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}
	
	@RequestMapping(value = "/get/aula-now", produces = "application/json")
	public ResponseEntity<?> getCurrentClassroom(@RequestParam String numIntAu, @RequestParam String abreviatura, @RequestParam String nombre)
	{
		try
		{
			Map<String, Object> infoAula = new HashMap<String, Object>();
			// recibe por parametro
			AulaEntity aula = new AulaEntity(numIntAu, abreviatura, nombre);

			// Buscamos el aula
			// recuperar aulas
			List<AulaEntity> aulas = this.aulaRepo.findAll(); 

			if (!aulas.contains(aula))
			{
				throw new HorariosError(404, "El aula seleccionada no se encuentra en los datos proporcionados");
			}

			// Obtenemos el profesor que se encuentra actualmente en el aula
			List<ProfesorEntity> profesor = this.util.searchTeacherAulaNow(aula);
			// Obtenemos la asignatura que se imparte actualmente en el aula

			List<AsignaturaEntity> asignatura = this.util.searchSubjectAulaNow( aula);

			// Sacamos el grupo que se encuentra en el aula
			List<GrupoEntity> grupos = this.util.searchGroupAulaNow(aula);
			
			// Sacamos los alumnos que se encuentran en el aula
			//List<Student> alumnos = this.util.getAlumnosAulaNow(grupos, this.students);

			infoAula.put("profesor", profesor);
			infoAula.put("asignatura", asignatura);
			infoAula.put("grupo", grupos);
			
			//infoAula.put("alumnos", alumnos);

			return ResponseEntity.ok().body(infoAula);
		}
		catch (HorariosError exception)
		{
			log.error("Error al mostrar la informacion del aula", exception);
			return ResponseEntity.status(exception.getCode()).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/get/alumnos-bathroom", produces = "application/json")
	public ResponseEntity<?> getAlumnosBathroom()
	{
		try
		{
			// Realizamos la consulta directamente en el repositorio
			List<StudentsEntity> students = this.iStudentsRepo.findAllByInBathroomTrue();
			// Esto es para que te compruebe si hay estudiantes en el baño devolviendo un
			// null
			students = students.isEmpty() ? null : students;
			return ResponseEntity.ok().body(students);
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}
}
