package es.iesjandula.reaktor.timetable_server.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.ActitudePoints;
import es.iesjandula.reaktor.timetable_server.models.Classroom;
import es.iesjandula.reaktor.timetable_server.models.Course;
import es.iesjandula.reaktor.timetable_server.models.Hour;
import es.iesjandula.reaktor.timetable_server.models.InfoError;
import es.iesjandula.reaktor.timetable_server.models.Rol;
import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.Teacher;
import es.iesjandula.reaktor.timetable_server.models.TeacherMoment;
import es.iesjandula.reaktor.timetable_server.models.entities.ActividadEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AsignaturaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaPlanoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.InfoErrorEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlotEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Actividad;
import es.iesjandula.reaktor.timetable_server.models.parse.Asignatura;
import es.iesjandula.reaktor.timetable_server.models.parse.Aula;
import es.iesjandula.reaktor.timetable_server.models.parse.AulaPlano;
import es.iesjandula.reaktor.timetable_server.models.parse.Centro;
import es.iesjandula.reaktor.timetable_server.models.parse.Grupo;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioGrup;
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
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * @author David Martinez, Pablo Ruiz Canovas
 */
@RestController
@RequestMapping("/horarios")
@Slf4j
public class TimetableRest
{
	/** Attribute centroPdfs , used for get the info of PDFS */
	//private Centro centroPdfs;

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
	private IAsignaturaRepository asignaturaRepo;

	@Autowired
	private IGrupoRepository grupoRepo;

	@Autowired
	private IAulaRepository aulaRepo;

	@Autowired
	private IProfesorRepository profesorRepo;

	@Autowired
	private ITimeSlotRepository timeslotRepo;

	@Autowired
	private IActividadRepository actividadRepo;

	// ---------------Este es para getListPointsCoexistence
	@Autowired
	private IActitudePointsRepository iActitudePointsRepo;

	@Autowired
	private IInfoErrorRepository iInfoErrorRepo;

	@Autowired
	private IStudentsRepository iStudentsRepo;
	
	@Autowired
	private IAulaPlanoRepository iAulaPlanoRepo;

	public TimetableRest()
	{

		this.studentOperation = new StudentOperation();
		this.students = new LinkedList<Student>();
	}

	/**
	 * Este metodo parsea el XML que recibe. Va por este orden. 1- Asignaturas. 2-
	 * Grupos 3- Aulas 4- Profes 5- Tramos
	 *
	 * @param xmlFile
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/send/xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> sendXmlToObjects(@RequestPart MultipartFile xmlFile)
	{
		try
		{
			File xml = new File(xmlFile.getOriginalFilename());
			log.info("FILE NAME: " + xml.getName());
			if (xml.getName().endsWith(".xml"))
			{
				// ES UN XML
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder;
				// -- OBJECT CENTRO ---
				Centro centro = new Centro();
				try
				{
					InputStream is = xmlFile.getInputStream();
					documentBuilder = builderFactory.newDocumentBuilder();
					Document document = documentBuilder.parse(is);

					// --- ELEMENTO ROOT CENTRO ------
					Element rootCenterElement = document.getDocumentElement();
					// --- ELEMENT CENTRO ATTRIBUTES ---
					centro.setNombreCentro(rootCenterElement.getAttribute("nombre_centro"));
					centro.setAutor(rootCenterElement.getAttribute("autor"));
					centro.setFecha(rootCenterElement.getAttribute("fecha"));
					// --------------------------------------------------------------------------------------------------
					// --- CARGA DATOS ---
					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO ASIGNATURAS ---
					log.info("- Almacenando asignaturas. -");

					// Recupera elemento de NODO padre ASIGNATURAS
					Element asignaturasElemet = (Element) rootCenterElement.getElementsByTagName("ASIGNATURAS").item(0);

					// Recupera elemento de NODOS hijos ASIGNATURA
					NodeList asignaturasNodeList = asignaturasElemet.getElementsByTagName("ASIGNATURA");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfAsignatura(asignaturasNodeList);
					log.info("Asignaturas almacenadas en BBDD: {}", asignaturasNodeList.getLength());

					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO GRUPOS ---
					log.info("- Almacenando Grupos -.");

					// Recupera elemento de NODO padre GRUPOS
					Element gruposElement = (Element) rootCenterElement.getElementsByTagName("GRUPOS").item(0);

					// Recupera listado de nodos GRUPO.
					NodeList gruposNodeList = gruposElement.getElementsByTagName("GRUPO");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfGrupo(gruposNodeList);
					log.info("Grupos almacenados en BBDD: {}", gruposNodeList.getLength());

					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO AULAS ---
					log.info("- Almacenando Aulas -.");

					// Recupera elemento de NODO padre AULAS
					Element aulasElement = (Element) rootCenterElement.getElementsByTagName("AULAS").item(0);

					// Recupera listado de nodos AULA.
					NodeList aulasNodeList = aulasElement.getElementsByTagName("AULA");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfAula(aulasNodeList);
					log.info("Aulas almacenadas en BBDD: {}", aulasNodeList.getLength());
					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO PROFESORES ---
					log.info("- Almacenando Profesores -.");

					// Recupera elemento de NODO padre PROFESORES
					Element profesoresElement = (Element) rootCenterElement.getElementsByTagName("PROFESORES").item(0);

					// Recupera listado de nodos PROFESOR.
					NodeList profesoresNodeList = profesoresElement.getElementsByTagName("PROFESOR");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfProfesor(profesoresNodeList);
					log.info("Profesores almacenados en BBDD: {}", profesoresNodeList.getLength());

					// --------------------------------------------------------------------------------------------------

					// --- ALMACENAMIENTO TRAMOS HORARIOS ---
					log.info("- Almacenando Tramos Horarios -.");

					// Recupera elemento de NODO padre TRAMOS HORARIOS
					Element tramosHorariosElement = (Element) rootCenterElement.getElementsByTagName("TRAMOS_HORARIOS")
							.item(0);

					// Recupera listado de nodos TRAMO
					NodeList tramosHorariosNodeList = tramosHorariosElement.getElementsByTagName("TRAMO");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfTramo(tramosHorariosNodeList);
					log.info("Tramos horarios almacenados en BBDD: {}", tramosHorariosNodeList.getLength());

					// --------------------------------------------------------------------------------------------------
					// Nota de David Jason:
					// En esta sección del código se trata de almacenar una serie de registros
					// que introducen información acerca de como se relacionan las entidades cuyos
					// datos se han ido almacenando hasta ahora.
					// --------------------------------------------------------------------------------------------------

					// --- HORARIOS ---

					// --------------------------------------------------------------------------------------------------
					// --- HORARIOS ELEMENT ---
					Element horariosElement = (Element) rootCenterElement.getElementsByTagName("HORARIOS").item(0);

					// --------------------------------------------------------------------------------------------------

					// HORARIOS ASIG.
					log.info("- Almacenando datos de Horarios Asignatura -");

					// Recupera elemento de NODO padre de HORARIOS ASIGNATURAS.
					Element horariosAsignaturasElement = (Element) horariosElement
							.getElementsByTagName("HORARIOS_ASIGNATURAS").item(0);

					// Recupera listado de nodos HORARIOS ASIGNATURAS.
					NodeList horarioAsigNodeList = horariosAsignaturasElement.getElementsByTagName("HORARIO_ASIG");

					// Almacena en BBDD el los valores listados en la lista de nodos XML.
					this.saveValuesOfHorarioAsig(horarioAsigNodeList);
					log.info("Horarios Asignatura almacenados en BBDD: {}", horarioAsigNodeList.getLength());

					// -------------------------------------------------------------------------------------------------------------------------------------------------

					// -------------------------------------------------------------------------------------------------------------------------------------------------
					log.info("File :" + xmlFile.getName() + " load-Done");

				}
				catch (ParserConfigurationException exception)
				{
					String error = "Parser Configuration Exception";
					log.error(error, exception);
					HorariosError horariosException = new HorariosError(400, exception.getLocalizedMessage(),
							exception);
					return ResponseEntity.status(400).body(horariosException.toMap());

				}
				catch (SAXException exception)
				{
					String error = "SAX Exception";
					log.error(error, exception);
					HorariosError horariosException = new HorariosError(400, exception.getLocalizedMessage(),
							exception);
					return ResponseEntity.status(400).body(horariosException.toMap());
				}
				catch (IOException exception)
				{
					String error = "In Out Exception";
					log.error(error, exception);
					HorariosError horariosException = new HorariosError(400, exception.getLocalizedMessage(),
							exception);
					return ResponseEntity.status(400).body(horariosException.toMap());
				}

				return ResponseEntity.ok().build();
			}
			else
			{
				// NO ES UN XML
				String error = "The file is not a XML file";
				HorariosError horariosException = new HorariosError(400, error, new Exception());
				log.error(error, horariosException);
				return ResponseEntity.status(400).body(horariosException.toMap());
			}
		}
		catch (Exception except)
		{
			// SERVER ERROR
			String error = "Server Error";
			HorariosError horariosException = new HorariosError(500, except.getLocalizedMessage(), except);
			log.error(error, horariosException);
			return ResponseEntity.status(500).body(horariosException.toMap());
		}
	}

	// ------------------------- METODOS PARA ALMACENAR ENTIDADES DE DATOS EN BBDD.
	// Estas entidades representan los datos basicos sin relacion entre ellos.

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de los tramos horarios a partir de una lista de nodos XML.
	 * El método recorre la lista de nodos proporcionada, extrae los atributos
	 * necesarios para crear instancias de la entidad TimeSlotEntity, y luego guarda
	 * todas las entidades creadas en la base de datos utilizando un repositorio.
	 *
	 * @param tramosHorariosNodeList la lista de nodos XML que contienen la
	 *                               información de los tramos horarios. Cada nodo
	 *                               debe tener atributos que correspondan a las
	 *                               horas de inicio y fin, el número del día y el
	 *                               identificador del tramo.
	 */
	private void saveValuesOfTramo(NodeList tramosHorariosNodeList)
	{
		List<TimeSlotEntity> listadoTramos = new ArrayList<TimeSlotEntity>();

		for (int i = 0; i < tramosHorariosNodeList.getLength(); i++)
		{
			TimeSlotEntity newTramo = new TimeSlotEntity();
			newTramo.setEndHour(tramosHorariosNodeList.item(i).getAttributes().item(0).getTextContent());
			newTramo.setStartHour(tramosHorariosNodeList.item(i).getAttributes().item(1).getTextContent());
			newTramo.setDayNumber(tramosHorariosNodeList.item(i).getAttributes().item(3).getTextContent());
			newTramo.setNumTr(tramosHorariosNodeList.item(i).getAttributes().item(2).getTextContent());
			listadoTramos.add(newTramo);
		}

		this.timeslotRepo.saveAllAndFlush(listadoTramos);
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de los profesores a partir de una lista de nodos XML. El
	 * método recorre la lista de nodos proporcionada, extrae los atributos
	 * necesarios para crear instancias de la entidad ProfesorEntity, y luego guarda
	 * todas las entidades creadas en la base de datos utilizando un repositorio.
	 *
	 * La información extraída incluye: - Abreviatura del profesor. - Número interno
	 * del profesor (numIntPR). - Nombre completo, dividido en nombre, primer
	 * apellido y segundo apellido.
	 *
	 * @param profesoresNodeList la lista de nodos XML que contienen la información
	 *                           de los profesores. Cada nodo debe tener atributos
	 *                           que representen la abreviatura, el número interno,
	 *                           y el nombre completo en formato "Apellidos,
	 *                           Nombre".
	 */
	private void saveValuesOfProfesor(NodeList profesoresNodeList)
	{
		// Crea una lista para rellenar con entidades profesor.
		List<ProfesorEntity> listadoProfesores = new ArrayList<>();

		// Por cada elemento profesor en el nodo xml
		for (int i = 0; i < profesoresNodeList.getLength(); i++)
		{
			// Crea un nuevo profesor.
			ProfesorEntity newProfesor = new ProfesorEntity();

			// Asigna los valores simples de Abreviatura e Identificador numIntPr.
			newProfesor.setAbreviatura(profesoresNodeList.item(i).getAttributes().item(0).getTextContent());
			newProfesor.setNumIntPR(profesoresNodeList.item(i).getAttributes().item(2).getTextContent());

			// Recoge nombre completo y lo separa en un string de cadenas.
			String nombreCompleto = profesoresNodeList.item(i).getAttributes().item(1).getTextContent();
			String[] nombreCompletoSpit = nombreCompleto.split(",");
			String[] apellidosSplit = nombreCompletoSpit[0].split(" ");

			// Configura nombre y apellidos basandose en el array de cadenas.
			newProfesor.setNombre(nombreCompletoSpit[nombreCompletoSpit.length - 1].trim());
			newProfesor.setPrimerApellido(apellidosSplit[0].trim());
			newProfesor.setSegundoApellido(apellidosSplit[1].trim());

			// Añade profesor actual al listado de profesores.
			listadoProfesores.add(newProfesor);
		}
		// Almacena listado completo en BBDD.
		this.profesorRepo.saveAll(listadoProfesores);
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de las aulas a partir de una lista de nodos XML. El método
	 * recorre la lista de nodos proporcionada, extrae los atributos necesarios para
	 * crear instancias de la entidad AulaEntity, y luego guarda todas las entidades
	 * creadas en la base de datos utilizando un repositorio.
	 *
	 * La información extraída incluye: - Abreviatura del aula. - Número interno del
	 * aula (numIntAu) (identificador). - Nombre del aula.
	 *
	 * @param aulasNodeList la lista de nodos XML que contienen la información de
	 *                      las aulas. Cada nodo debe tener atributos que
	 *                      representen la abreviatura, el número interno y el
	 *                      nombre del aula.
	 */
	private void saveValuesOfAula(NodeList aulasNodeList)
	{
		// Crea una nueva lista para ir almacenando las aulas que rellena.
		List<AulaEntity> listadoDeAulas = new ArrayList<>();

		// Por cada elemento aula en el nodo.
		for (int i = 0; i < aulasNodeList.getLength(); i++)
		{
			// Crea un nuevo objeto aula.
			AulaEntity aulaEntidad = new AulaEntity();

			// Configura abreviatura.
			aulaEntidad.setAbreviatura(aulasNodeList.item(i).getAttributes().item(0).getTextContent());

			// Configura identificador.
			aulaEntidad.setNumIntAu(aulasNodeList.item(i).getAttributes().item(2).getTextContent());

			// Configura nombre del aula.
			aulaEntidad.setNombre(aulasNodeList.item(i).getAttributes().item(1).getTextContent());

			// Agrega al listado de aulas.
			listadoDeAulas.add(aulaEntidad);
		}

		// Almacena todas las aulas listadas en BBDD.
		this.aulaRepo.saveAllAndFlush(listadoDeAulas);
		log.debug("Aulas almacenadas en BBDD");
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de los grupos a partir de una lista de nodos XML. El
	 * método recorre la lista de nodos proporcionada, extrae los atributos
	 * necesarios para crear instancias de la entidad {@code GrupoEntity}, y luego
	 * guarda todas las entidades creadas en la base de datos utilizando un
	 * repositorio.
	 *
	 * La información extraída incluye: - Abreviatura del grupo. - Número interno
	 * del grupo (numIntGr). - Nombre del grupo.
	 *
	 * @param gruposNodeList la lista de nodos XML que contienen la información de
	 *                       los grupos. Cada nodo debe tener atributos que
	 *                       representen la abreviatura, el número interno y el
	 *                       nombre del grupo.
	 */
	private void saveValuesOfGrupo(NodeList gruposNodeList)
	{
		List<GrupoEntity> listadoGrupos = new ArrayList<>();

		for (int i = 0; i < gruposNodeList.getLength(); i++)
		{
			GrupoEntity newGrupo = new GrupoEntity();
			newGrupo.setAbreviatura(gruposNodeList.item(i).getAttributes().item(0).getTextContent());
			newGrupo.setNumIntGr(gruposNodeList.item(i).getAttributes().item(2).getTextContent());
			newGrupo.setNombre(gruposNodeList.item(i).getAttributes().item(1).getTextContent());

			listadoGrupos.add(newGrupo);
		}
		this.grupoRepo.saveAllAndFlush(listadoGrupos);
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Guarda los valores de las asignaturas a partir de una lista de nodos XML. El
	 * método recorre la lista de nodos proporcionada, extrae los atributos
	 * necesarios para crear instancias de la entidad AsignaturaEntity, y luego
	 * guarda todas las entidades creadas en la base de datos utilizando un
	 * repositorio.
	 *
	 * La información extraída incluye: - Abreviatura de la asignatura. - Número
	 * identificacdor de la asignatura (numIntAs). - Nombre de la asignatura.
	 *
	 * @param asignaturasNodeList la lista de nodos XML que contienen la información
	 *                            de las asignaturas. Cada nodo debe tener atributos
	 *                            que representen la abreviatura, el número interno
	 *                            y el nombre de la asignatura.
	 */
	private void saveValuesOfAsignatura(NodeList asignaturasNodeList)
	{
		List<AsignaturaEntity> listadoAsignaturas = new ArrayList<>();

		for (int i = 0; i < asignaturasNodeList.getLength(); i++)
		{
			AsignaturaEntity newAsignatura = new AsignaturaEntity();
			newAsignatura.setAbreviatura(asignaturasNodeList.item(i).getAttributes().item(0).getTextContent());
			newAsignatura.setNumIntAs(asignaturasNodeList.item(i).getAttributes().item(2).getTextContent());
			newAsignatura.setNombre(asignaturasNodeList.item(i).getAttributes().item(1).getTextContent());

			listadoAsignaturas.add(newAsignatura);
		}
		this.asignaturaRepo.saveAllAndFlush(listadoAsignaturas);
	}

	// ---------------- METODOS PARA ALMACENAR HORARIOS EN BBDD.

	/**
	 * Autor: David Jason G.
	 * 
	 * Procesa y guarda una actividad asociada a cada grupo presente en un mapa de
	 * nodos.
	 * 
	 * @param actividadEntity        la entidad de la actividad que será asociada a
	 *                               los grupos.
	 * @param gruposActividadNodeMap un mapa de nodos que contiene los grupos
	 *                               relacionados con la actividad. Cada nodo
	 *                               representa un grupo único.
	 * 
	 *                               <p>
	 *                               Funcionamiento:
	 *                               </p>
	 *                               <ul>
	 *                               <li>Itera sobre cada nodo del mapa de grupos de
	 *                               actividad.</li>
	 *                               <li>Para cada nodo:
	 *                               <ul>
	 *                               <li>Obtiene la información del grupo.</li>
	 *                               <li>Asocia la actividad al grupo mediante el
	 *                               método
	 *                               {@link #saveActividadWithGroup(Node, ActividadEntity)}.</li>
	 *                               </ul>
	 *                               </li>
	 *                               </ul>
	 * 
	 *                               <p>
	 *                               Este método garantiza que una entrada de
	 *                               actividad sea creada para cada grupo asociado,
	 *                               preservando la relación entre actividades y
	 *                               grupos.
	 *                               </p>
	 * 
	 * @see ActividadEntity
	 * @see #saveActividadWithGroup(Node, ActividadEntity)
	 */
	private void saveValuesOfGruposActividadAttrs(ActividadEntity actividadEntity, NamedNodeMap gruposActividadNodeMap)
	{
		// Por cada grupo en el nodo de grupos.
		for (int i = 0; i < gruposActividadNodeMap.getLength(); i++)
		{
			// Asigna nodo.
			Node node = gruposActividadNodeMap.item(i);

			// Guarda una instancia de la actividad con el grupo del iteración actual.
			this.saveActividadWithGroup(node, actividadEntity);
		}
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Asocia una actividad a un grupo específico y guarda la relación en la base de
	 * datos.
	 * 
	 * @param node            el nodo XML que representa un grupo. Su contenido debe
	 *                        incluir el identificador del grupo.
	 * @param actividadEntity la entidad de actividad que será asociada al grupo.
	 * 
	 *                        <p>
	 *                        Funcionamiento:
	 *                        </p>
	 *                        <ul>
	 *                        <li>Verifica si el nombre del nodo contiene la palabra
	 *                        clave "grupo_".</li>
	 *                        <li>Recupera el identificador del grupo desde el
	 *                        contenido del nodo.</li>
	 *                        <li>Busca en el repositorio la entidad
	 *                        {@link GrupoEntity} correspondiente al
	 *                        identificador.</li>
	 *                        <li>Asocia la entidad del grupo a la actividad
	 *                        mediante
	 *                        {@link ActividadEntity#setGrupo(GrupoEntity)}.</li>
	 *                        <li>Guarda la actividad actualizada en la base de
	 *                        datos utilizando el repositorio de actividades.</li>
	 *                        <li>Registra la operación mediante un mensaje de
	 *                        depuración.</li>
	 *                        </ul>
	 * 
	 *                        <p>
	 *                        <strong>Nota:</strong> Este método crea una nueva
	 *                        entrada de actividad para cada grupo asociado. Esto
	 *                        reemplaza la lógica anterior de reutilizar una
	 *                        actividad única con múltiples referencias a grupos.
	 *                        </p>
	 * 
	 * @throws NoSuchElementException si no se encuentra el grupo en el repositorio
	 *                                {@code grupoRepo}.
	 * 
	 * @see ActividadEntity
	 * @see GrupoEntity
	 * @see ActividadRepository
	 * @see GrupoRepository
	 */
	private void saveActividadWithGroup(Node node, ActividadEntity actividadEntity)
	{
		if (node.getNodeName().contains("grupo_"))
		{
			// Recupera el ID.
			String idGrupo = node.getTextContent();
			// Busca el grupo correspondiente al ID recuperado.
			GrupoEntity nuevoGrupo = grupoRepo.findById(idGrupo).get();

			// Setea el grupo a la nueva actividad.
			actividadEntity.setGrupo(nuevoGrupo);

			// Guarda en base de datos.
			this.actividadRepo.saveAndFlush(actividadEntity);
			log.debug("Grupo asignado a actividad: {}", nuevoGrupo.toString());
		}
	}

	/**
	 * Autor: David Jason G.
	 * 
	 * Procesa una lista de nodos XML que representan horarios de asignaturas,
	 * recupera datos de las actividades asociadas y las guarda en la base de datos
	 * (SOLO ALMACENA REGISTROS DE LA ACTIVIDAD).
	 *
	 * @param horarioAsigNodeList una lista de nodos XML que representan horarios de
	 *                            asignaturas. Cada nodo debe contener datos de
	 *                            actividades asociadas.
	 * @throws Exception si ocurre un error durante el procesamiento, como la falta
	 *                   de una entidad requerida (Asignatura, Aula, Profesor o
	 *                   Tramo) en la base de datos.
	 *
	 *                   <p>
	 *                   Funcionamiento:
	 *                   </p>
	 *                   <ul>
	 *                   <li>Por cada nodo en la lista de horarios de
	 *                   asignaturas:</li>
	 *                   <ul>
	 *                   <li>Obtiene los nodos de actividades asociadas al horario
	 *                   de asignatura.</li>
	 *                   <li>Por cada actividad encontrada:</li>
	 *                   <ul>
	 *                   <li>Crea una instancia de {@link ActividadEntity} y asigna
	 *                   sus atributos basándose en la información de los nodos XML
	 *                   y los repositorios.</li>
	 *                   <li>Recupera entidades relacionadas (Aula, Profesor, Tramo,
	 *                   Asignatura) y las asigna a la actividad. Lanza una
	 *                   excepción si alguna de estas entidades no se encuentra en
	 *                   la base de datos.</li>
	 *                   <li>Obtiene los datos de los grupos asociados a la
	 *                   actividad y utiliza el método
	 *                   {@link #saveValuesOfGruposActividadAttrs(ActividadEntity, NamedNodeMap)}
	 *                   para procesarlos.</li>
	 *                   </ul>
	 *                   </ul>
	 *                   </ul>
	 *
	 *                   <p>
	 *                   <strong>Nota:</strong> Las entidades relacionadas son
	 *                   críticas para completar el registro de la actividad. Si
	 *                   alguna de ellas no está presente en la base de datos, el
	 *                   método lanza una excepción para garantizar la integridad de
	 *                   los datos.
	 *                   </p>
	 *
	 * @see ActividadEntity
	 * @see AsignaturaEntity
	 * @see AulaEntity
	 * @see ProfesorEntity
	 * @see TimeSlotEntity
	 * @see #saveValuesOfGruposActividadAttrs(ActividadEntity, NamedNodeMap)
	 */
	private void saveValuesOfHorarioAsig(NodeList horarioAsigNodeList) throws Exception
	{
		// Por cada elemento en el nodemap de horario asignatura.
		for (int i = 0; i < horarioAsigNodeList.getLength(); i++)
		{

			// Instrucciones para recoger el nodelist de Actividad.
			Element horarioAsigElement = (Element) horarioAsigNodeList.item(i);
			NodeList actividadNodeList = horarioAsigElement.getElementsByTagName("ACTIVIDAD");

			// POR CADA ACTIVIDAD EN EL NODELIST DE ACTIVIDADES DE HORARIO ASIGNATURAS.
			for (int j = 0; j < actividadNodeList.getLength(); j++)
			{
				// Crea una nuevo objeto de actividad en bbdd (nuevo registro actividad).
				ActividadEntity actividadEntity = new ActividadEntity();

				// ASIGNACIÓN DE REFERENCIAS A OTRAS ENTIDADES EN EL REGISTRO TRATADO EN
				// ITERACIÓN ACTUAL.

				// Referencia al AULA de la actividad (referencia a clave primaria).
				String aulaId = actividadNodeList.item(j).getAttributes().item(0).getTextContent();
				Optional<AulaEntity> aula = aulaRepo.findById(aulaId);
				// aqui peta
				if (!aula.isPresent())
				{
					String mensaje = "El AULA referenciada en saveValuesOfHorarioAsig no encontrada.";
					log.error(mensaje);
					throw new Exception(mensaje);
				}
				actividadEntity.setAula(aula.get());

				// Asigna atributo de Numero Actividad.
				actividadEntity.setNumAct(actividadNodeList.item(j).getAttributes().item(1).getTextContent());

				// Asigna atributo de Numero Unidad
				actividadEntity.setNumUn(actividadNodeList.item(j).getAttributes().item(2).getTextContent());

				// Referencia al PROFESOR de la actividad (referencia a clave primaria).
				String profeId = actividadNodeList.item(j).getAttributes().item(3).getTextContent();
				Optional<ProfesorEntity> profe = profesorRepo.findById(profeId);
				if (!profe.isPresent())
				{
					String mensaje = "El PROFESOR referenciado en saveValuesOfHorarioAsig no encontrado.";
					log.error(mensaje);
					throw new Exception(mensaje);
				}
				actividadEntity.setProfesor(profe.get());

				// Referencia al TRAMO HORARIO de la actividad (referencia a clave primaria).
				String tramoId = actividadNodeList.item(j).getAttributes().item(4).getTextContent();
				Optional<TimeSlotEntity> tramo = timeslotRepo.findById(tramoId);
				if (!tramo.isPresent())
				{
					String mensaje = "El TRAMO HORARIO referenciado en saveValuesOfHorarioAsig no encontrado.";
					log.error(mensaje);
					throw new Exception(mensaje);
				}
				actividadEntity.setTramo(tramo.get());

				// ATENCION: NO ES EL MISMO MECANISMO QUE PARA EL RESTO DE REFERENCIAS.
				// Recupera el ID que identifica la asignatura de la iteración actual.
				String asignaturaId = horarioAsigNodeList.item(i).getAttributes().item(0).getTextContent();
				Optional<AsignaturaEntity> asignatura = asignaturaRepo.findById(asignaturaId);
				if (!asignatura.isPresent())
				{
					String mensaje = "La ASIGNATURA referenciada en saveValuesOfHorarioAsig no encontrada.";
					log.error(mensaje);
					throw new Exception(mensaje);
				}
				actividadEntity.setAsignatura(asignatura.get());

				// recoge el nodelist de grupos actividad.
				NamedNodeMap gruposActividadNodeMap = ((Element) actividadNodeList.item(j))
						.getElementsByTagName("GRUPOS_ACTIVIDAD").item(0).getAttributes();

				this.saveValuesOfGruposActividadAttrs(actividadEntity, gruposActividadNodeMap);
			}

		}
	}

	// ------------------ METODOS DE RECUPERACIÓN DE DATOS.

	// Metodo de utilidad para rellenar el listado de horarios profesor.
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

	// Metodo para rellenar el listado de horarios grupo.
	public void fillHorarioGrupoValues(List<HorarioGrup> listadoHorariosGrupo)
	{
		for (GrupoEntity grupo : this.grupoRepo.findAll())
		{

			List<Actividad> listadoActividades = actividadRepo.recuperaListadoActividades();

			String totAc = String.valueOf(listadoActividades.size());

			// Crea nuevo horario profesor.
			HorarioGrup horarioGrupo = new HorarioGrup();

			// Rellena datos.
			horarioGrupo.setActividad(listadoActividades);
			horarioGrupo.setHorNumIntGr(grupo.getNumIntGr());
			horarioGrupo.setTotAC(totAc);
			horarioGrupo.setTotUn("0"); // REVISAR

			// Agrega el horario del grupo al listado.
			listadoHorariosGrupo.add(horarioGrupo);
		}
	}

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

// depende de carga de fichero csv.
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
	 * Method getClassroomCourse
	 *
	 * @param courseName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/get/teachersubject", produces = "application/json")
	public ResponseEntity<?> getTeacherSubject(@RequestParam(required = true) String courseName)

	{
		try
		{
			if (!courseName.isBlank() && !courseName.isBlank())
			{

				// --- IF EXIST THE COURSE ---
				Grupo grup = null;

				List<Grupo> listadoGrupos = this.grupoRepo.recuperaGruposDeParseo();

				for (Grupo grupo : listadoGrupos)
				{
					if (grupo.getNombre().trim().equalsIgnoreCase(courseName.trim()))
					{
						// --- EXIST THE COURSE ---
						grup = grupo;
					}
				}
				if (grup != null)
				{
					// --- GRUPO EXIST , NOW GET THE ACUTAL TRAMO ---

					// Getting the actual time
					String actualTime = LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute();
					log.info(actualTime);

					TimeSlot acutalTramo = this.gettingTramoActual(actualTime);

					// --- CHECKING IF THE TRAMO ACTUAL EXISTS ---
					if (acutalTramo != null)
					{
						// --- TRAMO ACTUAL EXISTS ---

						// --- NOW GETTING THE HORARIO GRUP , WITH THE SAME ID OF THE GROUP ---
						HorarioGrup horario = null;

						List<HorarioGrup> listadoHorariosGrupo = new ArrayList<>();

						this.fillHorarioGrupoValues(listadoHorariosGrupo);

						for (HorarioGrup horarioGrup : listadoHorariosGrupo)
						{
							// --- EQUAL IDS ---
							if (horarioGrup.getHorNumIntGr().trim().equalsIgnoreCase(grup.getNumIntGr().trim()))
							{
								// --- THE HORARIO GROUP OF THE GROUP ---
								horario = horarioGrup;
							}
						}

						// --- IF THE HORARIO GRUP EXIST ---
						if (horario != null)
						{
							// --- GETTING THE HORARIO GRUP ACTIVIDADES ----
							Actividad activ = null;
							for (Actividad actividad : horario.getActividad())
							{
								// --- GETTING THE ACTIVIDAD WITH THE SAME ID OF THE ACTUAL TRAMO ---
								if (actividad.getTramo().trim().equalsIgnoreCase(acutalTramo.getNumTr().trim()))
								{
									activ = actividad;
								}
							}

							// --- IF EXIST THIS ACTIVIDAD ---
							if (activ != null)
							{
								// --- NOW GET THE PROFESOR AND ASIGNATURA BY PROFESOR ID AND THE ASIGNATURA ID
								// ---

								// --- PROFESOR ---
								Profesor profesor = null;

								List<Profesor> listadoProfesores = this.profesorRepo.recuperaListadoProfesores();

								for (Profesor prof : listadoProfesores)
								{
									// --- EQUAL PROFESSOR ID --
									if (prof.getNumIntPR().trim().equalsIgnoreCase(activ.getProfesor().trim()))
									{
										profesor = prof;
									}
								}

								// --- ASIGNATURA ---
								Asignatura asignatura = null;

								List<Asignatura> listadoAsignaturas = this.asignaturaRepo.recuperaListadoAsignatura();

								for (Asignatura asig : listadoAsignaturas)
								{
									// --- EQUAL ASIGNATURA ID --
									if (asig.getNumIntAs().trim().equalsIgnoreCase(activ.getAsignatura().trim()))
									{
										asignatura = asig;
									}
								}

								if ((profesor != null) && (asignatura != null))
								{
									// --- THE FINAL PROFESSOR AND ASIGNATURA ---
									log.info("PROFESOR: " + profesor + "\n" + "ASIGNATURA: " + asignatura);
									TeacherMoment teacherMoment = new TeacherMoment();

									// --- TELEFONO - EMAIL - AND -ROL - IS FAKE AND HARDCODED, BECAUSE THE XML DONT
									// HAVE THIS INFO ---
									// --setting teacher---
									teacherMoment.setTeacher(new Teacher(profesor.getNombre().trim(),
											profesor.getPrimerApellido().trim() + " "
													+ profesor.getSegundoApellido().trim(),
											profesor.getNombre().trim() + "@email.com", "000-000-000",
											List.of(Rol.conserje)));

									// --- setting asignatura name ---
									teacherMoment.setSubject(asignatura.getNombre().trim());

									List<Aula> listadoAulas = this.aulaRepo.recuperaListadoAulas();

									Classroom clase = this.util.searchClassroom(activ.getAula(), listadoAulas);
									teacherMoment.setClassroom(clase);

									// --- RETURN THE THEACER MOMENT , WIOUTH CLASSROOM ---
									return ResponseEntity.ok().body(teacherMoment);

								}
								else
								{

									// --- ERROR ---
									String error = "PROFESOR O ASIGNATURA NO ENCONTRADOS O NULL " + profesor + "\n" + asignatura;

									log.info(error);

									HorariosError horariosError = new HorariosError(400, error, null);
									log.info(error, horariosError);
									return ResponseEntity.status(400).body(horariosError.toMap());
								}

							}
							else
							{
								// --- ERROR ---
								String error = "ERROR , ACTIVIDAD NULL O NO ENCONTRADA";

								log.info(error);

								HorariosError horariosError = new HorariosError(400, error, null);
								log.info(error, horariosError);
								return ResponseEntity.status(400).body(horariosError.toMap());
							}
						}
						else
						{
							// --- ERROR ---
							String error = "ERROR , HORARIO GRUP NULL O NO ENCONTRADO";

							log.info(error);

							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.status(400).body(horariosError.toMap());
						}
					}
					else
					{
						// --- ERROR ---
						String error = "ERROR , TRAMO NULL O NO EXISTE";

						log.info(error);

						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError.toMap());
					}
				}
				else
				{
					// --- ERROR ---
					String error = "ERROR GRUPO NULL O NO ENCONTRADO ";

					log.info(error);

					HorariosError horariosError = new HorariosError(400, error, null);
					log.info(error, horariosError);
					return ResponseEntity.status(400).body(horariosError.toMap());
				}
			}
			else
			{
				// --- ERROR ---
				String error = "ERROR , CURSO EN BLANCO O NO PERMITIDO";

				log.info(error);

				HorariosError horariosError = new HorariosError(400, error, null);
				log.info(error, horariosError);
				return ResponseEntity.status(400).body(horariosError.toMap());
			}
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

	/**
	 * Method getClassroomCourse
	 *
	 * @param courseName
	 * @param session
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
	 * Method getSchedulePdf
	 *
	 * @param name
	 * @param lastname
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/get/horario/teacher/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getSchedulePdf(@RequestParam(required = true) String name,
			@RequestParam(required = true) String lastname)
	{
		try
		{
			if (!name.trim().isBlank() && !name.trim().isEmpty() && !lastname.trim().isBlank()
					&& !lastname.trim().isEmpty())
			{


					// --- GETTING THE PROFESSOR AND CHECK IF EXISTS ---
					if (lastname.split(" ").length < 2)
					{
						// -- CATCH ANY ERROR ---
						String error = "ERROR NO HAY DOS APELLIDOS DEL PROFESOR O NO ENCONTRADOS EN HEADERS";
						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError);
					}
					String profFirstLastName = lastname.trim().split(" ")[0];
					String profSecondLastName = lastname.trim().split(" ")[1];

					ProfesorEntity profesor = null;
					for (ProfesorEntity prof : this.profesorRepo.findAll())
					{
						if (prof.getNombre().trim().equalsIgnoreCase(name.trim())
								&& prof.getPrimerApellido().trim().equalsIgnoreCase(profFirstLastName)
								&& prof.getSegundoApellido().trim().equalsIgnoreCase(profSecondLastName))
						{
							// --- PROFESSOR EXISTS , SET THE VALUE OF PROF IN PROFESOR ---
							profesor = prof;
							log.info("PROFESOR ENCONTRADO -> " + profesor.toString());
						}
					}

					if (profesor != null)
					{
						// --- PROFESOR EXISTS ---
						HorarioProf horarioProfesor = null;
						List<HorarioProf> horarioProfList = new ArrayList<>();
						fillHorarioProfValues(horarioProfList);
						
						for (HorarioProf horarioProf : horarioProfList)
						{
							if (horarioProf.getHorNumIntPR().trim().equalsIgnoreCase(profesor.getNumIntPR().trim()))
							{
								// --- HORARIO PROFESOR EXISTS , SET THE VALUE ON HORARIO PROFESOR---
								horarioProfesor = horarioProf;
							}
						}

						if (horarioProfesor != null)
						{
							// --- HORARIO EXISTS ---
							// --- CREATING THE MAP WITH KEY STRING TRAMO DAY AND VALUE LIST OF ACTIVIDAD
							// ---
							Map<String, List<Actividad>> profesorMap = new HashMap<>();

							// --- FOR EACH ACTIVIDAD , GET THE TRAMO DAY , AND PUT ON MAP WITH THE
							// ACTIVIDADES OF THIS DAY (LIST ACTIVIDAD) ---
							for (Actividad actividad : horarioProfesor.getActividad())
							{
								TimeSlotEntity tramo = this.extractTramoFromCentroActividad( actividad);

								// --- CHECKING IF THE TRAMO DAY EXISTS ---
								if (!profesorMap.containsKey(tramo.getDayNumber().trim()))
								{
									// --- ADD THE NEW KEY AND VALUE ---
									List<Actividad> actividadList = new ArrayList<>();
									actividadList.add(actividad);
									Collections.sort(actividadList);

									profesorMap.put(tramo.getDayNumber().trim(), actividadList);
								}
								else
								{
									// -- ADD THE VALUE TO THE ACTUAL VALUES ---
									List<Actividad> actividadList = profesorMap.get(tramo.getDayNumber().trim());
									actividadList.add(actividad);
									Collections.sort(actividadList);
									profesorMap.put(tramo.getDayNumber().trim(), actividadList);
								}
							}

							// --- CALLING TO APPLICATION PDF , TO GENERATE PDF ---

							try
							{
								// -- CALLING TO THE METHOD GET INFO PDF OF APLICATION PDF TO CREATE THE PDF ---
								applicationPdf.getInfoPdf( profesorMap, profesor);

								// --- GETTING THE PDF BY NAME URL ---
								File file = new File(
										profesor.getNombre().trim() + "_" + profesor.getPrimerApellido().trim() + "_"
												+ profesor.getSegundoApellido() + "_Horario.pdf");

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
								}
								catch (IOException exception)
								{
									// --- ERROR ---
									String error = "ERROR GETTING THE BYTES OF PDF ";

									log.info(error);

									HorariosError horariosError = new HorariosError(500, error, exception);
									log.info(error, horariosError);
									return ResponseEntity.status(500).body(horariosError);
								}
							}
							catch (HorariosError exception)
							{
								// --- ERROR ---
								String error = "ERROR getting the info pdf ";

								log.info(error);

								HorariosError horariosError = new HorariosError(400, error, exception);
								log.info(error, horariosError);
								return ResponseEntity.status(400).body(horariosError);
							}

						}
						else
						{
							// --- ERROR ---
							String error = "ERROR HORARIO_PROFESOR NOT FOUNT OR NULL";

							log.info(error);

							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.status(400).body(horariosError);
						}
					}
					else
					{
						// --- ERROR ---
						String error = "ERROR PROFESOR NOT FOUND OR NULL";

						log.info(error);

						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError);
					}

			} else
			{
				// --- ERROR ---
				String error = "ERROR PARAMETROS HEADER NULL OR EMPTY, BLANK";

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

	/**
	 * Method getSchedulePdf
	 *
	 * @param name
	 * @param lastname
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/get/grupo/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getGroupSchedule(@RequestParam(required = true, name = "group") String grupo)

	{
		try
		{
			// --- CHEKING THE GRUPO ---
			if ((grupo != null) && !grupo.trim().isBlank() && !grupo.trim().isEmpty())
			{
				
					// --- CHEKING IF GRUPO EXISTS ---
					GrupoEntity grupoFinal = null;
					for (GrupoEntity grup : this.grupoRepo.findAll())
					{
						// --- RAPLACING "SPACE", " º " AND " - " FOR EMPTY , THT IS FOR GET MORE
						// SPECIFIC DATA ---
						String grupoParam = grupo.replace(" ", "").replace("-", "").replace("º", "");
						String grupName = grup.getNombre().replace(" ", "").replace("-", "").replace("º", "");
						String grupAbr = grup.getAbreviatura().replace(" ", "").replace("-", "").replace("º", "");

						if (grupName.trim().toLowerCase().contains(grupoParam.trim().toLowerCase())
								|| grupAbr.trim().toLowerCase().contains(grupoParam.trim().toLowerCase()))
						{
							grupoFinal = grup;
						}
					}

					// --- IF GRUPO EXISTS ---
					if (grupoFinal != null)
					{
						// --- GRUPO EXISTS ---

						// -- CHEKING HORARIO_GRUP FROM GRUPO_FINAL ---
						HorarioGrup horarioGrup = null;
						List<HorarioGrup> horarioGrupList = new ArrayList<>();
						fillHorarioGrupoValues(horarioGrupList);
						for (HorarioGrup horarioGrp : horarioGrupList)
						{
							// -- GETTING THE HORARIO_GRUP OF THE GRUP ---
							if (horarioGrp.getHorNumIntGr().trim().equalsIgnoreCase(grupoFinal.getNumIntGr().trim()))
							{
								horarioGrup = horarioGrp;
							}
						}

						// --- IF EXISTS HORARIO_GRUP ---
						if (horarioGrup != null)
						{
							// --- GETTING THE ACTIVIDAD LIST OF THE GRUPO ---
							List<Actividad> actividadList = horarioGrup.getActividad();

							// --- ACTIVIDAD_LIST HV MORE THAN 0 ACTIVIDAD AN IS NOT NULL ---
							if ((actividadList != null) && (actividadList.size() > 0))
							{
								// --- GENERATE THE MAP FOR TRAMO DAY , AND ACTIVIDAD LIST ---
								Map<String, List<Actividad>> grupoMap = new HashMap<>();

								// --- CALSIFICATE EACH ACTIVIDAD ON THE SPECIFIC DAY ---
								for (Actividad actv : actividadList)
								{
									// --- GET THE TRAMO ---
									TimeSlotEntity tramo = this.extractTramoFromCentroActividad( actv);

									// --- IF THE MAP NOT CONTAINS THE TRAMO DAY NUMBER , ADD THE DAY NUMBER AND THE
									// ACTIVIDAD LIST ---
									if (!grupoMap.containsKey(tramo.getDayNumber().trim()))
									{
										List<Actividad> temporalList = new ArrayList<>();
										temporalList.add(actv);
										grupoMap.put(tramo.getDayNumber().trim(), temporalList);

									}
									else
									{
										// --- IF THE MAP ALRREADY CONTAINS THE TRAMO DAY , GET THE ACTIVIDAD LIST AND
										// ADD THE ACTV , FINALLY PUT THEN ON THE DAY ---
										List<Actividad> temporalList = grupoMap.get(tramo.getDayNumber().trim());
										temporalList.add(actv);
										grupoMap.put(tramo.getDayNumber().trim(), temporalList);
									}
								}

								// --- IF THE MAP IS NOT EMPTY , LAUNCH THE PDF GENERATION ---
								if (!grupoMap.isEmpty())
								{

									log.info(grupoMap.toString());

									try
									{
										applicationPdf.getInfoPdfHorarioGrupoCentro( grupoMap,
												grupo.trim());

										// --- GETTING THE PDF BY NAME URL ---
										File file = new File("Horario" + grupo + ".pdf");

										// --- SETTING THE HEADERS WITH THE NAME OF THE FILE TO DOWLOAD PDF ---
										HttpHeaders responseHeaders = new HttpHeaders();

										// --- REPLACE SPACES AND º BECAUSE THAT MADE CONFLICTS ON SAVE FILE ---
										String fileName = file.getName().replace("º", "").replace(" ", "_");
										// --- SET THE HEADERS ---
										responseHeaders.set("Content-Disposition", "attachment; filename=" + fileName);

										// --- CONVERT FILE TO BYTE[] ---
										byte[] bytesArray = Files.readAllBytes(file.toPath());

										// --- RETURN OK (200) WITH THE HEADERS AND THE BYTESARRAY ---
										return ResponseEntity.ok().headers(responseHeaders).body(bytesArray);
									}
									catch (HorariosError exception)
									{
										// --- ERROR ---
										String error = "ERROR getting the info pdf ";

										log.info(error);

										HorariosError horariosError = new HorariosError(400, error, exception);
										log.info(error, horariosError);
										return ResponseEntity.status(400).body(horariosError);
									}

								}
								else
								{
									// --- ERROR ---
									String error = "ERROR grupoMap IS EMPTY OR NOT FOUND";

									log.info(error);

									HorariosError horariosError = new HorariosError(400, error, null);
									log.info(error, horariosError);
									return ResponseEntity.status(400).body(horariosError);
								}
							}
							else
							{
								// --- ERROR ---
								String error = "ERROR actividadList HAVE 0 ACTIVIDAD OR IS NULL";

								log.info(error);

								HorariosError horariosError = new HorariosError(400, error, null);
								log.info(error, horariosError);
								return ResponseEntity.status(400).body(horariosError);
							}

						}
						else
						{
							// --- ERROR ---
							String error = "ERROR horarioGrup NULL OR NOT FOUND";

							log.info(error);

							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error, horariosError);
							return ResponseEntity.status(400).body(horariosError);
						}
					}
					else
					{
						// --- ERROR ---
						String error = "ERROR GRUPO_FINAL NULL OR NOT FOUND";

						log.info(error);

						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error, horariosError);
						return ResponseEntity.status(400).body(horariosError);
					}
				
			}
			else
			{
				// --- ERROR ---
				String error = "ERROR GRUPO PARAMETER ERROR";

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

	/**
	 * Method getTeachersSchedule
	 *
	 * @return ResponseEntity , File PDF
	 */
	@RequestMapping(value = "/get/grupos/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<?> getGlobalSchedule()
	{
		try
		{
			Map<GrupoEntity, Map<String, List<Actividad>>> mapGroups = new HashMap<>();
				// --- CENTRO PDF IS LOADED---
				for (GrupoEntity grupo : this.grupoRepo.findAll())
				{
					// --- FOR EACH GRUPO ---
					HorarioGrup horarioGrup = null;
					List<HorarioGrup> horarioGrupList = new ArrayList<>();
					fillHorarioGrupoValues(horarioGrupList);
					for (HorarioGrup horarioGrp : horarioGrupList)
					{
						if (horarioGrp.getHorNumIntGr().trim().equalsIgnoreCase(grupo.getNumIntGr().trim()))
						{
							horarioGrup = horarioGrp;
						}
					}

					if (horarioGrup != null)
					{
						// --- HORARIO GRUP EXISTS ---

						// --- FOR EACH ACTIVIDAD ---
						Map<String, List<Actividad>> mapGroup = new HashMap<>();
						for (Actividad atcv : horarioGrup.getActividad())
						{
							TimeSlotEntity temporalTramo = this.extractTramoFromCentroActividad( atcv);

							if (!mapGroup.containsKey(temporalTramo.getDayNumber().trim()))
							{
								List<Actividad> temporalList = new ArrayList<>();
								temporalList.add(atcv);
								mapGroup.put(temporalTramo.getDayNumber().trim(), temporalList);
							} else
							{
								List<Actividad> temporalList = mapGroup.get(temporalTramo.getDayNumber().trim());
								temporalList.add(atcv);
								mapGroup.put(temporalTramo.getDayNumber().trim(), temporalList);
							}
						}

						// --- ADD THE PROFESSOR WITH THE PROFESSOR MAP ---
						mapGroups.put(grupo, mapGroup);
					} else
					{
						log.error("ERROR grupo " + grupo + " HORARIO grup NOT FOUND OR NULL");
					}
				}

				try
				{
					// --- USING APPLICATION PDF TO GENERATE THE PDF , WITH ALL TEACHERS ---

					applicationPdf.getAllGroupsPdfInfo(mapGroups);

					// --- GETTING THE PDF BY NAME URL ---
					File file = new File("All_Groups_Horarios.pdf");

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

	// ENDPOINT FOR GETTING COEXISTENCE ACTITUDE POINTS

	//TODO: - Notas David - Este endpointt recupera los puntos 
	// de convivencia, pero no tenemos manera de subirlos a BBDD. 
	// Debemos crear un endpoint de carga o dejarlo como estaba.
	/**
	 * Method getListPointsCoexistence

	 * 
	 * 
	 * @return ResponseEntity
	 */
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

	@RequestMapping(value = "/send/csv-alumnos", consumes = "multipart/form-data")
	public ResponseEntity<?> loadStudents(@RequestPart(name = "csvFile", required = true) MultipartFile csvFile)
	{
		try
		{
			
			byte[] content = csvFile.getBytes(); // Obtiene el contenido del archivo CSV en un arreglo de bytes.

			// Parsea el CSV y lo convierte en una lista de estudiantes.
			List<Student> estudiantes = this.studentOperation.parseStudent(content);
			
			List<StudentsEntity> estudiantesEntityList = new ArrayList<>();
			
			log.debug("Estudiantes recibidos para almacenamiento: {}", estudiantes.size());
			
			for (Student estudiante : estudiantes)
			{
				log.debug("Preparando estudiante para persistencia: {}", estudiante);
				StudentsEntity estudianteEntidad = new StudentsEntity(estudiante);
				estudiantesEntityList.add(estudianteEntidad);
			}
			// Guarda el listado definido en BBDD.
			this.iStudentsRepo.saveAllAndFlush(estudiantesEntityList);

			return ResponseEntity.ok().body(estudiantes); // Devuelve los datos de los estudiantes procesados.
		}
		catch (HorariosError exception)
		{
			log.error("El fichero introducido no contiene los datos de los alumnos bien formados", exception);
			return ResponseEntity.status(406).body(exception.toMap()); // Si hay un error específico, responde con un
																		// error 406.
		}
		catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace()); // Si ocurre un
		}
	}

	@RequestMapping(value = "/send/csv-planos", consumes = "multipart/form-data")
	public ResponseEntity<?> loadPlanos(@RequestPart(name = "csvFile", required = true) MultipartFile csvFile)
	{
		try
		{
			byte[] content = csvFile.getBytes();
			if (!csvFile.getOriginalFilename().endsWith(".csv"))
			{
				throw new HorariosError(406, "El fichero no es un csv");
			}
			List <AulaPlano> aulasListado = this.util.parseAulasPlano(content);
			
			List<AulaPlanoEntity> aulasPlanosEntity = new ArrayList<>();
			
			// Por cada aula parseada lo convierte en entidad y lo agrega a la lista, luego guarda la lista.
			for ( AulaPlano aulaActual : aulasListado )
			{
				AulaPlanoEntity aulaPlanoEntity = new AulaPlanoEntity();
				
				Optional<AulaEntity> aulaEntity = aulaRepo.findById(aulaActual.getAula().getNumIntAu());
				
				// Si no recupera ningun aula devuelve error.
				if ( aulaEntity.isEmpty() )
				{
					return ResponseEntity.status(400).body("El aula referenciada no existe: + aulaActual.getAula().toString()" );
				}
				
				// Setea los atributos del aula actual.
				aulaPlanoEntity.setAula(aulaEntity.get());
				aulaPlanoEntity.setHeight(aulaActual.getHeight());
				aulaPlanoEntity.setLeftSide(aulaActual.getLeft());
				aulaPlanoEntity.setPlanta(aulaActual.getPlanta());
				aulaPlanoEntity.setRightSide(aulaActual.getRight());
				aulaPlanoEntity.setTop(aulaActual.getTop());
				
				aulasPlanosEntity.add(aulaPlanoEntity);
			}
			
			// Guarad listado obtenido en BBDD.
			this.iAulaPlanoRepo.saveAllAndFlush(aulasPlanosEntity);
			
			return ResponseEntity.ok().body(aulas); // Informa mediante DTO del listado parseado.
		}
		catch (HorariosError exception)
		{
			log.error("El fichero introducido no contiene los datos de los planos bien formados", exception);
			return ResponseEntity.status(406).body(exception.toMap());
		}
		catch (Exception exception)
		{
			log.error("Error de servidor", exception);
			return ResponseEntity.status(500).body("Error de servidor " + exception.getStackTrace());
		}
	}

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

	// TODO : lo que he arreglado es lo siguiente: 
	// TODO : Nota para Jose -> Probando en Postman produce error. 
	// Dice que debe de asignarse un ID para el error. 
	// Esto probablemente porque el objeto espera un ID manual no uno auto-generado. 
	// Como identificador de error tipo 402 403 etc.. 
	// -> Debemos arreglar la correspondencia de atributos / datos entre DTO y Entidad
	@RequestMapping(value = "/send/error-info", consumes = "application/json")
	public ResponseEntity<?> sendErrorInfo(@RequestBody(required = false) InfoError objectError)
	{
	    try {
	        // Validar si el objeto recibido es nulo
	        if (objectError == null) {
	            return ResponseEntity.badRequest().body("El cuerpo de la solicitud no puede estar vacío.");
	        }

	        // Validar si el ID está presente
	        if (objectError.getId() == null) {
	            return ResponseEntity.badRequest().body("El campo 'id' es obligatorio y debe asignarse manualmente.");
	        }

	        // Convertir el DTO InfoError a la entidad InfoErrorEntity
	        InfoErrorEntity infoErrorEntity = new InfoErrorEntity();
	        infoErrorEntity.setId(objectError.getId()); // Asignar el ID manual proporcionado
	        infoErrorEntity.setHeaderInfo(objectError.getHeaderInfo());
	        infoErrorEntity.setInfoError(objectError.getInfoError());
	        infoErrorEntity.setWait(objectError.getWait());

	        // Guardar en la base de datos usando el repositorio
	        this.iInfoErrorRepo.saveAndFlush(infoErrorEntity);

	        // Devuelve una respuesta exitosa (HTTP 200)
	        return ResponseEntity.ok().build();
	    }
	    catch (Exception exception)
	    {
	        log.error("Error al almacenar la información del error", exception);
	        return ResponseEntity.status(500).body("Error de servidor: " + exception.getMessage());
	    }
	}

	@RequestMapping(value = "/get/error-info", produces = "application/json")
	public ResponseEntity<?> getInfoError()
	{
		try
		{
			List<InfoErrorEntity> errorInfoList = this.iInfoErrorRepo.findAll();

			return ResponseEntity.ok().body(errorInfoList);

		}
		catch (Exception exception)
		{
			log.error("Error al obtner la informacion del error", exception);
			return ResponseEntity.status(500).body("Error al obtner la informacion");
		}

	}

	// TODO: Lo que he arreglado: 
	// TODO: Revisitar este bloque de control para los errores.
			// Se puede mejorar haciendo que devuelva un mensaje mas inteligente. 
			// Haciendo una concatenación en la cadena que define los errores. 
			// Actualmente si el XML no está cargado no avisa de los demas errores.
			// Si el XML si está cargado pero el resto no solo informa de los estudiantes. 
			// No refleja la situación precisa del servidor.
	@RequestMapping(value = "/check-data", produces = "application/json")
	public ResponseEntity<?> checkServerData() 
	{
	    Map<String, Object> responseMap = new HashMap<>();

	    Long contadorActividad = this.actividadRepo.count();
	    Long contadorStudents = this.iStudentsRepo.count();
	    Long contadorAulas = aulaRepo.count();

	    // Construir una lista de errores
	    List<String> errores = new ArrayList<>();

	    if (contadorActividad == null || contadorActividad == 0)
	    {
	        errores.add("Error de datos en actividades.");
	    }

	    if (contadorStudents == null || contadorStudents == 0)
	    {
	        errores.add("Error de datos en estudiantes.");
	    }

	    if (contadorAulas == null || contadorAulas == 0)
	    {
	        errores.add("Error de datos en aulas.");
	    }

	    // Si hay errores, devolverlos concatenados
	    if (!errores.isEmpty())
	    {
	        responseMap.put("status", "error");
	        responseMap.put("errores", errores);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
	    }

	    // Si no hay errores, indicar que todo está correcto
	    responseMap.put("status", "ok");
	    responseMap.put("mensaje", "Todos los datos están correctamente cargados.");
	    return ResponseEntity.ok(responseMap);
	}

	// TODO: Cambiar por llamadas a BBDD.
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

	// TODO: Revisar por que devuelve error "NO AUTORIZADO" al hacer petición en Postman.
	@RequestMapping(method = RequestMethod.POST, value = "/send/sancion", consumes = "application/json")
	public ResponseEntity<?> sendSancion(@RequestBody(required = true) Student student,


			@RequestParam(name = "value", required = true) Integer value,
			@RequestParam(name = "description", required = true) String description)
	{
		try
		{
			Optional<StudentsEntity> studentsEntityOpt = this.iStudentRepository.findByNameAndLastNameAndCourse(student.getName(), student.getLastName(), student.getCourse()) ;
			
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
	
	// TODO: Ver como interactuar con esto mediante postman. NO entiendo que se espera esta petición por parte del cliente. 
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
