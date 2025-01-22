package es.iesjandula.reaktor.timetable_server.rest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import es.iesjandula.reaktor.timetable_server.models.InfoError;
import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.entities.ActividadEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AsignaturaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaPlanoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.InfoErrorEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlotEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.AulaPlano;
import es.iesjandula.reaktor.timetable_server.models.parse.Centro;
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
import es.iesjandula.reaktor.timetable_server.utils.StudentOperation;
import es.iesjandula.reaktor.timetable_server.utils.TimeTableUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author David Martinez, Pablo Ruiz Canovas
 */
@RestController
@RequestMapping("/horarios/admin")
@Slf4j
public class TimetableRestAdmin {
	

	/** Clase que se encarga de las operaciones logicas del servidor */
	@Autowired
	TimeTableUtils util;
	
	@Autowired
	ApplicationPdf applicationPdf;

	/** Clase que se encarga de gestionar las operaciones con los estudiantes */
	@Autowired
	private StudentOperation studentOperation;
	
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
	private IInfoErrorRepository iInfoErrorRepo;

	@Autowired
	private IStudentsRepository iStudentsRepo;
	
	@Autowired
	private IAulaPlanoRepository iAulaPlanoRepo;

	
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
}
