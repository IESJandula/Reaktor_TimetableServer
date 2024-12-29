package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.TeacherMoment;
import es.iesjandula.reaktor.timetable_server.models.entities.ActividadEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Actividad;

@Repository
public interface IActividadRepository extends JpaRepository<ActividadEntity, String>
{

	// Recupera un listado de actividades relacinadas a un profesor concreto del que recibimos nombre.
	@Query("SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Actividad(ae) FROM ActividadEntity ae"  )
	public List<Actividad> recuperaListadoActividades();
	
	// Recupera un listado de actividades relacinadas a un profesor concreto del que recibimos nombre.
	@Query("SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Actividad(ae) FROM ActividadEntity ae WHERE ae.profesor.numIntPR = :idProfesor" )
	public List<Actividad> recuperaListadoActividadesPorIdProfesor( @Param( value = "idProfesor") String idProfesor);
		
	// Devuelve un optional de actividad realizando una busqueda basada en un tramo horario y un profesor. Usado para ubicar a un docente.
	@Query( "SELECT ae FROM ActividadEntity ae WHERE ae.tramo.numTr = :tramoId AND ae.profesor.numIntPR = :profesorId")
	public Optional<ActividadEntity> buscaActividadEntityPorTramoYProfesor( @Param(value="tramoId") String tramoId, @Param( value = "profesorId") String profesorId );
	
	
	List<Actividad> findByTramo(String tramo);
	
    List<Actividad> findByAsignatura_NumIntAs(String numIntAs);
	
	
    @Query("""
    	    SELECT new es.iesjandula.reaktor.timetable_server.models.TeacherMoment(
    	        new es.iesjandula.reaktor.timetable_server.models.Teacher(
    	            p.nombre,
    	            p.primerApellido
    	        ),
    	        asig.nombre,
    	        new es.iesjandula.reaktor.timetable_server.models.Classroom(
    	            a.abreviatura,
    	            a.nombre,
    	            a.planta
    	        )
    	    )
    	    FROM ActividadEntity ac
    	         JOIN ac.profesor p
    	         JOIN ac.asignatura asig
    	         JOIN ac.aula a
    	         JOIN ac.tramo t
    	         JOIN ac.grupo g
    	    WHERE g.nombre LIKE :groupName
    	      AND t.dayNumber = :dayNumber
    	      AND t.startHour LIKE :startHour
    	      AND t.endHour LIKE :endHour
    	    """)
    	List<TeacherMoment> findTeacherMomentsByParameters(@Param("groupName") String groupName,
    													   @Param("dayNumber") String dayNumber,
    													   @Param("startHour") String startHour,
    													   @Param("endHour") String endHour);

}
