package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.entities.ProfesorEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Profesor;

@Repository
public interface IProfesorRepository extends JpaRepository<ProfesorEntity, String>
{
	@Query("SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Profesor(p) FROM ProfesorEntity p")
	public List<Profesor> recuperaListadoProfesores();

	@Query("SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Profesor(p) " +
		       "FROM ProfesorEntity p " +
		       "WHERE p.nombre = :nombre AND p.primerApellido = :primerApellido AND p.segundoApellido = :segundoApellido")
		public Optional<Profesor> buscaProfesorPorNombreYApellidos(
		       @Param(value = "nombre") String nombre,
		       @Param(value = "primerApellido") String primerApellido,
		       @Param(value = "segundoApellido") String segundoApellido);
	
	Optional<Profesor> findByNumIntPR(String numIntPR);
	
    List<Profesor> findByNombreContainingIgnoreCase(String nombre);
    
    @Query("SELECT p " +
    	   "FROM ActividadEntity ac join ac.profesor p " +
    	   "WHERE ac.aula.numIntAu = :numAula and p.numIntPR = ac.profesor.numIntPR  ")
	public List<ProfesorEntity> recuperaProfesorPorNumAula(@Param("numAula") String numAula);

}
