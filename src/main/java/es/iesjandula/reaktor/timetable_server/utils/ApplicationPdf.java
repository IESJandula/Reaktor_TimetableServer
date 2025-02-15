package es.iesjandula.reaktor.timetable_server.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.entities.AsignaturaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.AulaEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlotEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Actividad;
import es.iesjandula.reaktor.timetable_server.repository.IActividadRepository;
import es.iesjandula.reaktor.timetable_server.repository.IAsignaturaRepository;
import es.iesjandula.reaktor.timetable_server.repository.IAulaRepository;
import es.iesjandula.reaktor.timetable_server.repository.IProfesorRepository;
import es.iesjandula.reaktor.timetable_server.repository.ITimeSlotRepository;
import lombok.extern.slf4j.Slf4j;


/**
 * This class is created for the conflicts with Document class of XML Parser and the iText dependency
 * 
 * @author David Martinez
 * 
 * Example how to use:
 * 		try
		{
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream("Example.pdf"));
			document.open();
			
			Font font = FontFactory.getFont(FontFactory.COURIER_BOLDOBLIQUE, 16, BaseColor.RED);
			document.add(new Paragraph("HORARIO PROFESOR", font));
			
			PdfPTable pdfTable = new PdfPTable(1);
			
			pdfTable.addCell("Primera celda"); // Use AddCell to Add a string data in first Cell

			document.add(pdfTable);                       
			document.close();
			
		}
		catch (FileNotFoundException | DocumentException e)
		{
	
		}
 *
 */
@Slf4j
@Service
public class ApplicationPdf
{
	@Autowired
	ITimeSlotRepository iTimeSlotRepository;
	
	@Autowired
	IActividadRepository iActividadRepository;
	
	@Autowired
	IAulaRepository iAulaRepository;
	
	@Autowired
	IProfesorRepository iProfesorRepository;
	
	@Autowired
	IAsignaturaRepository iAsignaturaRepository;
	
	/**
	 * Method getInfoPdf get hte professor info and make PDF
	 * @param centro
	 * @param profesorMap
	 * @param profesor
	 * @throws HorariosError 
	 */
	public void getInfoPdf( Map<String,List<Actividad>> profesorMap,ProfesorEntity profesor) throws HorariosError 
	{
		try 
		{
			// --- CREATING THE DOCUMENT FOR PDF ---
			Document document = new Document();
			
			// --- ROTATE THE DOCUMENT HORIZONTAL MODE ---
			document.setPageSize(PageSize.A4.rotate());
			
			// --- CREATING INSTANCE OF PDFWRITER
			PdfWriter.getInstance(document, new FileOutputStream(profesor.getNombre().trim()+"_"+profesor.getPrimerApellido().trim()+"_"+profesor.getSegundoApellido()+"_Horario.pdf"));
			// --- OPEN THE DOCUMENT TO WORK ---
			document.open();
			
			// --- FONT FOR THE PARAGRAPH ---
			Font font = FontFactory.getFont(FontFactory.COURIER_BOLDOBLIQUE, 12, BaseColor.RED);
			document.add(new Paragraph("HORARIO PROFESOR "+profesor.getNombre().trim()+" "+profesor.getPrimerApellido().trim()+" "+profesor.getSegundoApellido().trim(), font));
			document.add(new Paragraph("\n"));
			
			// --- CREATE THE PDF TABLE ---
			PdfPTable pdfTable = new PdfPTable(profesorMap.size());
			
			// --- SET THE WIDTH OF TABLE TO 100% WITH FLOAT ---
			pdfTable.setWidthPercentage(100f);
			
			// --- FOR EACH ENTRY ON THE PROFESSOR MAP ---
			for(Map.Entry<String,List<Actividad>> set :profesorMap.entrySet()) 
			{
				// --- GET THE DAY (LUNES,MARTES... ) FROM THE KEY NUMBER ---
				String temporalDay = this.extractTemporalDayTramo(set.getKey());
				log.info("TRAMO DIA: "+temporalDay);
				
				// --- OTHER FONT FOR THE CELL TEXT ---
				Font fontCell = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
				
				// --- CREATE THE CELL ---
				PdfPCell temporalDayCell = new PdfPCell();
				
				// --- PUT THE PARAGRAPH WITH THE FONT ON THE CELL ---
				temporalDayCell.addElement(new Paragraph(temporalDay,fontCell));
				
				// --- SET COLOR TO THE CELL (HEADERS LUNES MARTES ...) ---
				temporalDayCell.setBackgroundColor(BaseColor.CYAN);
				
				// --- SET TEH VERTICAL ALIG ON CENTER TO THE TEXT ON THE CELL ---
				temporalDayCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				
				// --- HORIZONTAL ALIG ON THE CENTER ---
				temporalDayCell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
				
				// FINALLY ADD THE CELL TO HTE TABLE ---
				pdfTable.addCell(temporalDayCell);
				
				// --- NOW FOR EACH ACTIVIDAD ---
				for(Actividad act : set.getValue()) 
				{
					// -- GET THE TRAMO OBJECT FROM THE ACTIVIDAD ---
					TimeSlotEntity temporalTramo = this.extractTramoFromCentroActividad( act);
					if(temporalTramo!=null) 
					{
						// --- GET THE TIME START AND END FROM THE TEMPORAL_TRAMO ---
						String temporalHourTime = temporalTramo.getStartHour().trim()+" - "+temporalTramo.getEndHour().trim();	
						
						log.info(temporalHourTime);
						
						// --- GET THE ASIGNATURA FROM ACTIVIDAD BY ID ---
						AsignaturaEntity temporalAsignatura = this.getAsignaturaById(act.getAsignatura().trim());
						if(temporalAsignatura!=null) 
						{
							log.info("ASIGNATURA: "+temporalAsignatura.getNombre().trim());
							
							//--- GET THE AULA FROM ACTIVIDAD BY ID ---
							AulaEntity temporalAula = this.getAulaById(act.getAula().trim());
							if(temporalAula!=null) 
							{
								log.info("AULA : "+temporalAula.getNombre().trim());
								
								// --- CREATE THE FONT FOR THE CELL ---
								fontCell = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
								
								// --- CREATE TEH CELL WITH ALL THE INFO ---
								PdfPCell temporalData = new PdfPCell();
								
								// --- PUT THE INFO INTO THE CELL WITH PARAGRAPH ---
								temporalData.addElement(new Paragraph(temporalHourTime+"\n"+temporalAsignatura.getNombre().trim()+"\n"+temporalAula.getNombre().trim(),fontCell));
								
								//--- SET COLOR TO THE CELL ---
								temporalData.setBackgroundColor(BaseColor.LIGHT_GRAY);
								
								// --- SET THE HEIGHT FOR THE CELL ---
								temporalData.setCalculatedHeight(90f);
								
								// --- FINALLY PUT THE CELL ON THE TABLE AND REPEAT WITH ALL ACTIVIDADES ---
								pdfTable.addCell(temporalData);
							}
							else 
							{
								// --- ERROR ---
								String error = "ERROR temporalAula NULL OR NOT FOUND";
								
								log.info(error);
								
								HorariosError horariosError = new HorariosError(400, error, null);
								log.info(error,horariosError);
								throw horariosError;

							}
						}
						else 
						{
							// --- ERROR ---
							String error = "ERROR temporalAsignatura NULL OR NOT FOUND";
							
							log.info(error);
							
							HorariosError horariosError = new HorariosError(400, error, null);
							log.info(error,horariosError);
							throw horariosError;
						}
	
					}
					else 
					{
						// --- ERROR ---
						String error = "ERROR temporalTramo NULL OR NOT FOUND";
						
						log.info(error);
						
						HorariosError horariosError = new HorariosError(400, error, null);
						log.info(error,horariosError);
						throw horariosError;
					}
				}
				pdfTable.completeRow();
				
			}
			document.add(pdfTable);
			document.close();
		}
		catch (FileNotFoundException exception)
		{
			// --- ERROR ---
			String error = "ERROR FileNotFoundException";
			
			log.info(error);
			
			HorariosError horariosError = new HorariosError(400, error, exception);
			log.info(error,horariosError);
			throw horariosError;
		}
		catch (DocumentException exception)
		{
			// --- ERROR ---
			String error = "ERROR DocumentException";
			
			log.info(error);
			
			HorariosError horariosError = new HorariosError(400, error, exception);
			log.info(error,horariosError);
			throw horariosError;
		}
		
	}

	/**
	 * Method getAulaById
	 * @param id
	 * @param centro
	 * @return
	 */
	private AulaEntity getAulaById(String id)
	{
		AulaEntity aula = null;
		for(AulaEntity aul : this.iAulaRepository.findAll()) 
		{
			if(aul.getNumIntAu().trim().equalsIgnoreCase(id.trim())) 
			{
				aula = aul;
			}	
		}
		
		return aula;
	}

	/**
	 * Method getAsignaturaById
	 * @param id
	 * @param centro
	 * @return
	 */
	private AsignaturaEntity getAsignaturaById(String id)
	{
		AsignaturaEntity asignatura = null;
		for(AsignaturaEntity asig : this.iAsignaturaRepository.findAll()) 
		{
			if(asig.getNumIntAs().trim().equalsIgnoreCase(id.trim())) 
			{
				asignatura = asig;
			}
		}
		return asignatura;
	}

	/**
	 * Method extractTemporalDayTramo
	 * @param key
	 * @return
	 */
	private String extractTemporalDayTramo(String key)
	{
		String finalString = null;
		switch(key) 
		{
			case "1":
			{
				finalString = "Lunes";
				break;
			}
			case "2":
			{
				finalString = "Martes";
				break;
			}
			case "3":
			{
				finalString = "Miercoles";
				break;
			}
			case "4":
			{
				finalString = "Jueves";
				break;
			}
			case "5":
			{
				finalString = "Viernes";
				break;
			}
			case "6":
			{
				finalString = "Sabado";
				break;
			}
			case "7":
			{
				finalString = "Domingo";
				break;
			}
			default:
			{
				finalString = "Desconocido";
				break;
			}
		}
		return finalString;
	}
	/**
	 * Method extractTramoFromCentroActividad
	 * @param centro
	 * @param actividad
	 * @param tramo
	 * @return
	 */
	private TimeSlotEntity extractTramoFromCentroActividad( Actividad actividad)
	{
		for(TimeSlotEntity tram : this.iTimeSlotRepository.recuperaListadoTramosHorariosEntity()) 
		{
			// --- GETTING THE TRAMO ---
			if(actividad.getTramo().trim().equalsIgnoreCase(tram.getNumTr().trim())) 
			{
				return tram;
			}
		}
		return null;
	}

	/**
	 * Method getInfoPdfHorarioGrupoCentro
	 * @param centroPdfs
	 * @param grupoMap
	 * @throws HorariosError 
	 */
	public void getInfoPdfHorarioGrupoCentro( Map<String, List<Actividad>> grupoMap, String grupo) throws HorariosError
	{

		try
		{
			// --- CREATING THE DOCUMENT FOR PDF ---
			Document document = new Document();
			
			// --- ROTATE THE DOCUMENT HORIZONTAL MODE ---
			document.setPageSize(PageSize.A4.rotate());
			
			// --- CREATING INSTANCE OF PDFWRITER
			PdfWriter.getInstance(document, new FileOutputStream("Horario"+grupo+".pdf"));
			// --- OPEN THE DOCUMENT TO WORK ---
			document.open();
			
			// --- FONT FOR THE PARAGRAPH ---
			Font font = FontFactory.getFont(FontFactory.COURIER_BOLDOBLIQUE, 12, BaseColor.RED);
			document.add(new Paragraph("HORARIO GRUPO "+grupo.trim(), font));
			document.add(new Paragraph("\n"));
			
			// --- CREATE THE PDF TABLE ---
			PdfPTable pdfTable = new PdfPTable(grupoMap.size());
			
			// --- SET THE WIDTH OF TABLE TO 100% WITH FLOAT ---
			pdfTable.setWidthPercentage(100f);
			

			for(Map.Entry entry : grupoMap.entrySet()) 
			{
				String temporalDay = this.extractTemporalDayTramo(entry.getKey().toString());
				log.info("DIA -> "+temporalDay);
				
				// --- OTHER FONT FOR THE CELL TEXT ---
				Font fontCell = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
				
				// --- CREATE THE CELL ---
				PdfPCell temporalDayCell = new PdfPCell();
				
				// --- PUT THE PARAGRAPH WITH THE FONT ON THE CELL ---
				temporalDayCell.addElement(new Paragraph(temporalDay,fontCell));
				
				// --- SET COLOR TO THE CELL (HEADERS LUNES MARTES ...) ---
				temporalDayCell.setBackgroundColor(BaseColor.CYAN);
				
				// --- SET TEH VERTICAL ALIG ON CENTER TO THE TEXT ON THE CELL ---
				temporalDayCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				
				// --- HORIZONTAL ALIG ON THE CENTER ---
				temporalDayCell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
				
				// FINALLY ADD THE CELL TO HTE TABLE ---
				pdfTable.addCell(temporalDayCell);
				
				
				List<Actividad> temporalList = (List<Actividad>) entry.getValue();
				Collections.sort(temporalList);
				for(Actividad actv : temporalList) 
				{
					TimeSlotEntity temporalTramo = this.extractTramoFromCentroActividad( actv);
					
					String horaInicio = temporalTramo.getStartHour().trim();
					String horaFinal = temporalTramo.getEndHour().trim();
					
					AsignaturaEntity asignatura = this.getAsignaturaById(actv.getAsignatura().trim());
					
					String nombreAsignatura = asignatura.getNombre().trim();
					
					ProfesorEntity profesor = this.getProfesorById(actv.getProfesor().trim());
					
					String nombreProfesor = profesor.getNombre().trim()+" "+profesor.getPrimerApellido().trim()+" "+profesor.getSegundoApellido().trim();
					
					log.info(" ASIGNATURA : "+nombreAsignatura);
					log.info("HORARIO : "+horaInicio+" "+horaFinal);
					log.info("PROFESOR : "+nombreProfesor);
					
					
					// --- CREATE THE FONT FOR THE CELL ---
					fontCell = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
					
					// --- CREATE TEH CELL WITH ALL THE INFO ---
					PdfPCell temporalData = new PdfPCell();
					
					// --- PUT THE INFO INTO THE CELL WITH PARAGRAPH ---
					temporalData.addElement(new Paragraph(nombreAsignatura+"\n"+horaInicio+"-"+horaFinal+"\n"+nombreProfesor,fontCell));
					
					//--- SET COLOR TO THE CELL ---
					temporalData.setBackgroundColor(BaseColor.LIGHT_GRAY);
					
					// --- SET THE HEIGHT FOR THE CELL ---
					temporalData.setCalculatedHeight(90f);
					
					// --- FINALLY PUT THE CELL ON THE TABLE AND REPEAT WITH ALL ACTIVIDADES ---
					pdfTable.addCell(temporalData);
					
					
				}
				pdfTable.completeRow();
			}
			document.add(pdfTable);
			document.close();
			
			
		}
		catch (FileNotFoundException exception)
		{
			// --- ERROR ---
			String error = "ERROR FileNotFoundException";
			
			log.info(error);
			
			HorariosError horariosError = new HorariosError(400, error, exception);
			log.info(error,horariosError);
			throw horariosError;
		}
		catch (DocumentException exception)
		{
			// --- ERROR ---
			String error = "ERROR DocumentException";
			
			log.info(error);
			
			HorariosError horariosError = new HorariosError(400, error, exception);
			log.info(error,horariosError);
			throw horariosError;
		}
	}

	/**
	 * Method getProfesorById
	 * @param trim
	 * @param centroPdfs
	 * @return
	 */
	private ProfesorEntity getProfesorById(String id)
	{
		for(ProfesorEntity profesor : this.iProfesorRepository.findAll()) 
		{
			if(profesor.getNumIntPR().trim().equalsIgnoreCase(id.trim()))
			{
				return profesor;
			}
		}
		return null;
	}

	/**
	 * Method getAllTeachersPdfInfo
	 * @param mapProfesors
	 * @throws HorariosError 
	 */
	public void getAllTeachersPdfInfo(Map<ProfesorEntity, Map<String, List<Actividad>>> mapProfesors) throws HorariosError
	{
		try
		{
			Document document = new Document();
			
			// --- ROTATE THE DOCUMENT HORIZONTAL MODE ---
			document.setPageSize(PageSize.A4.rotate());
			
			PdfWriter.getInstance(document, new FileOutputStream("All_Teachers_Horarios.pdf"));
			document.open();
			
			PdfPTable pdfTable = null;
			for(Map.Entry entry : mapProfesors.entrySet()) 
			{
				System.out.println(((ProfesorEntity)entry.getKey()));
				
				// --- GETTING THE PROFESSOR ---
				ProfesorEntity profesor = ((ProfesorEntity)entry.getKey());
				
				// --- GETTING THE MAP OF THE PROFESSOR ---
				Map<String,List<Actividad>> mapa = (Map<String,List<Actividad>>)entry.getValue();
				
				// --- FONT FOR THE PARAGRAPH ---
				Font font = FontFactory.getFont(FontFactory.COURIER_BOLDOBLIQUE, 12, BaseColor.RED);
				document.add(new Paragraph("HORARIO "+profesor.getNombre().trim()+" "+profesor.getPrimerApellido().trim()+" "+profesor.getSegundoApellido().trim(), font));
				document.add(new Paragraph("\n"));
				
				// --- CREATE THE PDF TABLE ---
				pdfTable = new PdfPTable(mapa.size());
				
				// --- SET THE WIDTH OF TABLE TO 100% WITH FLOAT ---
				pdfTable.setWidthPercentage(100f);
				
				// --- FOR EACH ENTRY ON THE MAPA ---
				for(Map.Entry entry2 : mapa.entrySet()) 
				{
					String temporalDay = this.extractTemporalDayTramo(entry2.getKey().toString());
					log.info("DIA -> "+temporalDay);
					
					// --- OTHER FONT FOR THE CELL TEXT ---
					Font fontCell = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
					
					// --- CREATE THE CELL ---
					PdfPCell temporalDayCell = new PdfPCell();
					
					// --- PUT THE PARAGRAPH WITH THE FONT ON THE CELL ---
					temporalDayCell.addElement(new Paragraph(temporalDay,fontCell));
					
					// --- SET COLOR TO THE CELL (HEADERS LUNES MARTES ...) ---
					temporalDayCell.setBackgroundColor(BaseColor.CYAN);
					
					// --- SET TEH VERTICAL ALIG ON CENTER TO THE TEXT ON THE CELL ---
					temporalDayCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					
					// --- HORIZONTAL ALIG ON THE CENTER ---
					temporalDayCell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
					
					// FINALLY ADD THE CELL TO HTE TABLE ---
					pdfTable.addCell(temporalDayCell);
					
					
					List<Actividad> temporalList = (List<Actividad>) entry2.getValue();
					Collections.sort(temporalList);
					
					// --- FOR EACH ACTIVIDAD ---
					for(Actividad actv : temporalList) 
					{
						// --- GET THE TRAMO OF THE ACTIVIDAD ---
						TimeSlotEntity temporalTramo = this.extractTramoFromCentroActividad( actv);
						
						String horaInicio = temporalTramo.getStartHour().trim();
						String horaFinal = temporalTramo.getEndHour().trim();
						
						AsignaturaEntity asignatura = this.getAsignaturaById(actv.getAsignatura().trim());
						AulaEntity aula = this.getAulaById(actv.getAula().trim());
						
						String nombreAsignatura = asignatura.getNombre().trim();
						
						String nombreAula = aula.getNombre().trim();
						
						log.info(" ASIGNATURA : "+nombreAsignatura);
						log.info("HORARIO : "+horaInicio+" "+horaFinal);
						log.info("AULA : "+nombreAula);
						
						
						// --- CREATE THE FONT FOR THE CELL ---
						fontCell = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
						
						// --- CREATE TEH CELL WITH ALL THE INFO ---
						PdfPCell temporalData = new PdfPCell();
						
						// --- PUT THE INFO INTO THE CELL WITH PARAGRAPH ---
						temporalData.addElement(new Paragraph(nombreAsignatura+"\n"+horaInicio+"-"+horaFinal+"\n"+nombreAula,fontCell));
						
						//--- SET COLOR TO THE CELL ---
						temporalData.setBackgroundColor(BaseColor.LIGHT_GRAY);
						
						// --- SET THE HEIGHT FOR THE CELL ---
						temporalData.setCalculatedHeight(90f);
						
						// --- FINALLY PUT THE CELL ON THE TABLE AND REPEAT WITH ALL ACTIVIDADES ---
						pdfTable.addCell(temporalData);
						
						
					}
					pdfTable.completeRow();
				}
				document.add(pdfTable);
				document.newPage();
			}
			document.close();
		}
		catch (FileNotFoundException exception)
		{
			// --- ERROR ---
			String error = "ERROR FileNotFoundException";
			
			log.info(error);
			
			HorariosError horariosError = new HorariosError(400, error, exception);
			log.info(error,horariosError);
			throw horariosError;
		}
		catch (DocumentException exception)
		{
			// --- ERROR ---
			String error = "ERROR DocumentException";
			
			log.info(error);
			
			HorariosError horariosError = new HorariosError(400, error, exception);
			log.info(error,horariosError);
			throw horariosError;
		}
	}

	/**
	 * Method getAllGroupsPdfInfo
	 * @param mapGroups
	 * @param centroPdfs
	 * @throws HorariosError 
	 */
	public void getAllGroupsPdfInfo(Map<GrupoEntity, Map<String, List<Actividad>>> mapGroups) throws HorariosError
	{
		try
		{
			Document document = new Document();
			
			// --- ROTATE THE DOCUMENT HORIZONTAL MODE ---
			document.setPageSize(PageSize.A4.rotate());
			
			PdfWriter.getInstance(document, new FileOutputStream("All_Groups_Horarios.pdf"));
			document.open();
			
			PdfPTable pdfTable = null;
			for(Map.Entry entry : mapGroups.entrySet()) 
			{
				System.out.println(((GrupoEntity)entry.getKey()));
				
				// --- GETTING THE PROFESSOR ---
				GrupoEntity grupo = ((GrupoEntity)entry.getKey());
				
				// --- GETTING THE MAP OF THE PROFESSOR ---
				Map<String,List<Actividad>> mapa = (Map<String,List<Actividad>>)entry.getValue();
				
				// --- FONT FOR THE PARAGRAPH ---
				Font font = FontFactory.getFont(FontFactory.COURIER_BOLDOBLIQUE, 12, BaseColor.RED);
				document.add(new Paragraph("HORARIO "+grupo.getNombre(), font));
				document.add(new Paragraph("\n"));
				
				// --- CREATE THE PDF TABLE ---
				pdfTable = new PdfPTable(mapa.size());
				
				// --- SET THE WIDTH OF TABLE TO 100% WITH FLOAT ---
				pdfTable.setWidthPercentage(100f);
				
				// --- FOR EACH ENTRY ON THE MAPA ---
				for(Map.Entry entry2 : mapa.entrySet()) 
				{
					String temporalDay = this.extractTemporalDayTramo(entry2.getKey().toString());
					log.info("DIA -> "+temporalDay);
					
					// --- OTHER FONT FOR THE CELL TEXT ---
					Font fontCell = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
					
					// --- CREATE THE CELL ---
					PdfPCell temporalDayCell = new PdfPCell();
					
					// --- PUT THE PARAGRAPH WITH THE FONT ON THE CELL ---
					temporalDayCell.addElement(new Paragraph(temporalDay,fontCell));
					
					// --- SET COLOR TO THE CELL (HEADERS LUNES MARTES ...) ---
					temporalDayCell.setBackgroundColor(BaseColor.CYAN);
					
					// --- SET TEH VERTICAL ALIG ON CENTER TO THE TEXT ON THE CELL ---
					temporalDayCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					
					// --- HORIZONTAL ALIG ON THE CENTER ---
					temporalDayCell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
					
					// FINALLY ADD THE CELL TO HTE TABLE ---
					pdfTable.addCell(temporalDayCell);
					
					
					List<Actividad> temporalList = (List<Actividad>) entry2.getValue();
					Collections.sort(temporalList);
					
					// --- FOR EACH ACTIVIDAD ---
					for(Actividad actv : temporalList) 
					{
						// --- GET THE TRAMO OF THE ACTIVIDAD ---
						TimeSlotEntity temporalTramo = this.extractTramoFromCentroActividad( actv);
						
						String horaInicio = temporalTramo.getStartHour().trim();
						String horaFinal = temporalTramo.getEndHour().trim();
						
						AsignaturaEntity asignatura = this.getAsignaturaById(actv.getAsignatura().trim());
						ProfesorEntity profesor = this.getProfesorById(actv.getProfesor().trim());
						
						String nombreAsignatura = asignatura.getNombre().trim();
						
						String nombreProfesor = profesor.getNombre().trim()+" "+profesor.getPrimerApellido().trim()+" "+profesor.getSegundoApellido().trim();
						
						log.info(" ASIGNATURA : "+nombreAsignatura);
						log.info("HORARIO : "+horaInicio+" "+horaFinal);
						log.info("PROFESOR : "+nombreProfesor);
						
						
						// --- CREATE THE FONT FOR THE CELL ---
						fontCell = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
						
						// --- CREATE TEH CELL WITH ALL THE INFO ---
						PdfPCell temporalData = new PdfPCell();
						
						// --- PUT THE INFO INTO THE CELL WITH PARAGRAPH ---
						temporalData.addElement(new Paragraph(nombreAsignatura+"\n"+horaInicio+"-"+horaFinal+"\n"+nombreProfesor,fontCell));
						
						//--- SET COLOR TO THE CELL ---
						temporalData.setBackgroundColor(BaseColor.LIGHT_GRAY);
						
						// --- SET THE HEIGHT FOR THE CELL ---
						temporalData.setCalculatedHeight(90f);
						
						// --- FINALLY PUT THE CELL ON THE TABLE AND REPEAT WITH ALL ACTIVIDADES ---
						pdfTable.addCell(temporalData);
							
					}
					pdfTable.completeRow();
				}
				document.add(pdfTable);
				document.newPage();
			}
			document.close();
		}
		catch (FileNotFoundException exception)
		{
			// --- ERROR ---
			String error = "ERROR FileNotFoundException";
			
			log.info(error);
			
			HorariosError horariosError = new HorariosError(400, error, exception);
			log.info(error,horariosError);
			throw horariosError;
		}
		catch (DocumentException exception)
		{
			// --- ERROR ---
			String error = "ERROR DocumentException";
			
			log.info(error);
			
			HorariosError horariosError = new HorariosError(400, error, exception);
			log.info(error,horariosError);
			throw horariosError;
		}
	}
}
