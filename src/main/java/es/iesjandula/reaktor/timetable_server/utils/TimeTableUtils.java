package es.iesjandula.reaktor.timetable_server.utils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.ActitudePoints;
import es.iesjandula.reaktor.timetable_server.models.Classroom;
import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Actividad;
import es.iesjandula.reaktor.timetable_server.models.parse.Asignatura;
import es.iesjandula.reaktor.timetable_server.models.parse.Aula;
import es.iesjandula.reaktor.timetable_server.models.parse.AulaPlano;
import es.iesjandula.reaktor.timetable_server.models.parse.Centro;
import es.iesjandula.reaktor.timetable_server.models.parse.Grupo;
import es.iesjandula.reaktor.timetable_server.models.parse.GruposActividad;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioAula;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioProf;
import es.iesjandula.reaktor.timetable_server.models.parse.Profesor;
import es.iesjandula.reaktor.timetable_server.models.parse.TimeSlot;
import es.iesjandula.reaktor.timetable_server.repository.IAsignaturaRepository;
import es.iesjandula.reaktor.timetable_server.repository.IGrupoRepository;
import es.iesjandula.reaktor.timetable_server.repository.IStudentsRepository;

public class TimeTableUtils 
{
	/**Logger de la clase */
	private static Logger log = LogManager.getLogger();
	
	public List<ActitudePoints> loadPoints()
	{
		List<ActitudePoints> points = new LinkedList<ActitudePoints>();
		
		points.add(new ActitudePoints(1, "Buen comportamiento en aula o en actividad extraescolar"));
		points.add(new ActitudePoints(1, "Buen trabajo en clase"));
		points.add(new ActitudePoints(1, "Realizacion de las tareas propuestas en el aula de reflexión/aula convivencia/trabajos comunitarios"));
		points.add(new ActitudePoints(1, "Buen comportamiento en el aula de reflexión/aula convivencia/trabajos comunitarios"));
		points.add(new ActitudePoints(2, "Alumno que no ha perdido puntos en 2 semanas"));
		points.add(new ActitudePoints(2, "Comportamiento excelente en aula o en actividad extraescolar"));
		points.add(new ActitudePoints(2, "Realizar trabajos extraordinarios o voluntarios"));
		points.add(new ActitudePoints(2, "Buen comportamiento en el aula de reflexión/aula convivencia/trabajos comunitarios"));
		points.add(new ActitudePoints(2, "Ayudar al profesor/a en clase"));
		points.add(new ActitudePoints(2, "Asistencia de las familias a reuniones grupales de tutoria"));
		points.add(new ActitudePoints(5, "Participar activamente en las actividades del centro"));
		points.add(new ActitudePoints(5, "Aparece en el cuadro de honor de la clase"));
		points.add(new ActitudePoints(5, "Participar activamente en el cuidado del centro"));
		points.add(new ActitudePoints(5, "Obtencion de un diploma de Convivencia+"));
		points.add(new ActitudePoints(-1, "Interrumpir puntualmente el desarrollo normal de la clase"));
		points.add(new ActitudePoints(-1, "No realizar las tareas en el aula de reflexión/aula convivencia/trabajos comunitarios"));
		points.add(new ActitudePoints(-1, "Mal comportamiento en el aula de reflexión/aula convivencia/trabajos comunitarios"));
		points.add(new ActitudePoints(-3, "Interrumpir de forma reiterada el desarrollo normal de la clase"));
		points.add(new ActitudePoints(-3, "Tres o mas retrasos injustificados en la misma materia a la entrada de clase"));
		points.add(new ActitudePoints(-3, "Molestar a un compañero/a"));
		points.add(new ActitudePoints(-3, "Consumir comida o bebida sin el permiso del profesor/a"));
		points.add(new ActitudePoints(-5, "Comportamiento inadecuado en dependencias comunes"));
		points.add(new ActitudePoints(-5, "Enfrentamiento verbal menor con un compañero"));
		points.add(new ActitudePoints(-5, "Salir de la clase sin la tarjeta del docente"));
		points.add(new ActitudePoints(-5, "Uso indebido del movil (1a y 2a vez)"));
		points.add(new ActitudePoints(-5, "Faltas de asistencia sin justificar igual o superior a 24 horas al mes (aviso al tutor)"));
		points.add(new ActitudePoints(-10, "Imposibilidad de desarrollar de forma normal la clase (envio al aula de reflexion)"));
		points.add(new ActitudePoints(-10, "Desobediencia o enfrentamiento verbal leve con el docente"));
		points.add(new ActitudePoints(-10, "No colaborar con el centro en el esclarecimiento de hechos de conducta contraria a las normas de convivencia"));
		points.add(new ActitudePoints(-10, "Incumplimiento de una sancion impuesta anteriormente"));
		points.add(new ActitudePoints(-10, "Copiar o hacer trampa durante una actividad evaluable"));
		points.add(new ActitudePoints(-10, "Enfrentamiento verbal con otro compañero o enfrentamiento con leve contacto fisico"));
		points.add(new ActitudePoints(-10, "Uso incorrecto de los medios TIC"));
		points.add(new ActitudePoints(-10, "Causar daños menores en material, instalaciones o mobiliario del centro"));
		points.add(new ActitudePoints(-10, "Perder o deteriorar la tarjeta del docente"));
		points.add(new ActitudePoints(-15, "Desobediencia o enfrentamiento grave con el docente"));
		points.add(new ActitudePoints(-15, "Impedimento del desarrollo normal de la clase de forma colectiva"));
		points.add(new ActitudePoints(-15, "Introduccion de objetos prohibidos en el centro"));
		points.add(new ActitudePoints(-15, "Injurias, ofensas, amenazas o coacciones entre iguales"));
		points.add(new ActitudePoints(-15, "No entregar a las familias los comunicados de infraccion"));
		points.add(new ActitudePoints(-15, "Quedarse en otras clases en periodo lectivo o durante el recreo"));
		points.add(new ActitudePoints(-15, "Uso indebido del movil 3a vez y siguientes (aviso a convivencia)"));
		points.add(new ActitudePoints(-25, "Uso de los objetos prohibidos en el centro"));
		points.add(new ActitudePoints(-25, "Causar daños intencionados en material, instalaciones o mobiliarios del centro"));
		points.add(new ActitudePoints(-25, "Suplantacion de identidad de un docente o familiar"));
		points.add(new ActitudePoints(-25, "Falsificacion o sustraccion de documentos o pertenencias academicas"));
		points.add(new ActitudePoints(-25, "Injurias, ofensas, amenazas o coacciones a un docente"));
		points.add(new ActitudePoints(-25, "Injurias, ofensas, amenazas o coacciones a un miembro de la comunidad educativa con fines agravantes"));
		points.add(new ActitudePoints(-25, "Abandonar el centro sin permiso previo"));
		points.add(new ActitudePoints(-25, "Sustraccion de pertenencias de cualquier miembro de la comunidad educativa"));
		points.add(new ActitudePoints(-25, "Realizacion y/o difusion de grabaciones de voz, fotos o videos en el centro de cualquier miembro de la comunidad educativa"));
		points.add(new ActitudePoints(-25, "Consumo de sustancias prohibidas en el centro"));
		points.add(new ActitudePoints(-25, "Agresion fisica entre iguales"));
		points.add(new ActitudePoints(-75, "Agresion fisica a docentes o cualquier miembro de la comunidad educativa con agravante"));
		
		return points;
	}
	
	public List<Aula> transformarAula(List<Aula> original)
	{
		List<Aula> transformada = new LinkedList<Aula>();
		
		for(Aula aula:original)
		{
			if(!this.desecharAula(aula.getNombre(),aula.getAbreviatura()))
			{
				transformada.add(aula);
			}
		}
		return transformada;
	}
	
	private boolean desecharAula(String nombre,String numero)
	{
		switch(nombre)
		{
		case "LABORATORIO DE CIENCIAS":
		{
			return true;
		}
		case "Aula Taller Tecnología":
		{
			return true;
		}
		case "DESDOBLES":
		{
			return true;
		}
		case "Laboratorio FyQ - Desdbl4ESOB":
		{
			return true;
		}
		case "INFORMATICA 1":
		{
			return true;
		}
		case "INFORMATICA 2":
		{
			return true;
		}
		case "Sin asignar o sin aula":
		{
			return true;
		}
		case "Aula de Dibujo":
		{
			return true;
		}
		case "Patio Deportes":
		{
			return true;
		}
		default:
		{
			if(numero.equals("2.21") || numero.equals("1.11"))
			{
				return true;
			}
			
			return false;
		}
		}
	}
	/**
	 * Metodo que busca una clase determinada usando su identificador
	 * @param numero
	 * @param aulas
	 * @return clase encontrada
	 */
	public Classroom searchClassroom(String numero,List<Aula> aulas)
	{	
		int index = 0;
		boolean out = false;
		Classroom classroom = null;
		
		while(index<aulas.size() && !out)
		{
			Aula aula = aulas.get(index);
			
			if(aula.getNumIntAu().equals(numero))
			{
				classroom = new Classroom(aula.getNumIntAu(),aula.getAbreviatura(),aula.getNombre());
				out = true;
			}	
			index++;
		}
		
		return classroom;
		
	}
	
	/**
	 * Metodo que busca un estudainte y suma en uno las veces que ha ido al baño
	 * @deprecated Actualmente se usara la base de datos para contar el numero de veces ademas
	 * el atributo numBathtroom de student ha sido eliminado
	 * @param student
	 * @param students
	 * @return
	 */
	public List<Student> sumarBathroom(Student student,List<Student> students)
	{
		int index = 0;
		boolean out = false;
		
		while(index<students.size() && !out)
		{
			Student item = students.get(index);
			
			if(item.equals(student))
			{
				students.remove(index);
				students.add(student);
				out = true;
			}
			index++;
		}
		
		return students;
	}

	/**
	 * Metodo que ordena una lista generica pasandola a array y ordenandola desde ahi
	 * @param <T> generico que tomara como valor la clase profesores y estudiante
	 * @param objectList
	 * @return array ordenado
	 */
	public <T> Object [] ordenarLista(List<T> objectList)
	{
		Object [] arraySorted = new Object[0];
		
		for(int i=0;i<objectList.size();i++)
		{
			arraySorted = Arrays.copyOf(arraySorted, i+1);
			arraySorted[i] = objectList.get(i);
		}
		
		
		Arrays.sort(arraySorted);
		
		return arraySorted;
	}
	
	
	/**
	 * Metodo que devuelve todas las aulas para la parte de planos en
	 * el frontend
	 * <br>
	 * <br>
	 * AVISO: No alterar el orden en el que se añaden las aulas
	 * ya que luego en el frontend saldran datos erroneos
	 * @return lista de aulas para los planos en el front
	 */
	public List<AulaPlano> parseAulasPlano(byte[] data) throws HorariosError {
	    List<AulaPlano> aulas = new LinkedList<AulaPlano>();

	    // Transformamos los datos a string
	    String content = new String(data);
	    
	    // Los separamos por \n
	    String[] splitContent = content.split("\n");
	    
	    // Comprobamos que la cabecera y los datos estén bien formados
	    if (!splitContent[0].trim().equals("height,width,top,right,left,planta,numIntAu,abreviatura,nombre")) {
	        throw new HorariosError(406, "Los datos del fichero son incorrectos. La cabecera del csv debe ser height,width,top,right,left,planta,numIntAu,abreviatura,nombre");
	    } else {
	        for (String rawData : splitContent) {
	            // Nos saltamos la cabecera
	            if (rawData.trim().equals("height,width,top,right,left,planta,numIntAu,abreviatura,nombre")) {
	                continue;
	            } else {
	                // Separamos los datos por ","
	                String[] attributes = rawData.split(",");
	                
	                try {
	                    // Creamos los atributos y lo añadimos a la lista
	                    double height = Double.parseDouble(attributes[0].trim());
	                    double width = Double.parseDouble(attributes[1].trim());
	                    double top = Double.parseDouble(attributes[2].trim());
	                    double right = Double.parseDouble(attributes[3].trim());
	                    double left = Double.parseDouble(attributes[4].trim());

	                    // Si ya existe el aula, solo actualizamos los datos o lo ignoramos
	                    Aula aula = new Aula(attributes[6].trim(), attributes[7].trim(), attributes[8].trim());
	                    AulaPlano aulaPlano = new AulaPlano(height, width, top, right, left, attributes[5].trim(), aula);

	                    aulas.add(aulaPlano);  // Añadimos el nuevo plano
	                } catch (NumberFormatException exception) {
	                    String message = "Las medidas están mal formadas";
	                    log.error(message, exception);
	                    throw new HorariosError(406, message, exception);
	                } catch (NullPointerException exception) {
	                    String message = "Hay datos que vienen vacíos";
	                    log.error(message, exception);
	                    throw new HorariosError(406, message, exception);
	                }
	            }
	        }
	    }
	    
	    return aulas;
	}
	
	/**
	 * Metodo que filtra las aulas para los planos del front por su planta
	 * devolviendo una lista de las mismas filtradas
	 * @param planta
	 * @param aulas
	 * @return lista de aulas filtradas
	 * @throws HorariosError
	 */
	public List<AulaPlano> buscarPorPlanta(String planta,List<AulaPlano> aulas) throws HorariosError
	{ 
		//Establecemos "" por defecto en caso de que planta sea nulo
		planta = planta==null ? "" : planta;
		
		List<AulaPlano> aulasEncontradas = new LinkedList<AulaPlano>();
		
		if(aulas.isEmpty())
		{
			throw new HorariosError(404,"No se ha cargado ninguna informacion sobre aulas");
		}
		
		if(!planta.isEmpty())
		{
			for(AulaPlano aula:aulas)
			{
				if(aula.getPlanta().equals(planta))
				{
					aulasEncontradas.add(aula);
				}
			}
		}
		else
		{
			aulasEncontradas = aulas;
		}
		
		
		if(aulasEncontradas.isEmpty())
		{
			throw new HorariosError(404,"La planta introducida es erronea, su valor debe de se PLANTA BAJA, PRIMERA PLANTA, SEGUNDA PLANTA, en literal");
		}
		
		return aulasEncontradas;
	}
	
	/**
	 * Metodo que encuentra un profesor en tiempo real usando el aula seleccionada en los planos

	 * @param centro
	 * @param aula
	 * @return profesor encontrado
	 * @throws HorariosError
	 */
	public Profesor searchTeacherAulaNow(Centro centro, Aula aula) throws HorariosError
	{
		Profesor profesor = null;
		//Identificador del profesor
		String numeroProfesor = "";
		TimeOperations timeOp = new TimeOperations();
		LocalDateTime date = LocalDateTime.now();
		String actualTime = date.getHour() + ":" + date.getMinute();
		//Obtenemos el tramo actual
		TimeSlot tramo = timeOp.gettingTramoActual(centro, actualTime);
		
		if(tramo != null)
		{
			//Obtenemos la lista de horarios de las aulas
			List<HorarioAula> horarioAulas = centro.getHorarios().getHorariosAulas().getHorarioAula();
			
			for(HorarioAula horario:horarioAulas)
			{
				//Buscamos el aula en el horario
				if(horario.getHorNumIntAu().equals(aula.getNumIntAu()))
				{
					//Recorremos las actividades con el aula encontrada
					for(Actividad act:horario.getActividad())
					{
						//Si el tramo del aula encontrada coincide con el tramo actual recogemos el
						//numero del profesor
						if(act.getTramo().equalsIgnoreCase(tramo.getNumTr().trim()))
						{
							numeroProfesor = act.getProfesor();
							break;
						}
					}
				}
			}
			
			if(!numeroProfesor.isEmpty())
			{
				int index = 0;
				boolean out = false;
				List<Profesor> profesores = centro.getDatos().getProfesores().getProfesor();
				
				//Buscamos el profesor por su numero
				while(index<profesores.size() && !out)
				{
					Profesor profe = profesores.get(index);
					if(profe.getNumIntPR().equalsIgnoreCase(numeroProfesor))
					{
						profesor = profe;
						out = true;
					}
					index++;
				}
			}
			else
			{
				throw new HorariosError(404,"No se ha podido encontrar el profesor en este momento actual");
			}
		}
		else
		{
			throw new HorariosError(406,"Se esta buscando un horario fuera dfel horario de trabajo del centro");
		}
		
		return profesor;
		
	}
	
	/**
	 * Metodo que encuentra la asignatura que se esta impartiendo a tiempo real usando el aula seleccionada de los planos

	 * @param centro
	 * @param profesor
	 * @return asignatura encontrada
	 * @throws HorariosError
	 */

	@Autowired
	private IAsignaturaRepository asignaturaRepo;
	
	public Map<String, Object> searchSubjectAulaNow(Centro centro, Profesor profesor) throws HorariosError {
	    Map<String, Object> asignaturaActividad = new HashMap<>();
	    Asignatura asignatura = null;
	    String numeroAsignatura = "";
	    TimeOperations timeOp = new TimeOperations();
	    LocalDateTime date = LocalDateTime.now();
	    String actualTime = date.getHour() + ":" + date.getMinute();

	    // Obtener el tramo actual
	    TimeSlot tramo = timeOp.gettingTramoActual(centro, actualTime);
	    
	    if (tramo != null) {
	        List<HorarioProf> horarioProfesor = centro.getHorarios().getHorariosProfesores().getHorarioProf();
	        
	        // Buscar el horario del profesor en la base de datos
	        for (HorarioProf horario : horarioProfesor) {
	            if (horario.getHorNumIntPR().equalsIgnoreCase(profesor.getNumIntPR().trim())) {
	                // Buscar la actividad actual
	                for (Actividad act : horario.getActividad()) {
	                    if (act.getTramo().trim().equalsIgnoreCase(tramo.getNumTr().trim())) {
	                        numeroAsignatura = act.getAsignatura();
	                        asignaturaActividad.put("actividad", act);
	                        break;
	                    }
	                }
	            }
	        }

	        if (!numeroAsignatura.isEmpty()) {
	            // Buscar la asignatura en la base de datos en lugar de la sesión
	            asignatura = asignaturaRepo.findByNumIntAs(numeroAsignatura).orElseThrow(() -> new HorariosError(404, "Asignatura no encontrada en la base de datos"));

	            asignaturaActividad.put("asignatura", asignatura);
	        } else {
	            throw new HorariosError(404, "No se ha podido encontrar la asignatura en este momento actual");
	        }
	    } else {
	        throw new HorariosError(406, "Se está buscando un horario fuera del horario de trabajo del centro");
	    }

	    if (asignaturaActividad.size() <= 1) {
	        throw new HorariosError(400, "El conjunto está incompleto, falta una asignatura o actividad por recoger");
	    }

	    return asignaturaActividad;
	}
	
	/**
	 * Metodo que busca el grupo que se encuentra en el aula seleccionada en los planos

	 * en tiempo real
	 * @param centro
	 * @param actividad
	 * @return grupo encontrado
	 */

	@Autowired
	private IGrupoRepository iGrupoRepo;
	public List<Grupo> searchGroupAulaNow(Centro centro,Actividad actividad)
	{
		List<Grupo> grupos = new LinkedList<Grupo>();
		List<String> numeroGrupo = new LinkedList<String>();
		
		//Recogemos los grupos que participan en la actividad
		GruposActividad numGrupos = actividad.getGruposActividad();
		
		//Recogemos el numero de grupo comparandolo con sus valores
		if(numGrupos.getGrupo1()!=null)
		{
			numeroGrupo.add(numGrupos.getGrupo1());
		}
		if(numGrupos.getGrupo2()!=null)
		{
			numeroGrupo.add(numGrupos.getGrupo2());
		}
		if(numGrupos.getGrupo3()!=null)
		{
			numeroGrupo.add(numGrupos.getGrupo3());
		}
		if(numGrupos.getGrupo4()!=null)
		{
			numeroGrupo.add(numGrupos.getGrupo4());
		}
		if(numGrupos.getGrupo5()!=null)
		{
			numeroGrupo.add(numGrupos.getGrupo5());
		}
		
		//Obtenemos todos los grupos guardado en base este metodo esta en el repositorio
		 List<Grupo> listaGrupos = iGrupoRepo.findAllByNumIntGrIn(numeroGrupo);
		
		for(Grupo item:listaGrupos)
		{
			for(String id:numeroGrupo)
			{
				if(item.getNumIntGr().equals(id))
				{
					grupos.add(item);
				}
			}
		}
		
		return grupos;
	}
	
	/**
	 * Metodo que devuelve una lista de alumnis en funcion del grupo seleccionado
	 * por las aulas de los planos
	 * @param grupo
	 * @param alumnos
	 * @return lista de alumnos por grupo
	 * @throws HorariosError
	 */
	@Autowired
	private IStudentsRepository studentRepo;

	public List<Student> getAlumnosAulaNow(List<Grupo> grupos, List<Student> alumnos) throws HorariosError {
	    List<Student> alumnosAula = new LinkedList<>();

	    for (Grupo grupo : grupos) {
	        // Para el caso de Bachillerato, aplicar un filtro especial
	        String grupoEspecial = this.getAlumnosBach(grupo.getNombre());

	        // Si el grupo no es de Bachillerato, aplicar otro filtro especial
	        grupoEspecial = grupoEspecial.isEmpty() ? this.getSpecialGroup(grupo.getNombre()) : grupoEspecial;

	        List<StudentsEntity> studentsEntities;
	        if (grupoEspecial.isEmpty()) {
	            // Construir el curso del grupo
	            String grade = getGroupGrade(grupo.getNombre());
	            if (grade.isEmpty()) {
	                throw new HorariosError(400, "El curso seleccionado " + grupo.getNombre() + " no coincide con ningún curso de los alumnos");
	            }

	            String grupoAlumno = this.transformGroup(grupo.getNombre());
	            String letraGrupo = this.getGroupLetter(grupo.getNombre());

	            // Formar el curso completo
	            String completeGrade = grade + " " + grupoAlumno;
	            if (letraGrupo != null && !letraGrupo.isEmpty()) {
	                completeGrade = completeGrade + " " + letraGrupo;
	            }

	            studentsEntities = studentRepo.findByCourse(completeGrade);
	        } else {
	            studentsEntities = studentRepo.findByCourse(grupoEspecial);
	        }

	        // Convertir las entidades de StudentsEntity a Student usando el constructor Student(StudentsEntity entity)
	        for (StudentsEntity entity : studentsEntities) {
	            alumnosAula.add(new Student(entity));  // Aquí se usa el constructor que ya tienes
	        }
	    }

	    return alumnosAula;
	}
	
	/**
	 * Metodo que recoge el grado del curso, en caso de que no sea 1,2,3,4
	 * vacia el grado para que en el metodo {@link #getAlumnosAulaNow(Grupo, List)} 
	 * se lance un error 
	 * @param group
	 * @return
	 */
	private String getGroupGrade(String group)
	{
		String grade = String.valueOf(group.charAt(0));
		
		if(!grade.equals("1") && !grade.equals("2") && !grade.equals("3") && !grade.equals("4"))
		{
			grade = "";
		}
		
		return grade;
	}
	
	/**
	 * Metodo que transforma el nombre del curso de los datos del centro
	 * al nombre del curso de los datos del alumno
	 * @param group
	 * @return grupo transformado
	 */
	private String transformGroup(String group)
	{
		String grupoAlumno = "";
		//Nombre de guia natural en alumnos
		if(group.contains("GUIA MEDIO NATURAL") || group.contains("CFGM GMNTL"))
		{
			grupoAlumno = "GMNTL";
		}
		//Nombre de mecatronica en alumnos
		else if(group.contains("MECATRÓNICA INDUSTRIAL"))
		{
			grupoAlumno = "MEC";
		}
		//Nombre de Formacion Profesional Basica en alumnos
		else if(group.contains("Formación Profesional Básica"))
		{
			grupoAlumno = "CFGB";
		}
		//Nombre de DAM en alumnos
		else if(group.contains("CFGS DAM"))
		{
			grupoAlumno = "DAM";
		}
		//Nombre de ESO en alumnos
		else if(group.contains("ESO"))
		{
			grupoAlumno = "ESO";
		}
		
		return grupoAlumno;
	}
	
	private String getAlumnosBach(String group)
	{
		String bach = "";
		String letraGrupo = this.getGroupLetter(group);
		
		if(group.contains("1") && group.contains("BACH") && letraGrupo.equals("A"))
		{
			bach = "1 BHCS A";
		}
		else if(group.contains("1") && group.contains("BACH") && letraGrupo.equals("B"))
		{
			bach = "1 BHCS B";
		}
		else if(group.contains("1") && group.contains("BACH") && letraGrupo.equals("C"))
		{
			bach = "1 BCT C";
		}
		else if(group.contains("2") && group.contains("BACH") && letraGrupo.equals("A"))
		{
			bach = "2 BC A";
		}
		else if(group.contains("2") && group.contains("BACH") && letraGrupo.equals("B"))
		{
			bach = "2 BHCS B";
		}
		else if(group.contains("2") && group.contains("BACH") && letraGrupo.equals("C"))
		{
			bach = "2 BHCS C";
		}
		
		return bach;
		
	}
	
	private String getGroupLetter(String group)
	{
		String letraGrupo = String.valueOf(group.charAt(group.length()-1));
		
		if(!letraGrupo.equals("A") && !letraGrupo.equals("B") && !letraGrupo.equals("C") && !letraGrupo.equals("D"))
		{
			letraGrupo = "";
		}
		
		return letraGrupo;
	}
	/**
	 * Metodo que transforma los grupos del xml a los del csv de alumnos
	 * para los casos especiales que no se pueden transformar de forma
	 * general
	 * @param group
	 * @return grupo transformado
	 */
	private String getSpecialGroup(String group)
	{
		String studentGroup = "";
		switch(group)
		{
			case "1ESO C- Biling":
			{
				studentGroup = "1 ESO C";
				break;
			}
			case "2º ESO-B-Biling":
			{
				studentGroup = "2 ESO B";
				break;
			}
			case "2º ESO-C-BILING.":
			{
				studentGroup = "2 ESO C";
				break;
			}
			case "2ºA":
			{
				studentGroup = "2 ESO A";
				break;
			}
			case "3ESOCD-DIVER":
			{
				studentGroup = "3 ESO D";
				break;
			}
			case "3º ESO-C BILING.":
			{
				studentGroup = "3 ESO C";
				break;
			}
			case "4º ESO-DIVER":
			{
				studentGroup = "4º ESO-DIVER";
				break;
			}
			default:
			{
				studentGroup = "";
			}
		}
		return studentGroup;
	}
	
	/**
	 * Metodo que busca un estudiante proporcionado por la web y
	 * confirma si el estudiante existe o no mediante el lanzamiento
	 * de un error
	 * @param student
	 * @param students
	 * @throws HorariosError
	 */
	public void findStudent (Student student,List<Student> students) throws HorariosError
	{
		boolean encontrado = false;
		int index = 0;
		
		//Iteramos la lista en busca del estudiante
		while(index<students.size())
		{
			Student item = students.get(index);
			//Comprobamos su nombre, apellidos y curso los cuales siempre tendran valor
			if(student.getName().equals(item.getName()) && student.getLastName().equals(item.getLastName()) 
					&& student.getCourse().equals(item.getCourse()))
			{
				encontrado = true;
			}
			
			index++;
		}
		
		//En caso de que no lo encuentre lanzara un error
		if(!encontrado)
		{
			throw new HorariosError(404,"El estudiante proporcionado no se encuentra en los datos generales");
		}
	}
	
	public String parseStudentGroup(String group, List<GrupoEntity> grupos) throws HorariosError 
	{
	    // Esta variable almacenará el grupo final una vez que lo encontremos.
	    String grupoFinal = "";

	    // Usaremos este índice para recorrer la lista de grupos.
	    int index = 0;

	    // Esto nos ayudará a salir del bucle cuando encontremos el grupo que buscamos.
	    boolean out = false;

	    // Recorremos la lista de grupos hasta que encontremos el correcto o terminemos de procesarlos.
	    while (index < grupos.size() && !out) 
	    {
	        // Obtenemos el grupo actual de la lista.
	        GrupoEntity grupo = grupos.get(index);

	        // **Casos especiales**: 
	        // Hay ciertos grupos que necesitan un tratamiento diferente. 
	        // Por ejemplo, grupos de Bachillerato o aquellos que tienen nombres especiales.
	        String grupoEspecial = this.getAlumnosBach(grupo.getNombre());

	        // Si no es un caso especial de Bachillerato, verificamos si pertenece a otro grupo especial.
	        grupoEspecial = grupoEspecial.isEmpty() ? this.getSpecialGroup(grupo.getNombre()) : grupoEspecial;

	        // Si no es un caso especial, seguimos con la transformación general del nombre del grupo.
	        if (grupoEspecial.isEmpty()) 
	        {
	            // Obtenemos el "grado" del curso (por ejemplo, "1º", "2º", "3º").
	            String grade = getGroupGrade(grupo.getNombre());

	            // Si no podemos determinar el grado, lanzamos un error porque el grupo no es válido.
	            if (grade.isEmpty()) {
	                throw new HorariosError(400, "El curso seleccionado " + group + " no coincide con ningún curso válido");
	            }

	            // Transformamos el nombre del grupo en un formato más estándar (por ejemplo, de "ESO-A" a "ESO").
	            String grupoAlumno = this.transformGroup(grupo.getNombre());

	            // Obtenemos la letra del grupo (por ejemplo, "A", "B", "C").
	            String letraGrupo = this.getGroupLetter(grupo.getNombre());

	            // Construimos el nombre completo del grupo (por ejemplo, "1º ESO A").
	            String grupoCompleto = (grade + " " + grupoAlumno + " " + letraGrupo).trim();

	            // Si el nombre completo coincide con el grupo que buscamos, lo guardamos y salimos del bucle.
	            if (grupoCompleto.equals(group)) 
	            {
	                grupoFinal = grupo.getNombre();
	                out = true;
	            }
	        } 
	        // Si sí es un caso especial y coincide con el grupo que buscamos, lo guardamos y salimos del bucle.
	        else if (grupoEspecial.equals(group)) 
	        {
	            grupoFinal = grupo.getNombre();
	            out = true;
	        }

	        // Avanzamos al siguiente grupo en la lista.
	        index++;
	    }

	    // Si terminamos de recorrer la lista y no encontramos un grupo válido, lanzamos un error.
	    if (grupoFinal.isEmpty()) 
	    {
	        throw new HorariosError(404, "No se ha encontrado ningún curso válido para: " + group);
	    }

	    // Finalmente, devolvemos el nombre del grupo que encontramos.
	    return grupoFinal;
	}
	
}
