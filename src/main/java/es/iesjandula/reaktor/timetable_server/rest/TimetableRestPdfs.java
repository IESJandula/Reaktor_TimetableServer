package es.iesjandula.reaktor.timetable_server.rest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlotEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Actividad;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioGrup;
import es.iesjandula.reaktor.timetable_server.models.parse.HorarioProf;
import es.iesjandula.reaktor.timetable_server.repository.IActividadRepository;
import es.iesjandula.reaktor.timetable_server.repository.IGrupoRepository;
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
@RequestMapping("/horarios/pdfs")
@Slf4j
public class TimetableRestPdfs {
	

	/** Clase que se encarga de las operaciones logicas del servidor */
	@Autowired
	TimeTableUtils util;
	
	@Autowired
	ApplicationPdf applicationPdf;
	
	@Autowired
	private IStudentsRepository iStudentsRepository;



	// --------------------------- JAYDEE

	@Autowired
	private IGrupoRepository grupoRepo;



	@Autowired
	private IProfesorRepository profesorRepo;

	@Autowired
	private ITimeSlotRepository timeslotRepo;

	@Autowired
	private IActividadRepository actividadRepo;



	

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
	
	
	//---------------methods---------------

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


}
