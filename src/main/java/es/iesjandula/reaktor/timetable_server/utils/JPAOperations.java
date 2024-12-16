package es.iesjandula.reaktor.timetable_server.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.ActitudePoints;
import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.jpa.Alumnos;
import es.iesjandula.reaktor.timetable_server.models.jpa.Curso;
import es.iesjandula.reaktor.timetable_server.models.jpa.CursoId;
import es.iesjandula.reaktor.timetable_server.models.jpa.PuntosConvivencia;
import es.iesjandula.reaktor.timetable_server.models.jpa.PuntosConvivenciaAlumnoCurso;
import es.iesjandula.reaktor.timetable_server.models.jpa.PuntosConvivenciaAlumnoCursoId;
import es.iesjandula.reaktor.timetable_server.models.jpa.VisitasServicio;
import es.iesjandula.reaktor.timetable_server.models.jpa.VisitasServicioId;
import es.iesjandula.reaktor.timetable_server.repository.IAlumnoRepository;
import es.iesjandula.reaktor.timetable_server.repository.ICursosRepository;
import es.iesjandula.reaktor.timetable_server.repository.IPuntosConvivenciaALumnoCursoRepository;
import es.iesjandula.reaktor.timetable_server.repository.IPuntosConvivenciaRepository;
import es.iesjandula.reaktor.timetable_server.repository.IVisitasServicioRepository;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public class JPAOperations 
{
	
	/**Repositorio que contiene todas las operaciones CRUD de la entidad Alumnos */
	private IAlumnoRepository alumnoRepo;
	
	/**Repositorio que contiene todas las operaciones CRUD de la entidad Curso */
	private ICursosRepository cursoRepo;
	
	/**Repositorio que contiene todas las operaciones CRUD de la entidad Puntos convivencia */
	private IPuntosConvivenciaRepository puntosRepo;
	
	/**Repositorio que contiene todas las operaciones CRUD de la entidad PuntosConvivenciaAlumnosCurso */
	private IPuntosConvivenciaALumnoCursoRepository sancionRepo;
	
	/**Repositorio que contiene todas las operaciones CRUD de la entidad VisitasServicio */
	private IVisitasServicioRepository visitasRepo;
	
	/**Clase que gestiona las operaciones con fecha y hora */
	private TimeOperations timeOperation;
	
	/**
	 * Constructor que instancia la clase con los repositorios inicializados en
	 * la clase rest
	 * @param alumnoRepo
	 * @param cursoRepo
	 * @param puntosRepo
	 * @param sancionRepo
	 * @param visitasRepo
	 */
	public JPAOperations(IAlumnoRepository alumnoRepo, ICursosRepository cursoRepo,
			IPuntosConvivenciaRepository puntosRepo, IPuntosConvivenciaALumnoCursoRepository sancionRepo,
			IVisitasServicioRepository visitasRepo) 
	{
		this.alumnoRepo = alumnoRepo;
		this.cursoRepo = cursoRepo;
		this.puntosRepo = puntosRepo;
		this.sancionRepo = sancionRepo;
		this.visitasRepo = visitasRepo;
		this.timeOperation = new TimeOperations();
	}
	
	/**
	 * Metodo que registra y comprueba la ida al baño de un estudiante en la base de datos
	 * @param student
	 * @throws HorariosError
	 */
	public void comprobarVisita(Student student) throws HorariosError
	{
		// Recupera datos de la visita.
		String cursoAcademico = student.getMatriculationYear()+"/"+(Integer.parseInt(student.getMatriculationYear())+1);
		Long idAlumno = this.cargarAlumno(student);
		CursoId cursoId = this.cargarCurso(student.getCourse(), cursoAcademico);
		Date now = new Date();
		VisitasServicioId visitaId = new VisitasServicioId(idAlumno,cursoId,now);
		
		List<VisitasServicio> visitasDto = this.visitasRepo.findAll();
		if(visitasDto.isEmpty())
		{
			this.visitasRepo.saveAndFlush(new VisitasServicio(visitaId, 
					new Alumnos(idAlumno,student.getName(),student.getLastName()), new Curso(cursoId), null));
		}
		else
		{
			int index = 0;
			//Comprobamos que el alumno no haya ido al baño aun
			while(index<visitasDto.size())
			{
				VisitasServicio visita = visitasDto.get(index);
				if(visita.getVisitasServicioId().equals(visitaId) && visita.getFechaVuelta()==null)
				{
					throw new HorariosError(404,"El estudiante no ha regresado del baño");
				}
					
				index++;
			}
			//Si el bucle termina significa que el alumno no ha ido al baño y que por lo tanto 
			//podremos registrar una visita sin duplicarla
			this.visitasRepo.saveAndFlush(new VisitasServicio(visitaId, 
					new Alumnos(idAlumno,student.getName(),student.getLastName()), new Curso(cursoId), null));
		}
	}
	
	/**
	 * Metodo que registra y comprueba la vuelta del baño de un estudiante en la base de datos
	 * @param student
	 * @throws HorariosError
	 */
	public void comprobarVuelta(Student student) throws HorariosError
	{
		String cursoAcademico = student.getMatriculationYear()+"/"+(Integer.parseInt(student.getMatriculationYear())+1);
		Long idAlumno = this.cargarAlumno(student);
		CursoId cursoId = this.cargarCurso(student.getCourse(), cursoAcademico);
		
		
		List<VisitasServicio> visitasDto = this.visitasRepo.findAll();
		if(visitasDto.isEmpty())
		{
			throw new HorariosError(404,"No hay visitas al servicio registradas");
		}
		else
		{
			int index = 0;
			boolean vuelta = false;
			//Comprobamos que el alumno haya ido al baño
			while(index<visitasDto.size() && !vuelta)
			{
				VisitasServicio visita = visitasDto.get(index);
				if(visita.getVisitasServicioId().getAlumnoId().equals(idAlumno) 
						&& visita.getVisitasServicioId().getCursoId().equals(cursoId) 
						&& visita.getFechaVuelta()==null
						)
				{
					Date date = new Date();
					visita.setFechaVuelta(date);
					this.visitasRepo.save(visita);
					vuelta = true;
				}	
				index++;
			}
			
			if(!vuelta)
			{
				throw new HorariosError(404,"El alumno "+student.getName()+" "+student.getLastName()+" no ha ido al baño");
			}
			
		}
	}
	
	/**
	 * Metodo que busca las visitas al baño de un determinado alumno usando
	 * un periodo de fechas, los datos se devuelven en una lista de mapas de formato
	 * String String en el que en cada item se guarda el dia y la hora en la que se fue
	 * al baño 
	 * @param student
	 * @param fechaInicio
	 * @param fechaFin
	 * @param visitas
	 * @return una lista que contiene un mapa clave-valor con el patron:
	 * <ul>
	 * 	<li>dia - dia en el que fue al servicio</li>
	 *  <li>hora_ida - hora en la que salio al servicio</li>
	 *  <li>hora_vuelta - dia en el que visito el servicio</li>
	 * </ul>
	 */
	public List<Map<String,String>> getVisitaAlumno(Student student,String fechaInicio,String fechaFin)
	{
		List<Map<String,String>> visitaAlumno = new LinkedList<Map<String,String>>();
		
		List <VisitasServicio> visitas = this.visitasRepo.findAll();
		
		//Lista que se completara con los alumnos que visitaron el baño
		List <VisitasServicio> visitasAlumno = new LinkedList<VisitasServicio>();
		
		String cursoAcademico = student.getMatriculationYear()+"/"+(Integer.parseInt(student.getMatriculationYear())+1);
		Long idAlumno = this.cargarAlumno(student);
		CursoId cursoId = this.cargarCurso(student.getCourse(), cursoAcademico);
		
		//Guardamos las visitas del alumno seleccionado y nos saltamos aquellas que tienen
		//la fecha de vuelta a null
		for(VisitasServicio visita:visitas)
		{
			if(visita.getVisitasServicioId().getAlumnoId().equals(idAlumno) && visita.getVisitasServicioId().getCursoId().equals(cursoId))
			{
				visitasAlumno.add(visita);
			}
		}
		
		//Separador de fecha en dia mes year
		String[] splitFecha = fechaInicio.split("/");
		
		//Array de fechas en formato int
		int[] fechaInt = {Integer.parseInt(splitFecha[0].trim()),Integer.parseInt(splitFecha[1].trim()),Integer.parseInt(splitFecha[2].trim())};
		
		boolean endParser = false;
		
		//Bucle para iterar y guardar los dias y horas en los que el alumno ha ido al baño
		while(!endParser)
		{
			//Transformamos la fecha a string
			String itemDate = this.timeOperation.transformDate(fechaInt);
			
			//Iteramos las visitas
			for(VisitasServicio item:visitasAlumno)
			{
				Date date = item.getVisitasServicioId().getFechaIda();
				//Nos quedamos solo con las que coincida la fecha
				if(this.timeOperation.compareDate(itemDate, date))
				{
					Date horaVuelta = item.getFechaVuelta();
					String valorFechaVuelta = horaVuelta==null ? "No se pulsó la vuelta" : this.timeOperation.transformHour(horaVuelta.getHours(), horaVuelta.getMinutes());
					//Anotamos la fecha y la hora con las que ha ido al baño
					Map<String,String> datosVisita = new HashMap<String,String>();
					datosVisita.put("dia",itemDate);
					datosVisita.put("horas", this.timeOperation.transformHour(date.getHours(), date.getMinutes())+" - "+valorFechaVuelta);
					visitaAlumno.add(datosVisita);
				}
			}
			
			//Comprobamos si la fecha iterada coincide con la fecha final si no la aumentamos
			if(itemDate.equals(fechaFin))
			{
				endParser = true;
			}
			else
			{
				fechaInt = this.timeOperation.sumarDate(fechaInt);
			}
		}
 		
		return visitaAlumno;
	}

	/**
	 * Metodo que busca las visitas de varios alumnos usando un rango de fechas
	 * @param fechaInicio
	 * @param fechaFin
	 * @param visitas
	 * @return una lista que contiene un mapa clave-valor con el patron:
	 * <ul>
	 * 	<li>alumno - modelo alumnos de jpa</li>
	 *  <li>curso - nombre del curso del modelo jpa</li>
	 *  <li>dia - dia en el que visito el servicio</li>
	 *  <li>horas - tiempo en el que estuvo en el servicio</li>
	 * </ul>
	 */
	public List<Map<String,Object>> getVisitasAlumnos(String fechaInicio,String fechaFin)
	{
		List<Map<String,Object>> visitasAlumnos = new LinkedList<Map<String,Object>>();
		
		List <VisitasServicio> visitas = this.visitasRepo.findAll();
		
		//Separador de fecha en dia mes year
		String[] splitFecha = fechaInicio.split("/");
		
		//Array de fechas en formato int
		int[] fechaInt = {Integer.parseInt(splitFecha[0].trim()),Integer.parseInt(splitFecha[1].trim()),Integer.parseInt(splitFecha[2].trim())};
		
		boolean endParser = false;
		
		//Bucle para iterar y guardar los dias y horas en los que el alumno ha ido al baño
		while(!endParser)
		{
			//Transformamos la fecha a string
			String itemDate = this.timeOperation.transformDate(fechaInt);
			
			//Iteramos las visitas
			for(VisitasServicio item:visitas)
			{
				Date date = item.getVisitasServicioId().getFechaIda();
				//Nos quedamos solo con las que coincida la fecha
				if(this.timeOperation.compareDate(itemDate, date))
				{
					Alumnos alumno = this.alumnoRepo.getReferenceById(item.getVisitasServicioId().getAlumnoId());
					Date horaIda = item.getVisitasServicioId().getFechaIda();
					Date horaVuelta = item.getFechaVuelta();
					String valorFechaVuelta = horaVuelta==null ? "No se pulsó la vuelta"  : this.timeOperation.transformHour(horaVuelta.getHours(), horaVuelta.getMinutes());
					//Anotamos la fecha y la hora con las que ha ido al baño
					Map<String,Object> datosVisita = new HashMap<String,Object>();
					datosVisita.put("alumno",alumno);
					datosVisita.put("curso",item.getVisitasServicioId().getCursoId().getNombre());
					datosVisita.put("dia", itemDate);
					datosVisita.put("horas", this.timeOperation.transformHour(horaIda.getHours(), horaIda.getMinutes())+" - "+valorFechaVuelta);
					visitasAlumnos.add(datosVisita);
				}
			}
			
			//Comprobamos si la fecha iterada coincide con la fecha final si no la aumentamos
			if(itemDate.equals(fechaFin))
			{
				endParser = true;
			}
			else
			{
				fechaInt = this.timeOperation.sumarDate(fechaInt);
			}
		}
		return visitasAlumnos;
				
	}
	
	/**
	 * Metodo que obtiene el numero de visitas al servicio por parte de
	 * un estudiante
	 * @param student
	 * @return numero de visitas realizadas, en caso de que 
	 * no haya ninguna se devuelve 0 por defecto
	 */
	public int obtenerNumeroVecesServicio(Student student)
	{
		int numeroVisitas = 0;
		
		String cursoAcademico = student.getMatriculationYear()+"/"+(Integer.parseInt(student.getMatriculationYear())+1);
		Long alumnoId = this.cargarAlumno(student);
		CursoId cursoId = this.cargarCurso(student.getCourse(), cursoAcademico);
		
		List<VisitasServicio> visitas = this.visitasRepo.findAll();
		
		for(VisitasServicio visita:visitas)
		{
			if(visita.getVisitasServicioId().getAlumnoId().equals(alumnoId) && visita.getVisitasServicioId().getCursoId().equals(cursoId)
					&& visita.getFechaVuelta()!=null)
			{
				numeroVisitas++;
			}
		}
		
		return numeroVisitas;
		
	}
	
	/**
	 * Metodo que registra una sancion o recompensa en la base de datos
	 * @param student
	 * @param points
	 */
	public void ponerSancion (Student student, ActitudePoints points)
	{
		String cursoAcademico = student.getMatriculationYear()+"/"+(Integer.parseInt(student.getMatriculationYear())+1);
		Long alumnoId = this.cargarAlumno(student);
		CursoId cursoId = this.cargarCurso(student.getCourse(), cursoAcademico);
		Long puntoId = this.cargarPuntos(points);
		Date date = new Date();
		PuntosConvivenciaAlumnoCursoId puntosConvivenciaId = new PuntosConvivenciaAlumnoCursoId(alumnoId,cursoId,puntoId,date);
		this.sancionRepo.save(new PuntosConvivenciaAlumnoCurso(puntosConvivenciaId, new Alumnos(alumnoId,student.getName(),student.getLastName()),
				new Curso(cursoId),new PuntosConvivencia(puntoId,points.getPoints(),points.getDescription()))); 
		
	}
	
	/**
	 * Metodo que comrpueba e inserta un alumno en la base de datos en caso de que este no se 
	 * encuentre en la misma
	 * @param student
	 * @return el id del alumno para registrarlo en una visita
	 */
	private Long cargarAlumno(Student student)
	{
		//Obtenemos todos los alumnos de la base de datos
		List<Alumnos> alumnos = this.alumnoRepo.findAll();
		Alumnos alumnoDto = null;
		//En caso de que la lista este vacia insertamos el primer alumno
		if(alumnos.isEmpty())
		{
			alumnoDto = this.alumnoRepo.save( new Alumnos(student.getName(),student.getLastName()));
		}
		else
		{
			boolean insert = true;
			int index = 0;
			//Recorremos la lista para asegurar que el alumno no este insertado en la base de datos
			while(index<alumnos.size() && insert)
			{
				Alumnos alumno = alumnos.get(index);
				//Si esta insertado abandonamos el bucle y nos quedamos con el alumno
				if(alumno.getNombre().equals(student.getName()) && alumno.getApellidos().equals(student.getLastName()))
				{	
					insert = false;
					alumnoDto = alumno;
				}
				index++;
			}
			
			//Si no existe en la base de datos lo insertamos
			if(insert)
			{
				alumnoDto = this.alumnoRepo.save( new Alumnos(student.getName(),student.getLastName()));
			}
		}
		
		//Devolvemos su id
		return alumnoDto.getAlumnoId();
	}
	/**
	 * Metodo que comprueba e inserta un curso en la base de datos en caso de que este no
	 * se encuentre en la misma
	 * @param curso
	 * @param cursoAcademico
	 * @return el id del alumno para registrarlo en una visita
	 */
	private CursoId cargarCurso(String curso,String cursoAcademico)
	{
		//Obtenemos todos los cursos de la base de datos
		List<Curso> cursos = this.cursoRepo.findAll();
		CursoId id = new CursoId(curso,cursoAcademico);
		//En caso de que la lista este vacia insertamos el primer curso
		if(cursos.isEmpty())
		{
			this.cursoRepo.save(new Curso(id));
		}
		else
		{
			boolean insert = true;
			int index = 0;
			//Recorremos la lista para asegurar que el curso no este insertado en la base de datos
			while(index<cursos.size() && insert)
			{
				Curso item = cursos.get(index);
				//Si esta insertado abandonamos el bucle y nos quedamos con el curso
				if(item.getCursoId().equals(id))
				{
					insert = false;
				}
				index++;
			}
			
			//Si no existe en la base de datos lo insertamos
			if(insert)
			{
				this.cursoRepo.save(new Curso(id));
			}
		}
		//Devolvemos su id
		return id;	
	}
	
	/**
	 * Metodo que comprueba e inserta una sancion en la base de datos en caso de que este no
	 * se encuentre en la misma
	 * @param points
	 * @return el id de la sancion
	 */
	private Long cargarPuntos(ActitudePoints points)
	{
		//Obtenemos todas las sanciones de la base de datos
		List<PuntosConvivencia> puntos = this.puntosRepo.findAll();
		PuntosConvivencia puntosDto = null;
		//En caso de que la lista este vacia insertamos la primera sancion
		if(puntos.isEmpty())
		{
			puntosDto = this.puntosRepo.save(new PuntosConvivencia(points.getPoints(), points.getDescription()));
		}
		else
		{
			int index = 0;
			boolean insert = true;
			//Recorremos la lista para asegurar que el curso no este insertado en la base de datos
			while(index<puntos.size() && insert)
			{
				PuntosConvivencia item = puntos.get(index);
				//Si esta insertado abandonamos el bucle y nos quedamos con la sancion
				if(item.getValor()==points.getPoints() && item.getDescripcion().equals(points.getDescription()))
				{
					insert = false;
					puntosDto = item;
				}
				
				index++;
			}
			//Si no existe en la base de datos lo insertamos
			if(insert)
			{
				puntosDto = this.puntosRepo.save(new PuntosConvivencia(points.getPoints(), points.getDescription()));
			}
		}
		//Devolvemos su id
		return puntosDto.getPuntosId();
	}
	
	/**
	 * Metodo que busca y encuentra los alumnos que se encuentran en el baño
	 * @param students
	 * @return Conjunto de alumnos que se encuentran en el baño
	 * @throws HorariosError
	 */
	public List<Student> findStudentBathroom(List<Student>students) throws HorariosError
	{
		List<Student> alumnosInBathroom = new LinkedList<Student>();
		
		if(students.isEmpty())
		{
			throw new HorariosError(400,"No se han cargado los datos de los estudiantes");
		}
		
		List<VisitasServicio> visitas = this.visitasRepo.findAll();
		
		for(VisitasServicio visita:visitas)
		{
			Optional<Alumnos> optionalAlumno = null;
			Optional<Curso> optionalCurso = null;
			if(visita.getFechaVuelta()==null)
			{
				optionalAlumno =  this.alumnoRepo.findById(visita.getVisitasServicioId().getAlumnoId());
				optionalCurso = this.cursoRepo.findById(visita.getVisitasServicioId().getCursoId());
			}
			
			if((optionalAlumno!=null && !optionalAlumno.isEmpty()) && (optionalCurso!=null && !optionalCurso.isEmpty()))
			{
				Alumnos alumno = optionalAlumno.get();
				Curso curso = optionalCurso.get();
				
				for(Student student:students)
				{
					String cursoAcademico = student.getMatriculationYear()+"/"+(Integer.parseInt(student.getMatriculationYear())+1);
					if(student.getName().equals(alumno.getNombre()) && student.getLastName().equals(alumno.getApellidos())
							&& student.getCourse().equals(curso.getCursoId().getNombre())
							&& cursoAcademico.equals(curso.getCursoId().getAnioAcademico()))
					{
						alumnosInBathroom.add(student);
					}
				}
			}
		}
		alumnosInBathroom = this.limpiarRepetidos(alumnosInBathroom);
		return alumnosInBathroom;
	}
	
	/**
	 * Metodo que elimina los alumnos repetidos de la lista que se pasa por parametro
	 * @param students 
	 * @return lista sin alumnos repetidos
	 */
	private List<Student> limpiarRepetidos(List<Student> students)
	{
		List<Student> listaCompleta = new LinkedList<Student>();
		
		for(Student student:students)
		{
			if(!listaCompleta.contains(student))
			{
				listaCompleta.add(student);
			}
		}
		
		return listaCompleta;
	} 
}
