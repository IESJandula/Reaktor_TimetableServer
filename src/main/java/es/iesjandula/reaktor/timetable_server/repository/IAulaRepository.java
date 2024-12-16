package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.entities.AulaEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Aula;

@Repository
public interface IAulaRepository extends JpaRepository<AulaEntity, String>
{
	@Query("SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Aula(a) FROM AulaEntity a ")
	public List<Aula> recuperaListadoAulas();
	
	public Optional<AulaEntity> findByNombre( String nombre );
	
	public Optional<Aula> findByNumIntAu(String numIntAu);
	
	 List<AulaEntity> findByPlanta(String planta);  // Método para buscar aulas por planta.
	 
	 List<Aula> findByAbreviaturaContaining(String abreviatura);
}
