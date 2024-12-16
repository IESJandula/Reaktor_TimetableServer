package es.iesjandula.reaktor.timetable_server.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.timetable_server.models.entities.TimeSlotEntity;
import es.iesjandula.reaktor.timetable_server.repository.ITimeSlotRepository;

@Service
public class TimeOperations 
{
	
	@Autowired
	ITimeSlotRepository timeslotRepo;
	
	/**Logger de la clase */
	private static Logger log = LogManager.getLogger();
	
	/**
	 * Metodo que obtiene el tramo actual del centro usando la hora actual
	 *
	 * @param centro
	 * @param actualTime
	 * @param tramoActual
	 * @return tramo actual
	 */
	/**
	 * Method gettingTramoActual
	 *
	 * @param centro
	 * @param actualTime
	 * @param tramoActual
	 * @return
	 */
	public TimeSlotEntity gettingTramoActual(String actualTime )
	{
		TimeSlotEntity tramoActual = null;
		// Recupera listado de tramos de BBDD
		List<TimeSlotEntity> tramosLista = this.timeslotRepo.recuperaListadoTramosHorariosEntity();

		for (TimeSlotEntity tramo : tramosLista)
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
	 * Metodo que transforma la fecha en entero a una fecha en string añadiendo
	 * 0 en la fecha en caso de que un valor del entero este comprendido entre
	 * 1 y 9
	 * @param dateInt
	 * @return fecha en formato string
	 */
	protected String transformDate(int[]fechaInt)
	{
		String fechaString = "";
		
		if((fechaInt[0]>0 && fechaInt[0]<10) && (fechaInt[1]>0 && fechaInt[1]<10))
		{
			fechaString = "0"+fechaInt[0]+"/0"+fechaInt[1]+"/"+fechaInt[2];
		}
		else if(fechaInt[0]>0 && fechaInt[0]<10)
		{
			fechaString = "0"+fechaInt[0]+"/"+fechaInt[1]+"/"+fechaInt[2];
		}
		else if(fechaInt[1]>0 && fechaInt[1]<10)
		{
			fechaString = fechaInt[0]+"/0"+fechaInt[1]+"/"+fechaInt[2];
		}
		else
		{
			fechaString = fechaInt[0]+"/"+fechaInt[1]+"/"+fechaInt[2];
		}
		
		return fechaString;
	}
	
	/**
	 * Metodo que transforma la hora de entero a una hora en string añadiendo
	 * 0 en la hora en caso de que un valor del entero este comprendido entre
	 * 0 y 9
	 * @param hours
	 * @param minutes
	 * @return hora en formato string
	 */
	protected String transformHour(int hours,int minutes)
	{
		String horaString = "";
		if(hours<10)
		{
			horaString = "0"+hours;
		}
		else
		{
			horaString = String.valueOf(hours);
		}
		
		if(minutes<10)
		{
			horaString+=":0"+minutes;
		}
		else
		{
			horaString+=":"+minutes;
		}
		
		return horaString;
	}
	
	/**
	 * Metodo que comprueba que dos fechas sean iguales para recoger los
	 * datos de la fecha en la que un alumno fue al baño
	 * @param fecha
	 * @param fechaReal
	 * @return true si son iguales false si no
	 */
	protected boolean compareDate(String fecha,Date fechaReal)
	{
		int [] fechaInt = {fechaReal.getDate(),fechaReal.getMonth()+1,fechaReal.getYear()+1900};
		
		String otherFecha = this.transformDate(fechaInt);
		
		return fecha.equals(otherFecha);
	}
	
	/**
	 * Metodo que suma la fecha en uno y comprueba los saltos de meses y años
	 * @param dateInt
	 * @return nueva fecha en array de enteros
	 */
	protected int [] sumarDate(int[]dateInt)
	{
		int [] newFecha = null;
		
		switch(dateInt[1])
		{
			case 1,3,5,7,8,10,12:
			{
				dateInt[0]++;
				if(dateInt[0]>31)
				{
					dateInt[1]++;
					dateInt[0] = 1;
					if(dateInt[1]>12)
					{
						dateInt[2]++;
						dateInt[1] = 1;
					}
				}
				newFecha = dateInt;
				break;
			}
			case 4,6,9,11:
			{
				dateInt[0]++;
				if(dateInt[0]>31)
				{
					dateInt[1]++;
					dateInt[0] = 1;
				}
				newFecha = dateInt;
				break;
			}
			case 2:
			{
				boolean bisiesto = dateInt[2]%4==0;
				dateInt[0]++;
				if(bisiesto && dateInt[0]>29)
				{
					dateInt[1]++;
					dateInt[0] = 1;
				}
				else if(dateInt[0]>28 && !bisiesto)
				{
					dateInt[1]++;
					dateInt[0] = 1;
				}
				newFecha = dateInt;
				break;
			}
		}
		
		return newFecha;
	}
	
	/**
	 * Metodo que parsea los minutos y las horas de entero a string
	 * en caso de que vengan dados en numeros entre 1 y 9 se le coloca un 0 detras
	 * por ejemplo si una hora viene en 4 se parsea a 04
	 * @param hour
	 * @return hora parseada
	 */
	protected String parseTime(int hour)
	{
		String newHour = ""+hour;
		
		if(hour>0 && hour<10)
		{
			newHour = "0"+hour;
		}
		
		return newHour;
	}
}
