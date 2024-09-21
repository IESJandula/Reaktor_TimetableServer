package es.iesjandula.reaktor.timetable_server.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.ProfesorDto;
import lombok.extern.log4j.Log4j2;

/**
 * clase Parse
 */
@Log4j2
public class Parse 
{
	/**
	 * Método para parsear profesoresDto
	 * 
	 * @param csvFile Fichero csv
	 * @return La lista de profesoresDto
	 * @throws HorarioException Excepción en la línea en la que ocurre el error
	 */
	public List<ProfesorDto> parseProfesoresDto(MultipartFile csvFile) throws HorariosError 
	{
		Scanner scanner = null;
		try
		{
			List<ProfesorDto> listaProfesoresDto = new ArrayList<>();

			Validations validations = new Validations();

			String contenido = validations.obtenerContenidoFichero(Constants.NOMBRE_METH_PARSE_PROFESORDTO, csvFile);

			scanner = new Scanner(contenido);
			// Saltamos la cabecera
			scanner.nextLine();
			while (scanner.hasNext())
			{
				String linea = scanner.nextLine();

				// Separamos la linea por ,
				String[] lineaArray = linea.split(",");

				if (lineaArray.length != 4)
				{
					String errorString = Constants.ERR_LECTURA_FICHEROS_CSV_MSG
							+ Constants.NOMBRE_METH_PARSE_PROFESORDTO + ". Concretamente aquí: " + linea;

					log.error(errorString);
					throw new HorariosError(Constants.ERR_LECTURA_FICHEROS_CSV_CODE, errorString);
				}

				listaProfesoresDto.add(new ProfesorDto(lineaArray[0],lineaArray[1],lineaArray[2],lineaArray[3]));
			}
			// Mostramos la lista de departamentos en el log
			log.info(listaProfesoresDto);

			return listaProfesoresDto;
		}
		finally
		{
			if (scanner != null)
			{
				scanner.close();
			}
		}
	}
}
