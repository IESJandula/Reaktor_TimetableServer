package es.iesjandula.reaktor.timetable_server.rest;
import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.Classroom;
import es.iesjandula.reaktor.timetable_server.models.Hour;
import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.TeacherMoment;
import es.iesjandula.reaktor.timetable_server.models.entities.ActividadEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AsignaturaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaPlanoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlotEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Actividad;
import es.iesjandula.reaktor.timetable_server.models.parse.Asignatura;
import es.iesjandula.reaktor.timetable_server.models.parse.Aula;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioProf;
import es.iesjandula.reaktor.timetable_server.models.parse.Profesor;
import es.iesjandula.reaktor.timetable_server.models.parse.TimeSlot;
import es.iesjandula.reaktor.timetable_server.repository.IActitudePointsRepository;
import es.iesjandula.reaktor.timetable_server.repository.IActividadRepository;
import es.iesjandula.reaktor.timetable_server.repository.IAsignaturaRepository;
import es.iesjandula.reaktor.timetable_server.repository.IAulaPlanoRepository;
import es.iesjandula.reaktor.timetable_server.repository.IAulaRepository;
import es.iesjandula.reaktor.timetable_server.repository.IGrupoRepository;
import es.iesjandula.reaktor.timetable_server.repository.IInfoErrorRepository;
import es.iesjandula.reaktor.timetable_server.repository.IProfesorRepository;
import es.iesjandula.reaktor.timetable_server.repository.IStudentsRepository;
import es.iesjandula.reaktor.timetable_server.repository.ITimeSlotRepository;
import es.iesjandula.reaktor.timetable_server.utils.ApplicationPdf;
import es.iesjandula.reaktor.timetable_server.utils.JPAOperations;
import es.iesjandula.reaktor.timetable_server.utils.StudentOperation;
import es.iesjandula.reaktor.timetable_server.utils.TimeTableUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author David Martinez, Pablo Ruiz Canovas
 */
@RestController
@RequestMapping("/horarios/teachers")
@Slf4j
public class TimetableRestTeachers {

	/** Clase que se encarga de las operaciones logicas del servidor */
	@Autowired
	TimeTableUtils util;
	
	@Autowired
	ApplicationPdf applicationPdf;

	// --------------------------- JAYDEE

	@Autowired
	private IProfesorRepository profesorRepo;

	@Autowired
	private ITimeSlotRepository timeslotRepo;

	@Autowired
	private IActividadRepository actividadRepo;

	/**
	 * Recupera un listado de profesoresDTO a partir de una llamada al repositorio.
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/get/teachers", produces = "application/json")
	public ResponseEntity<?> getProfesores()

	{
		try
		{
			List<Profesor> profesores = this.profesorRepo.recuperaListadoProfesores();
			// Devuelve un listado ordenado de profesores.
			return ResponseEntity.ok().body(this.util.ordenarLista(profesores));

		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}
	

	/**
	 * Method getClassroomTeacher
	 *
	 * @param name
	 * @param lastname
	 * @return
	 */
	@RequestMapping(value = "/teacher/get/classroom", produces = "application/json")
	public ResponseEntity<?> getClassroomTeacher(@RequestParam(required = true) String name,
			@RequestParam(required = true) String lastname)
	{
		try
		{ 

			// Si el nombre y apellidos no están en blanco.
			if (!name.isEmpty() && !name.isBlank() && !lastname.isBlank() && !lastname.isEmpty())
			{
				// Recoge el parametro y lo separa en campos.
				String[] apellidos = lastname.trim().split(" ");

				String nombreProfesor = name;
				String primerApellido = apellidos[0]; // Asigna el primer apellido retomando el primer campo.
				String segundoApellido = apellidos[1]; // Asigna el segundo apellido retomando el segundo campo.

				// Recupera al objeto referente al docente buscado.
				Optional<Profesor> profesorOpt = this.profesorRepo.buscaProfesorPorNombreYApellidos(nombreProfesor,
						primerApellido, segundoApellido);

				if (profesorOpt.isEmpty())
				{
					// --- ERROR ---
					String error = "Error on search : Professor not found.";
					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError.toMap());
				}

				// PROFESOR
				Profesor profesor = profesorOpt.get();
				log.info(" - - - - Profesor encontrado {}", profesor.getAbreviatura());

				// HORA ACTUAL
				String currentTime = LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute();
				log.info(" - - - - Hora recuperada {}.", currentTime);

				// TRAMO RELATIVO HORA ACTUAL

				TimeSlot profTramo = this.gettingTramoActual(currentTime);
				
				// Devuelve error si buscamos un profesor trabajando fuera de dias semanales.
				if( profTramo == null ){
					// --- ERROR ---
					String error = "Tramo horario excedido.";
					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError.toMap()); 
				}
				
				log.info(" - - - - Tramo recuperado {}.", profTramo.toString());
				
				
				// Busca una actividad basandose en un tramo horario y en un profesor. (Un
				// profesor solo puede estar en una actividad en un momento dado.)
				Optional<ActividadEntity> actividadProfesor = this.actividadRepo
						.buscaActividadEntityPorTramoYProfesor(profTramo.getNumTr(), profesor.getNumIntPR());

				if (actividadProfesor.isEmpty())
				{
					// --- ERROR ---
					String error = "No activity for searched professor";
					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError.toMap());
				}

				// Recupera el aula relacionada a la actividad del profesor.
				Aula profAula = new Aula();

				profAula.setAbreviatura(actividadProfesor.get().getAula().getAbreviatura());
				profAula.setNombre(actividadProfesor.get().getAula().getNombre());
				profAula.setNumIntAu(actividadProfesor.get().getAula().getNumIntAu());

				log.info(" - - - - Aula recuperada: {}", profAula.getAbreviatura());

				Asignatura asignatura = new Asignatura(actividadProfesor.get().getAsignatura());
				log.info(" - - - - Asignatura recuperada: {}", asignatura.getAbreviatura());

				log.info("AULA ACTUAL PROFESOR: " + profesor + "\n" + profAula);
				String nombreAula = profAula.getNombre();

				String[] plantaAula = profAula.getAbreviatura().split("\\.");
				log.debug(" - - - - Planta Aula : {}", plantaAula.toString());

				String plantaNumero = "";
				log.debug(" - - - - Planta Numero : {}", plantaNumero);

				String numeroAula = "";
				log.debug(" - - - - Numero Aula : {}", numeroAula);

				// -- THE VALUES WITH CHARACTERS ONLY HAVE 1 POSITION ---
				if (plantaAula.length > 1)
				{
					plantaNumero = plantaAula[0].trim();
					numeroAula = plantaAula[1].trim();
				} else
				{
					plantaNumero = plantaAula[0].trim();
					numeroAula = plantaAula[0].trim();
					if (plantaNumero.isEmpty() || numeroAula.isEmpty())
					{
						plantaNumero = nombreAula;
						numeroAula = nombreAula;
					}
				}

				Map<String, Object> mapa = new HashMap<String, Object>();
				Classroom classroom = new Classroom(numeroAula, plantaNumero, profAula.getNombre());
				mapa.put("classroom", classroom);
				mapa.put("subject", asignatura);
				log.info(mapa.toString());
				return ResponseEntity.ok().body(mapa);
			}
			// --- ERROR ---
			String error = "Error on parameters from header";
			HorariosError horariosError = new HorariosError(500, error, null);
			log.info(error, horariosError);
			return ResponseEntity.status(400).body(horariosError);

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

	/**
	 * Method getClassroomTeacher
	 *
	 * @param name
	 * @param lastname
	 * @return
	 */
	@RequestMapping(value = "/teacher/get/classroom/tramo", produces = "application/json", consumes = "application/json")
	public ResponseEntity<?> getClassroomTeacherSchedule(@RequestParam(required = true) String name,
			@RequestParam(required = true) String lastname, @RequestBody(required = true) TimeSlot profTime)
	{
		try
		{
			log.info(profTime.toString());

			// Si el nombre y el apellido NO SON NULOS.
			if (!name.isEmpty() && !name.isBlank() && !lastname.isBlank() && !lastname.isEmpty())
			{

				String nombre = name;
				String apellidos[] = lastname.trim().split(" ");
				String apellido1 = apellidos[0];
				String apellido2 = apellidos[1];

				Optional<Profesor> profesorOpt = this.profesorRepo.buscaProfesorPorNombreYApellidos(nombre, apellido1,
						apellido2);

				if (profesorOpt.isEmpty())
				{
					// alzar error profesor no encontrado.

				}

				Profesor prof = profesorOpt.get();
				log.info("Profesor recuperado: {}", prof.getAbreviatura());

				// Recupera una actividad filtrando por tramo y profesor pasados por parametro.
				Optional<ActividadEntity> actividadTramoProfesor = this.actividadRepo
						.buscaActividadEntityPorTramoYProfesor(profTime.getNumTr().trim(), prof.getNumIntPR());

				if (actividadTramoProfesor.isEmpty())
				{
					// lanzar error de que no tiene cosas ccon esa hora.
					log.info("EL TRAMO " + profTime + "\nNO EXISTE EN LAS ACTIVIDADES DEL PROFESOR " + prof);
					// --- ERROR ---
					String error = "EL TRAMO " + profTime + "\nNO EXISTE EN LAS ACTIVIDADES DEL PROFESOR " + prof;
					HorariosError horariosError = new HorariosError(500, error, null);
					log.info(error, horariosError);
					return ResponseEntity.ok().body("El profesor en el tramo " + profTime.getStartHour() + " - "
							+ profTime.getEndHour() + " no se encuentra en ningun aula");
				}

				AulaEntity aulaEntity = actividadTramoProfesor.get().getAula();
				AsignaturaEntity asignaturaEntity = actividadTramoProfesor.get().getAsignatura();

				Aula aulaProfe = new Aula();
				// sete atributos aula
				aulaProfe.setAbreviatura(aulaEntity.getAbreviatura());
				aulaProfe.setNombre(aulaEntity.getNombre());
				aulaProfe.setNumIntAu(aulaEntity.getNumIntAu());

				Asignatura asignaturaProfe = new Asignatura();
				// setea atributos asignatura.
				asignaturaProfe.setAbreviatura(asignaturaEntity.getAbreviatura());
				asignaturaProfe.setNombre(asignaturaEntity.getNombre());
				asignaturaProfe.setNumIntAs(asignaturaEntity.getNumIntAs());

				// Si aula profe es nula.
				if (aulaProfe != null)
				{
					log.info("AULA ACTUAL PROFESOR: " + prof + "\n" + aulaProfe);
					String nombreAula = aulaProfe.getNombre();

					String[] plantaAula = aulaProfe.getAbreviatura().split("\\.");
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
						if (plantaNumero.isEmpty() || numeroAula.isEmpty())
						{
							plantaNumero = nombreAula;
							numeroAula = nombreAula;
						}
					}

					Map<String, Object> mapa = new HashMap<String, Object>();
					Classroom classroom = new Classroom(numeroAula, plantaNumero, aulaProfe.getNombre());
					mapa.put("classroom", classroom);
					mapa.put("subject", asignaturaProfe);
					log.info(mapa.toString());

					return ResponseEntity.ok().body(mapa); // respuesta del metodo.
				}
			}
			// --- ERROR ---
			String error = "Error on parameters from header";
			HorariosError horariosError = new HorariosError(500, error, null);
			log.info(error, horariosError);
			return ResponseEntity.status(400).body(horariosError);
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
	

	/**
	 * Method getTeacherSubject
	 *
	 * @param courseName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/teachersubject", produces = "application/json")
	public ResponseEntity<?> getTeacherSubject(@RequestParam(required = true) String courseName)

	{
		try
		{
			if (courseName.isBlank())
			{
				String error = "ERROR , CURSO EN BLANCO O NO PERMITIDO";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError.toMap());
			}
			
			// Getting the actual time
			String actualTime = LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute();
			log.info(actualTime);

			TimeSlot tramoActual = this.gettingTramoActual(actualTime);
			
			List<TeacherMoment> teacherMoments = this.actividadRepo.findTeacherMomentsByParameters(courseName, tramoActual.getDayNumber(), tramoActual.getStartHour(), tramoActual.getEndHour()) ;
			
			return ResponseEntity.ok(teacherMoments) ;
		}
		catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.info(error, horariosError);
			return ResponseEntity.status(500).body(horariosError.toMap());
		}
	}
	

	@RequestMapping(value = "/get/tramos", produces = "application/json")
	public ResponseEntity<?> getNumTramos()
	{
		try
		{
			List<TimeSlot> tramos = this.timeslotRepo.recuperaListadoTramosHorarios();
			return ResponseEntity.ok().body(tramos);
		}
		catch (Exception exception)
		{
			String message = "Error de servidor, no se encuentran datos de los tramos";
			log.error(message, exception);
			HorariosError error = new HorariosError(500, message, exception);
			return ResponseEntity.status(500).body(error.toMap());
		}
	}
	

	/**
	 * Method getListHours
	 *
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/hours", produces = "application/json")
	public ResponseEntity<?> getListHours()
	{
		try
		{

			List<Hour> hourList = new ArrayList<>();

			// Recupera un listado de tramos perteneciente al primer dia semanal (lunes)
			// Esto recupera un total de 7 tramos horarios.
			List<TimeSlotEntity> tramos = this.timeslotRepo.findByDayNumber("1");

			// Por cada tramo en el listado.
			for (TimeSlotEntity tramo : tramos)
			{

				// --- GETTING THE HOURNAME BY THE ID OF THE TRAMO 1-7 (1,2,3,R,4,5,6) ---
				String hourName = "";
				switch (tramo.getNumTr().trim())
				{
				case "1":
				{
					hourName = "primera";
					break;
				}
				case "2":
				{
					hourName = "segunda";
					break;
				}
				case "3":
				{
					hourName = "tercera";
					break;
				}
				case "4":
				{
					hourName = "recreo";
					break;
				}
				case "5":
				{
					hourName = "cuarta";
					break;
				}
				case "6":
				{
					hourName = "quinta";
					break;
				}
				case "7":
				{
					hourName = "sexta";
					break;
				}
				default:
				{
					// --- DEFAULT ---
					hourName = "Desconocido";
					break;
				}
				}
				// --- ADD THE INFO OF THE TRAMO ON HOUR OBJECT ---
				hourList.add(new Hour(hourName, tramo.getStartHour().trim(), tramo.getEndHour().trim()));
			}
			// --- RESPONSE WITH THE HOURLIST ---
			return ResponseEntity.ok(hourList);

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
	

	/**
	 * Method getTeachersSchedule
	 *
	 * @return ResponseEntity , File PDF
	 */
	@RequestMapping(value = "/get/teachers/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getTeachersSchedule()
	{
		try
		{
			Map<ProfesorEntity, Map<String, List<Actividad>>> mapProfesors = new HashMap<>();

				// --- CENTRO PDF IS LOADED---
				for (ProfesorEntity profesor : this.profesorRepo.findAll())
				{
					// --- FOR EACH PROFESOR ---
					HorarioProf horarioProfesor = null;
					List<HorarioProf> horarioProfList = new ArrayList<>();
					fillHorarioProfValues(horarioProfList);
					for (HorarioProf horarioPrf : horarioProfList)
					{
						if (horarioPrf.getHorNumIntPR().trim().equalsIgnoreCase(profesor.getNumIntPR().trim()))
						{
							horarioProfesor = horarioPrf;
						}
					}

					if (horarioProfesor != null)
					{
						// --- HORARIO PROF EXISTS ---

						// --- FOR EACH ACTIVIDAD ---
						Map<String, List<Actividad>> mapProfesor = new HashMap<>();
						for (Actividad atcv : horarioProfesor.getActividad())
						{
							TimeSlotEntity temporalTramo = this.extractTramoFromCentroActividad( atcv);

							if (!mapProfesor.containsKey(temporalTramo.getDayNumber().trim()))
							{
								List<Actividad> temporalList = new ArrayList<>();
								temporalList.add(atcv);
								mapProfesor.put(temporalTramo.getDayNumber().trim(), temporalList);
							} else
							{
								List<Actividad> temporalList = mapProfesor.get(temporalTramo.getDayNumber().trim());
								temporalList.add(atcv);
								mapProfesor.put(temporalTramo.getDayNumber().trim(), temporalList);
							}
						}

						// --- ADD THE PROFESSOR WITH THE PROFESSOR MAP ---
						mapProfesors.put(profesor, mapProfesor);
					} else
					{
						log.error("ERROR profesor " + profesor + " HORARIO PROF NOT FOUND OR NULL");
					}
				}

				try
				{
					// --- USING APPLICATION PDF TO GENERATE THE PDF , WITH ALL TEACHERS ---
					applicationPdf.getAllTeachersPdfInfo(mapProfesors);

					// --- GETTING THE PDF BY NAME URL ---
					File file = new File("All_Teachers_Horarios.pdf");

					// --- SETTING THE HEADERS WITH THE NAME OF THE FILE TO DOWLOAD PDF ---
					HttpHeaders responseHeaders = new HttpHeaders();
					// --- SET THE HEADERS ---
					responseHeaders.set("Content-Disposition", "attachment; filename=" + file.getName());

					try
					{
						// --- CONVERT FILE TO BYTE[] ---
						byte[] bytesArray = Files.readAllBytes(file.toPath());

						// --- RETURN OK (200) WITH THE HEADERS AND THE BYTESARRAY ---
						return ResponseEntity.ok().headers(responseHeaders).body(bytesArray);
					} catch (IOException exception)
					{
						// --- ERROR ---
						String error = "ERROR GETTING THE BYTES OF PDF ";

						log.info(error);

						HorariosError horariosError = new HorariosError(500, error, exception);
						log.info(error, horariosError);
						return ResponseEntity.status(500).body(horariosError);
					}
				} catch (HorariosError exception)
				{
					// --- ERROR ---
					String error = "ERROR getting the info pdf ";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, exception);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}
			
		} catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}
	
	//-----methods-------
	
	/**
	 * Method gettingTramoActual
	 *
	 * @param centro
	 * @param actualTime
	 * @param tramoActual
	 * @return
	 */
	private TimeSlot gettingTramoActual(String actualTime )
	{
		TimeSlot tramoActual = null;
		// Recupera listado de tramos de BBDD
		List<TimeSlot> tramosLista = timeslotRepo.recuperaListadoTramosHorarios();

		for (TimeSlot tramo : tramosLista)
		{

			// --- GETTING THE HORA,MINUTO , INICIO AND FIN ---
			int horaInicio = Integer.parseInt(tramo.getStartHour().split(":")[0].trim());
			int minutoInicio = Integer.parseInt(tramo.getStartHour().split(":")[1].trim());

			int horaFin = Integer.parseInt(tramo.getEndHour().split(":")[0].trim());
			int minutoFin = Integer.parseInt(tramo.getEndHour().split(":")[1].trim());

			// --- GETTING THE HORA, MINUTO ACTUAL ---
			int horaActual = Integer.parseInt(actualTime.split(":")[0].trim());
			int minutoActual = Integer.parseInt(actualTime.split(":")[1].trim());

			// --- USE CALENDAR INSTANCE FOR GET INTEGER WITH THE NUMBER OF THE DAY ON THE
			// WEEK ---
			Calendar calendar = Calendar.getInstance();
			// --- PARSIN CALENDAR DAY_OF_WEK TO NUMBER -1 (-1 BECAUSE THIS START ON
			// SUNDAY)--
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

			// --- IF DAY IS 0 , IS 7 , BACUSE IS SUNDAY ---
			if (dayOfWeek == 0)
			{
				dayOfWeek = 7;
			}
			if (dayOfWeek >= 6)
			{
				log.warn("DIA EXCEDIDO: (6:SABADO-7:DOMINGO) -> " + dayOfWeek);
			}

			// --- DAY OF TRAMO ---
			if (Integer.parseInt(tramo.getDayNumber()) == dayOfWeek)
			{
				// --- IF HORA ACTUAL EQUALS HORA INICIO ---
				if (horaActual == horaInicio)
				{
					// --- CHEKING IF THE MINUTO ACTUAL IS GREATER THAN THE MINUTO INICIO AND
					// HORA ACTUAL LESS THAN HORA FIN ---
					if ((minutoActual >= minutoInicio) && (horaActual <= horaFin))
					{
						// --- SETTING THE VALUE OF TRAMO INTO PROF TRAMO ---
						log.info("ENCONTRADO -> " + tramo);
						tramoActual = tramo;

					}
				}
				// --- IF HORA ACTUAL EQUALS HORA FIN ---
				else if (horaActual == horaFin)
				{
					// --- CHEKING IF THE MINUTO ACTUAL IS LESS THAN MINUTO FIN ---
					if (minutoActual <= minutoFin)
					{
						// --- SETTING THE VALUE OF TRAMO INTO PROF TRAMO ---
						log.info("ENCONTRADO -> " + tramo);
						tramoActual = tramo;

					}
				}
			}
		}
		return tramoActual;
	}
	

	public void fillHorarioProfValuesById(List<HorarioProf> listadoHorarios, String profesorId)
	{

		// List<Actividad> actividadesList = actividadRepo.recuperaListadoActividades();
		List<Actividad> actividadesList = actividadRepo.recuperaListadoActividadesPorIdProfesor(profesorId);

		String totAc = String.valueOf(actividadesList.size());

		// Crea nuevo horario profesor.
		HorarioProf horarioProfe = new HorarioProf();

		// Rellena datos.
		horarioProfe.setActividad(actividadesList);
		horarioProfe.setHorNumIntPR(profesorId);
		horarioProfe.setTotUn("0"); // Revisar.
		horarioProfe.setTotAC(totAc);

		// Agrega el horario del profesor al listado.
		listadoHorarios.add(horarioProfe);

	}
	
	public void fillHorarioProfValues(List<HorarioProf> listadoHorarios)
	{
		// Por cada profesor en el listado de profesores almacenado en bbdd.
		for (ProfesorEntity profe : this.profesorRepo.findAll())
		{

			List<Actividad> actividadesList = actividadRepo.recuperaListadoActividades();
			String totAc = String.valueOf(actividadesList.size());

			// Crea nuevo horario profesor.
			HorarioProf horarioProfe = new HorarioProf();

			// Rellena datos.
			horarioProfe.setActividad(actividadesList);
			horarioProfe.setHorNumIntPR(profe.getNumIntPR());
			horarioProfe.setTotUn("0"); // Revisar.
			horarioProfe.setTotAC(totAc);

			// Agrega el horario del profesor al listado.
			listadoHorarios.add(horarioProfe);
		}
	}
	
	/**
	 * Method extractTramoFromCentroActividad
	 *
	 * @param centro
	 * @param actividad
	 * @param tramo
	 * @return
	 */
	private TimeSlotEntity extractTramoFromCentroActividad( Actividad actividad)
	{
		for (TimeSlotEntity tram : this.timeslotRepo.findAll())
		{
			// --- GETTING THE TRAMO ---
			if (actividad.getTramo().trim().equalsIgnoreCase(tram.getNumTr().trim()))
			{
				return tram;
			}
		}
		return null;
	}
	

}
