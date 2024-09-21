package es.iesjandula.reaktor.timetable_server.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author David Martinez
 *
 */
@Data
@NoArgsConstructor
@Slf4j
public class HorariosError extends Exception
{
	/** Attribute serialVersionUID*/
	private static final long serialVersionUID = 2937203647694023448L;

	/**Codigo del error generado */
	private int code;

	/**Descripcion del error */
	private String text;
	
	/**Causa externa generada */
	private Exception exception;

	/**
	 * Constructor que crea la excepcion usando un codigo, un mensaje y una causa
	 * @param message descripcion del error
	 * @param code codigo del error generado
	 * @param text descripcion del error
	 * @param exception causa externa del error generado
	 */
	public HorariosError( int code, String text, Exception exception) 
	{
		super(text, exception);
		this.code = code;
		this.text = text;
		this.exception = exception;
	}
	
	/**
	 * Constructor que crea la excepcion usando un codigo y un mensaje
	 * @param message descripcion del error
	 * @param code codigo del error generado
	 * @param text descripcion del error
	 */
	public HorariosError( int code, String text) 
	{
		super(text);
		this.code = code;
		this.text = text;
	}
	
	
	/**
	 * Metodo que mapea el error generado para detallar el error generado
	 * @return mapa con clave string y valor object que detalla el error
	 */
	public Map<String, Object> toMap()
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("codigo", this.code);
		map.put("descripcion", this.text);
		
		if(this.exception!=null)
		{
			map.put("causa", ExceptionUtils.getStackTrace(this.exception));
		}
		
		return map;
	}
	
	/**
	 * mapa para devolver la excepcion
	 * @return mapa con mensaje, error y en caso de tener excepcion tambien devuelve la excepcion
	 */
	public Map<String,String> getBodyExceptionMessage()
	{
		Map<String,String> messageMap = new HashMap<String,String>();
		messageMap.put("code", String.valueOf(code));
		messageMap.put("text",text);
		if(this.exception!=null)
		{
			String stackTrace = ExceptionUtils.getStackTrace(exception);
			messageMap.put("exception", stackTrace);
		}
		return messageMap;
	}

}
