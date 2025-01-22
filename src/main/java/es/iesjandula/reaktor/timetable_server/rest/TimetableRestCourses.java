package es.iesjandula.reaktor.timetable_server.rest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.Classroom;
import es.iesjandula.reaktor.timetable_server.models.Course;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Aula;
import es.iesjandula.reaktor.timetable_server.models.parse.Grupo;
import es.iesjandula.reaktor.timetable_server.repository.IAulaRepository;
import es.iesjandula.reaktor.timetable_server.repository.IGrupoRepository;
import es.iesjandula.reaktor.timetable_server.utils.ApplicationPdf;
import es.iesjandula.reaktor.timetable_server.utils.TimeTableUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author David Martinez, Pablo Ruiz Canovas
 */
@RestController
@RequestMapping("/horarios/courses")
@Slf4j
public class TimetableRestCourses {
	

	/** Clase que se encarga de las operaciones logicas del servidor */
	@Autowired
	TimeTableUtils util;
	
	@Autowired
	ApplicationPdf applicationPdf;

	// --------------------------- JAYDEE

	@Autowired
	private IGrupoRepository grupoRepo;

	@Autowired
	private IAulaRepository aulaRepo;


	/**
	 * Method getListCourse
	 *
	 * @param session
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/courses", produces = "application/json")
	public ResponseEntity<?> getListCourse()

	{
		List<Course> listaCurso = new ArrayList<>();
		Course curso;
		Classroom classroom;
		List<Aula> listaAula = new ArrayList<>();
		try
		{
			// -- Recupera un listado de aulas (dto) de la base de datos.
			listaAula = aulaRepo.recuperaListadoAulas();

			// -- FOR EACH AULA IN listAula ---
			for (int i = 0; i < listaAula.size(); i++)
			{
				if (listaAula.get(i).getAbreviatura().isEmpty() || (listaAula.get(i).getAbreviatura() == null))
				{
					continue;
				}

				String nombreAula = listaAula.get(i).getNombre();

				String[] plantaAula = listaAula.get(i).getAbreviatura().split("\\.");

				String plantaNumero = "";

				String numeroAula = "";

				// -- THE VALUES WITH CHARACTERS ONLY HAVE 1 POSITION ---
				if (plantaAula.length > 1)
				{
					plantaNumero = plantaAula[0].trim();
					numeroAula = plantaAula[1].trim();
				} else
				{
					plantaNumero = plantaAula[0].trim();
					numeroAula = plantaAula[0].trim();
				}

				// -- IMPORTANT , CLASSROOM PLANTANUMERO AND NUMEROAULA , CHANGED TO STRING
				// BECAUSE SOME PARAMETERS CONTAINS CHARACTERS ---
				classroom = new Classroom(plantaNumero, numeroAula);
				curso = new Course(nombreAula, classroom);
				listaCurso.add(curso);
			}

		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}
		return ResponseEntity.ok().body(listaCurso);
	}
	

	/**
	 * Method getClassroomCourse
	 *
	 * @param courseName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/classroomcourse", produces = "application/json")
	public ResponseEntity<?> getClassroomCourse(@RequestParam(required = true) String courseName)
	{
		try
		{
			// --- CHECKING IF THE COURSE NAME IS NOT BLANK AND NOT EMPTY ---
			if (!courseName.isBlank() && !courseName.isEmpty())
			{
				// Inicializa Aula a nulo.
				Aula aula = null;

				log.debug("Aula buscada: {}", courseName);
				Optional<AulaEntity> aulaEntityOpt = aulaRepo.findByNombre(courseName);

				if (aulaEntityOpt.isPresent())
				{

					aula = new Aula();
					aula.setAbreviatura(aulaEntityOpt.get().getAbreviatura());
					aula.setNombre(aulaEntityOpt.get().getNombre());
					aula.setNumIntAu(aulaEntityOpt.get().getNumIntAu());
					log.debug("Entidad Aula buscada recuperada con exito. {}", aula.getNombre());
				}

				// Si el proceso ha encontrado un aula y ya no es nula
				if (aula != null)
				{
					String nombreAula = aula.getNombre();

					// --- SPLIT BY '.' ---
					String[] plantaAula = aula.getAbreviatura().split("\\.");

					String plantaNumero = "";
					String numeroAula = "";
					// -- THE VALUES WITH CHARACTERS ONLY HAVE 1 POSITION ---
					if (plantaAula.length > 1)
					{
						plantaNumero = plantaAula[0].trim();
						numeroAula = plantaAula[1].trim();
					}
					else
					{
						plantaNumero = plantaAula[0].trim();
						numeroAula = plantaAula[0].trim();
					}

					// -- IMPORTANT , CLASSROOM PLANTANUMERO AND NUMEROAULA , CHANGED TO STRING
					// BECAUSE SOME PARAMETERS CONTAINS CHARACTERS ---
					Classroom classroom = new Classroom(numeroAula, plantaNumero, nombreAula);

					// --- RETURN FINALLY THE CLASSROOM ---
					return ResponseEntity.ok(classroom);

				}
				else
				{
					// --- ERROR ---
					String error = "ERROR AULA NOT FOUND OR NULL";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}

			}
			else
			{
				// --- ERROR ---
				String error = "ERROR HEADER COURSE NAME EMPTY OR BLANK";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError);
			}
		}
		catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError);
		}
	}
	
	@RequestMapping(value = "/get/coursenames", produces = "application/json")
	public ResponseEntity<?> getCourseNames()

	{
		try
		{
			// Crea una lista vacia de grupos.
			List<Grupo> grupos = this.grupoRepo.recuperaGruposDeParseo();

			return ResponseEntity.ok().body(this.util.ordenarLista(grupos));
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR --
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			// -- RETURN A SERVER ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 500
			// (INTERNAL SERVER ERROR) --
			return ResponseEntity.status(500).body(horariosError);
		}
	}

}
