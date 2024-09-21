package es.iesjandula.reaktor.timetable_server.utils;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reaktor.timetable_server.exceptions.HorariosError;
import es.iesjandula.reaktor.timetable_server.models.ProfesorDto;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Validations 
{
	/**
	 * Método para comprobar si el fichero esta vacio
	 * 
	 * @param metodoLlamada método que llama a esta funcionalidad
	 * @param ficheroMultipart fichero Multipart
	 * @return El contenido del fichero
	 * @throws HorarioException Se lanza si el contenido del fichero da algun error
	 */
	public String obtenerContenidoFichero(String metodoLlamada, MultipartFile ficheroMultipart) throws HorariosError
	{
		String contenido = null ;
		
		try
		{
			contenido = new String(ficheroMultipart.getBytes());
			
			if (contenido == null || contenido.isEmpty())
			{
				String parameterName = ficheroMultipart.getName() ;
				
				log.error(Constants.ERR_CONTENIDO_FICHEROS_CSV_MSG + parameterName) ;
				throw new HorariosError(Constants.ERR_CONTENIDO_FICHEROS_CSV_CODE, Constants.ERR_CONTENIDO_FICHEROS_CSV_MSG + parameterName);
			}
		}
		catch (IOException ioException)
		{
			log.error(Constants.ERR_LECTURA_FICHEROS_CSV_MSG + metodoLlamada, ioException);
			
			throw new HorariosError(Constants.ERR_LECTURA_FICHEROS_CSV_CODE, 
									   Constants.ERR_LECTURA_FICHEROS_CSV_MSG + metodoLlamada, 
									   ioException);
		}
		
		return contenido ;
	}
	
	/**
	 * Método para obtener que la lista de rofesoresDto
	 * 
	 * @param session Utilizado para guardas u obtener cosas en sesión
	 * @return Lista de profesoresDto
	 * @throws HorarioException se lanzará esta excepción si la lista de profesoresDto es nula
	 */
	@SuppressWarnings("unchecked")
	public List<ProfesorDto> obtenerListaProfesoresDto(HttpSession session)
			throws HorariosError
	{
		List<ProfesorDto> listaProfesoresDto = (List<ProfesorDto>) session.getAttribute(Constants.SESION_LISTA_PROFESORESDTO) ;

		if (listaProfesoresDto == null)
		{
			String error = "Los ProfesoresDto no han sido cargados en sesion todavía";
			
			// Log con el error
			log.error(error);
			
			throw new HorariosError(Constants.ERR_LIST_NULL_CODE, error);
			
		}

		return listaProfesoresDto;
	}
	
	/**
	 * Metodo para encontrar el profesorDto
	 * @param correo del profesor
	 * @param profesorDto Objeto profesorDto
	 * @param listaProfesoresDto Listado de los profesoresDto
	 * @return Devuelve el profesorDto
	 * @throws HorariosError Da un error en caso de que no encuentre el profesorDto
	 */
	public ProfesorDto encontrarProfesorDto(String correo, ProfesorDto profesorDto,
			List<ProfesorDto> listaProfesoresDto) throws HorariosError 
	{
		Boolean encontrado = false;
		int i = 0;

		while(!encontrado && i < listaProfesoresDto.size())
		{
		    if(listaProfesoresDto.get(i).getCorreo().equals(correo)) 
		    {
		        encontrado = true;
		        profesorDto = new ProfesorDto(listaProfesoresDto.get(i));
		    }
		    else 
		    {
		        i++;
		    }
		}
		
		if(!encontrado) 
		{
			String errorString = "el correo: " + correo + " no existe";
			
			// Log con el error
			log.error(errorString);
			
			throw new HorariosError(Constants.ERR_PROFESORDTO, errorString);
		}
		return profesorDto;
	}
}
