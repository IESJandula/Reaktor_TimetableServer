package es.iesjandula.reaktor.timetable_server.rest;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.ActitudePoints;
import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;
import es.iesjandula.reaktor.timetable_server.repository.IActitudePointsRepository;
import es.iesjandula.reaktor.timetable_server.repository.IStudentsRepository;
import es.iesjandula.reaktor.timetable_server.utils.ApplicationPdf;
import es.iesjandula.reaktor.timetable_server.utils.JPAOperations;
import es.iesjandula.reaktor.timetable_server.utils.StudentOperation;
import es.iesjandula.reaktor.timetable_server.utils.TimeTableUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author David Martinez, Pablo Ruiz Canovas
 */
@RestController
@RequestMapping("/horarios/coexistence")
@Slf4j
public class TimetableRestCoexistence {
	

	/** Clase que se encarga de las operaciones logicas del servidor */
	@Autowired
	TimeTableUtils util;
	
	@Autowired
	ApplicationPdf applicationPdf;

	/** Clase que se encarga de manejar las operaciones con la base de datos */
	@Autowired
	private JPAOperations operations;
	
	@Autowired
	private IStudentsRepository iStudentsRepository;









	// --------------------------- JAYDEE


	// ---------------Este es para getListPointsCoexistence
	@Autowired
	private IActitudePointsRepository iActitudePointsRepo;

	
	@RequestMapping(method = RequestMethod.POST, value = "/send/sancion", consumes = "application/json")
	public ResponseEntity<?> sendSancion(@RequestBody(required = true) Student student,


			@RequestParam(name = "value", required = true) Integer value,
			@RequestParam(name = "description", required = true) String description)
	{
		try
		{
			Optional<StudentsEntity> studentsEntityOpt = this.iStudentsRepository.findByNameAndLastNameAndCourse(student.getName(), student.getLastName(), student.getCourse()) ;
			
			// TODO si no existe, devolver error

			ActitudePoints points = new ActitudePoints(value, description);

			
			List<Integer> valores = this.iActitudePointsRepo.findAllPuntosConvivenciaValores();
			List<String> descripciones = this.iActitudePointsRepo.findAllPuntosConvivenciaDescripciones();


			List<ActitudePoints> puntos = new ArrayList<>();


			for (int i = 0; i < valores.size(); i++) {
				puntos.add(new ActitudePoints(valores.get(i), descripciones.get(i)));
			}

			// Busqueda de puntos
			if (!puntos.contains(points))
			{
				throw new HorariosError(404, "Los puntos proporcionados no se encuentran en los datos generales");
			}

			// Guardamos la sancion en la base de datos
			this.operations.ponerSancion(studentsEntityOpt.get(), points);

			return ResponseEntity.ok().build();
		} catch (HorariosError exception)
		{
			log.error("Los datos proporcionados no son correctos", exception);
			return ResponseEntity.status(exception.getCode()).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}
}
