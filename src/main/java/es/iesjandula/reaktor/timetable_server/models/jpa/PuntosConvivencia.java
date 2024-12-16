package es.iesjandula.reaktor.timetable_server.models.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "puntos_convivencia")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuntosConvivencia 
{
	/**Id de la sancion/recompensa autoincremental */
	@Id
	//GeneratedValue hace que el id vaya incrementandose uno a uno
	//GenerationType.Identity indica que el valor sera generado por la base de datos
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "puntos_id")
	private Long puntosId;
	
	/**Valor de los puntos que se restan/suman */
	@Column(nullable = false)
	private Integer valor;
	
	/**Descripcion de la sancion/recompensa */
	@Column(nullable = false)
	private String descripcion;
	
	/**
	 * Constructor que instancia una sancion por su valor y descripcion
	 * el id no se coloca porque se autogenera
	 * @param nombre
	 * @param apellidos
	 */
	public PuntosConvivencia(int valor, String descripcion)
	{
		this.valor = valor;
		this.descripcion = descripcion;
	}
}
