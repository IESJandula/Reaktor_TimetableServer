package es.iesjandula.reaktor.timetable_server.rest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.ActitudePoints;
import es.iesjandula.reaktor.timetable_server.models.Classroom;
import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.Teacher;
import es.iesjandula.reaktor.timetable_server.models.TeacherMoment;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaPlanoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Actividad;
import es.iesjandula.reaktor.timetable_server.models.parse.Aula;
import es.iesjandula.reaktor.timetable_server.models.parse.Centro;
import es.iesjandula.reaktor.timetable_server.models.parse.Grupo;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioGrup;
import es.iesjandula.reaktor.timetable_server.models.parse.Profesor;
import es.iesjandula.reaktor.timetable_server.models.parse.TimeSlot;
import es.iesjandula.reaktor.timetable_server.repository.IActitudePointsRepository;
import es.iesjandula.reaktor.timetable_server.repository.IGrupoRepository;
import es.iesjandula.reaktor.timetable_server.repository.IStudentsRepository;
import es.iesjandula.reaktor.timetable_server.utils.ApplicationPdf;
import es.iesjandula.reaktor.timetable_server.utils.JPAOperations;
import es.iesjandula.reaktor.timetable_server.utils.StudentOperation;
import es.iesjandula.reaktor.timetable_server.utils.TimeTableUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * @author David Martinez, Pablo Ruiz Canovas
 */
@RestController
@RequestMapping("/horarios/students")
@Slf4j
public class TimetableRestStudents {
	

	/** Clase que se encarga de las operaciones logicas del servidor */
	@Autowired
	TimeTableUtils util;
	
	@Autowired
	ApplicationPdf applicationPdf;

	/** Clase que se encarga de gestionar las operaciones con los estudiantes */
	private StudentOperation studentOperation;

	/** Clase que se encarga de manejar las operaciones con la base de datos */
	@Autowired
	private JPAOperations operations;
	
	@Autowired
	private IStudentsRepository iStudentsRepository;

	/** Lista de estudiantes cargados por csv */
	private List<Student> students;
	
	/** Lista de los planos de las aulas */
	private List<AulaPlanoEntity> aulas;








	// --------------------------- JAYDEE


	@Autowired
	private IGrupoRepository grupoRepo;



	// ---------------Este es para getListPointsCoexistence
	@Autowired
	private IActitudePointsRepository iActitudePointsRepo;


	@Autowired
	private IStudentsRepository iStudentsRepo;


	public TimetableRestStudents()
	{

		this.studentOperation = new StudentOperation();
		this.students = new LinkedList<Student>();
	}
	
	/**
	 * Method getListStudentsAlphabetically
	 *
	 * @return
	 */
	@RequestMapping(value = "/get/sortstudents", produces = "application/json")
	public ResponseEntity<?> getListStudentsAlphabetically()
	{
		try
		{
			List <Student> listadoEstudiantes = iStudentsRepo.recuperaListadoEstudiantes();
			if (listadoEstudiantes.isEmpty())
			{
				HorariosError error = new HorariosError(400, "No se han cargado estudiantes");
				return ResponseEntity.status(404).body(error.toMap());
			}

			return ResponseEntity.ok().body(this.util.ordenarLista(listadoEstudiantes));

		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR ---
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}
	
	/**
	 * @author MANU
	 * @param name
	 * @param lastname
	 * @param fechaInicio
	 * @param fechaEnd
	 * @param session
	 * @return
	 */
	@Autowired
	IStudentsRepository iStudentRepository;
	@RequestMapping(value = "/get/veces/visitado/studentFechas", produces = "application/json")
	public ResponseEntity<?> getNumberVisitsBathroom(@RequestParam(required = true, name = "name") String name,
			@RequestParam(required = true, name = "lastName") String lastname,
			@RequestParam(required = true, name = "course") String course,
			@RequestParam(required = true, name = "fechaInicio") String fechaInicio,
			@RequestParam(required = true, name = "fechaFin") String fechaEnd)
	{
		try
		{
			// Obtenemos el estudiante por su nombre apellido y curso
			Optional<StudentsEntity> studentEntidad = iStudentRepository.findByNameAndLastNameAndCourse(name, lastname, course);
			studentEntidad.orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
			
			List<Map<String, String>> visitasAlumno = this.operations.getVisitaAlumno(studentEntidad.get(), fechaInicio, fechaEnd);

			// Establecemos dos tipos de respuesta, una correcta si la lista contiene datos
			// y un error en caso contrario
			ResponseEntity<?> respuesta = !visitasAlumno.isEmpty() ? ResponseEntity.ok().body(visitasAlumno)
					: ResponseEntity.status(404).body(
							"El alumno no ha ido en el periodo " + fechaInicio + " - " + fechaEnd + " al servicio");

			// Devolvemos una de las dos respuestas
			return respuesta;

		}
		catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}
	

	/**
	 * Metodo que devuelve el numero de visitas realizadas por un alumno al servicio
	 * (solo cuenta aquellas en las que la fecha de vuelta no sea nula)
	 * 
	 * @param name
	 * @param lastname
	 * @param course
	 * @return numero de veces que ha ido al servicio
	 */
	@RequestMapping(value = "/get/student/numero-veces-servicio", produces = "application/json")
	public ResponseEntity<?> getDayHourBathroom(@RequestParam(required = true, name = "name") String name,
			@RequestParam(required = true, name = "lastname") String lastname,
			@RequestParam(required = true, name = "course") String course)
	{
		try
		{
			// Obtenemos el estudiante por su nombre apellido y curso
			Optional<StudentsEntity> studentsEntity = this.iStudentsRepository.findByNameAndLastNameAndCourse(name, lastname, course);

			// Obtenemos el numero de vecew que ha ido y vuelto del servicio
			int numVecesBathroom = this.operations.obtenerNumeroVecesServicio(studentsEntity.get());

			return ResponseEntity.ok().body(numVecesBathroom);
		}
		catch (Exception exception)
		{
			String error = "Error en el servidor";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError.toMap());
		}
	}
	



	/**
	 * Metodo que registra una visita al baño en la base de datos por parte de un
	 * alumno
	 * 
	 * @param name
	 * @param lastname
	 * @param course
	 * @return ok si todo ha ido bien, error si los parametros fallan o surge un
	 *         error de servidor
	 */
	@RequestMapping("/student/visita/bathroom")
	public ResponseEntity<?> postVisit(@RequestParam(required = true, name = "name") String name,
			@RequestParam(required = true, name = "lastName") String lastname,
			@RequestParam(required = true, name = "course") String course)
	{
		try
		{
			//Buscamos el estudiante
			Optional<StudentsEntity> studentEntityOpt = this.iStudentsRepo.findByNameAndLastNameAndCourse(name, lastname, course);
            log.info("Estudiante recuperado: {}", studentEntityOpt.toString());
			//En caso de que no haya ido al baño se anota
			this.operations.comprobarVisita(studentEntityOpt.get());

			// Si no hay error devolvemos que todo ha ido bien
			return ResponseEntity.ok().body("Salida al baño marcada con éxito.");
		}
		catch (HorariosError exception)
		{
			log.error("Error al registrar la ida de un estudiante", exception);
			return ResponseEntity.status(404).body(exception.toMap());
		}
		catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}

	/**
	 * @author MANU
	 * @param name
	 * @param lastname
	 * @param course
	 * @param session
	 * @return
	 */
	@RequestMapping("/student/regreso/bathroom")
	public ResponseEntity<?> postReturnBathroom(@RequestParam(required = true, name = "name") String name,
			@RequestParam(required = true, name = "lastName") String lastname,
			@RequestParam(required = true, name = "course") String course)
	{
		try
		{
			// Buscamos el estudiante
			Optional<StudentsEntity> optionalStudentEntity = iStudentsRepo.findByNameAndLastNameAndCourse(name, lastname, course) ;
			// Bsucar estudiante en base de datos.

			if (optionalStudentEntity.isEmpty())
			{
				throw new HorariosError(400, "El estudiante no resulta registrado en bases de datos.");
			}
			// En caso de que haya ido al baño se anota si esta, en caso de que no hay ido
			// se manda un error
			this.operations.comprobarVuelta(optionalStudentEntity.get());
			// Si no hay error devolvemos que todo ha ido bien
			return ResponseEntity.ok().build();
		}
		catch (HorariosError exception)
		{
			log.error("Error al registrar la vuelta de un estudiante", exception);
			return ResponseEntity.status(404).body(exception.toMap());
		}
		catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}



	/**
	 * @author MANU
	 * @param fechaInicio
	 * @param fechaEnd
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/get/students/visitas/bathroom", produces = "application/json")
	public ResponseEntity<?> getListTimesBathroom(
			@RequestParam(required = true, name = "fechaInicio") String fechaInicio,
			@RequestParam(required = true, name = "fechaFin") String fechaEnd)
	{
		try
		{
			List<Map<String, Object>> visitas = this.operations.getVisitasAlumnos(fechaInicio, fechaEnd);

			// Establecemos dos tipos de respuesta, una correcta si la lista contiene datos
			// y un error en caso contrario
			ResponseEntity<?> respuesta = !visitas.isEmpty() ? ResponseEntity.ok().body(visitas)
					: ResponseEntity.status(404)
							.body("No hay alumnos en el periodo " + fechaInicio + " - " + fechaEnd + " al servicio");

			// Devolvemos una de las dos respuestas
			return respuesta;
		}
		catch (Exception exception)
		{
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}
	


	/**
	 * Method getTeacherClassroom
	 *
	 * @param name
	 * @param lastname
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/teacher/Classroom", produces = "application/json")
	public ResponseEntity<?> getTeacherClassroom(@RequestHeader(required = true) String name,
												 @RequestHeader(required = true) String lastname,
												 HttpSession session)
	{
		try
		{
			// --- checking stored CENTRO ---
			if ((session.getAttribute("storedCentro") != null)
					&& (session.getAttribute("storedCentro") instanceof Centro))
			{
				Centro centro = (Centro) session.getAttribute("storedCentro");

				if ((name != null) && !name.trim().isBlank() && !name.trim().isEmpty())
				{
					// -- NOMBRE Y APELLIDOS CON CONTENIDO ---

					Student student = null;
					for (Student st : students)
					{
						// -- CHECKING IF STUDENT EXISTS ---
						if (st.getName().trim().equalsIgnoreCase(name.trim())
								&& st.getLastName().trim().equalsIgnoreCase(lastname.trim()))
						{
							student = st;
						}

					}

					if (student != null)
					{
						// --- STUDENT EXISTS ---
						Grupo grupo = null;
						for (Grupo grp : centro.getDatos().getGrupos().getGrupo())
						{
							String nombreGrp = grp.getNombre().trim().replace("º", "").replace(" ", "").replace("-",
									"");
							String abrvGrp = grp.getAbreviatura().trim().replace("º", "").replace(" ", "").replace("-",
									"");

							log.info(student.getCourse().toString());
							String nombreGrupo = student.getCourse().trim().replace("º", "").replace(" ", "")
									.replace("-", "");

							if (nombreGrp.toLowerCase().contains(nombreGrupo.toLowerCase())
									|| abrvGrp.toLowerCase().contains(nombreGrupo.toLowerCase()))
							{
								grupo = grp;
							}
						}

						if (grupo != null)
						{
							// --- GRUPO EXISTS ---

							HorarioGrup horarioGrup = null;
							for (HorarioGrup horarioGrp : centro.getHorarios().getHorariosGrupos().getHorarioGrup())
							{
								if (horarioGrp.getHorNumIntGr().trim().equalsIgnoreCase(grupo.getNumIntGr().trim()))
								{
									horarioGrup = horarioGrp;
								}
							}

							if (horarioGrup != null)
							{
								// --- HORARIO_GRUP EXISTS ---

								// Getting the actual time



								TimeSlot tramoActual = null;

								// tramoActual = this.gettingTramoActual(centro, actualTime, tramoActual);

								if (tramoActual != null)
								{
									// --- TRAMO ACTUAL EXISTS ---
									Actividad actividadActual = null;

									for (Actividad actv : horarioGrup.getActividad())
									{
										if (actv.getTramo().trim().equalsIgnoreCase(tramoActual.getNumTr().trim()))
										{
											actividadActual = actv;
										}
									}

									if (actividadActual != null)
									{
										log.info(actividadActual.toString());
										// --- ACTIVIDAD ACTUAL EXISTS ---
										TeacherMoment teacherMoment = new TeacherMoment();
										Teacher teacher = new Teacher();
										Classroom classroom = new Classroom();

										// -- GETTING TEACHER ---
										for (Profesor profesor : centro.getDatos().getProfesores().getProfesor())
										{
											if (profesor.getNumIntPR().trim()
													.equalsIgnoreCase(actividadActual.getProfesor().trim()))
											{
												teacher.setName(profesor.getNombre().trim());
												teacher.setLastName(profesor.getPrimerApellido().trim() + " "
														+ profesor.getSegundoApellido().trim());
											}
										}

										// --- GETTING THE CLASSROOM ---
										for (Aula aula : centro.getDatos().getAulas().getAula())
										{
											if (aula.getNumIntAu().trim()
													.equalsIgnoreCase(actividadActual.getAula().trim()))
											{
												String nombreAula = aula.getNombre();

												String[] plantaAula = aula.getAbreviatura().split("\\.");

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

												classroom.setFloor(plantaNumero);
												classroom.setNumber(numeroAula);
											}
										}

										// --- BUILD THE TEACHER MOMENT ---
										teacherMoment.setClassroom(classroom);
										teacherMoment.setTeacher(teacher);

										log.info(teacherMoment.toString());

										// --- RETURN THE TEACHER MOMENT ---
										return ResponseEntity.ok(teacherMoment);
									} else
									{
										// --- ERROR ---
										String error = "ERROR ACTIVDAD ACTUAL NO EXISTENTE OR NULL";

										log.info(error);

										HorariosError horariosError = new HorariosError(400, error, null);
										log.info(error, horariosError);
										return ResponseEntity.status(400).body(horariosError);
									}

								} else
								{
									// --- ERROR ---
									String error = "ERROR TRAMO ACTUAL NO EXISTENTE OR NULL";

									log.info(error);

									HorariosError horariosError = new HorariosError(400, error, null);
									log.info(error, horariosError);
									return ResponseEntity.status(400).body(horariosError);
								}

							} else
							{
								// --- ERROR ---
								String error = "ERROR HORARIO GRUP NOT FOUND OR NULL";

								log.info(error);

								HorariosError horariosError = new HorariosError(400, error, null);
								log.info(error, horariosError);
								return ResponseEntity.status(400).body(horariosError);
							}

						} else
						{
							// --- ERROR ---
							String error = "GRUPO NOT FOUND OR NULL";

							log.info(error);

							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.status(400).body(horariosError);
						}

					} else
					{
						// --- ERROR ---
						String error = "ERROR STUDENT NOT FOUND OR NULL";

						log.info(error);

						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError);
					}

				} else
				{
					// --- ERROR ---
					String error = "ERROR DE PARAMETROS";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError);
				}

			} else
			{
				// --- ERROR ---
				String error = "ERROR storedCentro NOT FOUND OR NULL";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
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
	
	/**
	 * method getListAlumnoFirstSurname
	 *
	 * @param course
	 * @return
	 */
	// REQUEST MAPPING FOR GETTING SORTED STUDENT LIST BASED ON FIRST SURNAME AND
	// COURSE
	@RequestMapping(value = "/get/course/sort/students", produces = "application/json")
	public ResponseEntity<?> getListAlumnoFirstSurname(@RequestParam(required = true) String course)
	{
		try
		{
			if (this.iStudentRepository.count() == 0)
			{
				throw new HorariosError(409, "No hay alumnos cargados en el servidor");
			}

			return ResponseEntity.ok().body(this.iStudentRepository.findByCourseOrderByLastNameAsc(course));
			
		} catch (HorariosError exception)
		{
			log.error("Error al devolver los alumnos ordenados", exception);
			return ResponseEntity.status(400).body(exception.toMap());
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR --
			// RETURN A SERVER ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 500
			// (INTERNAL SERVER ERROR)
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}
	

	@RequestMapping(value = "/get/students-course", produces = "application/json")
	public ResponseEntity<?> getStudentsCourse()
	{
		try
		{
			List<String> listaDeCursos = this.iStudentsRepository.findDistinctCourses();
			if (listaDeCursos.isEmpty())
			{
				throw new HorariosError(409, "No hay cursos cargados en el servidor");
			}

			return ResponseEntity.ok().body(listaDeCursos);
			
		} catch (HorariosError exception)
		{
			log.error("Error al devolver los cursos de los alumnos", exception);
			return ResponseEntity.status(409).body(exception.toMap());
		} catch (Exception exception)
		{
			// -- CATCH ANY ERROR --
			// RETURN A SERVER ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 500
			// (INTERNAL SERVER ERROR)
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			return ResponseEntity.status(500).body(horariosError);
		}
	}
	
	@RequestMapping(value = "/get/points", produces = "application/json")
	public ResponseEntity<?> getListPointsCoexistence()


	{
		try
		{
			List<Integer> valores = this.iActitudePointsRepo.findAllPuntosConvivenciaValores();
			List<String> descripciones = this.iActitudePointsRepo.findAllPuntosConvivenciaDescripciones();


			List<ActitudePoints> listActitudePoints = new ArrayList<>();


			for (int i = 0; i < valores.size(); i++) {
				listActitudePoints.add(new ActitudePoints(valores.get(i), descripciones.get(i)));
			}
			
			// --CHECK IF THE LIST OF ACTITUDE POINTS IS NOT EMPTY--
			if (!listActitudePoints.isEmpty())
			{
				// --RETURN THE LIST OF COEXISTENCE ACTITUDE POINTS AS A RESPONSEENTITY WITH
				// HTTP STATUS 200 (OK)--
				return ResponseEntity.ok().body(listActitudePoints);
			}
			else
			{
				// --RETURN AN ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 400 (BAD
				// REQUEST)--
				String error = "List not found";
				HorariosError horariosError = new HorariosError(400, error, null);
				log.error(error);
				return ResponseEntity.status(400).body(horariosError);
			}
		}
		catch (Exception exception)
		{
			// CATCH ANY ERROR
			String error = "Server Error";
			HorariosError horariosError = new HorariosError(500, error, exception);
			log.error(error, exception);
			// --RETURN A SERVER ERROR MESSAGE AS A RESPONSEENTITY WITH HTTP STATUS 500
			// (INTERNAL SERVER ERROR)--
			return ResponseEntity.status(500).body(horariosError);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/get/parse-course", produces = "application/json")
	public ResponseEntity<?> localizarAlumno(@RequestParam(name = "course", required = true) String studentCourse)
	{
		try
		{
			// Recupera los grupos válidos desde la base de datos
			List<GrupoEntity> grupos = this.grupoRepo.findAllValidGroups();

			// Procesa el grupo del estudiante
			String course = util.parseStudentGroup(studentCourse, grupos);

			// Se coloca un mapa ya que los cursos de los datos generales pueden contener
			// caracteres que el front no lee bien y con el mapa forzamos a mandarlos
			Map<String, String> map = new HashMap<String, String>();
			map.put("curso", course);
			return ResponseEntity.ok().body(map);
		} catch (HorariosError exception)
		{
			log.error("No existe una relacion entre el curso del alumno con los datos generales", exception);
			return ResponseEntity.status(exception.getCode()).body(exception.toMap());
		} catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}

}
