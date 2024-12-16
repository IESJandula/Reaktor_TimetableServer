package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.entities.GrupoEntity;
import es.iesjandula.reaktor.timetable_server.models.parse.Grupo;

@Repository
public interface IGrupoRepository extends JpaRepository<GrupoEntity, String>
{
	// Recupera una lista de objetos GrupoEntity y los convierte a clase Grupo tipo parse.
	@Query( "SELECT new es.iesjandula.reaktor.timetable_server.models.parse.Grupo(g) FROM GrupoEntity g"  )
	public List<Grupo> recuperaGruposDeParseo();
	
	// Este método devuelve una lista de grupos válidos, es decir, grupos cuyos nombres
	// no están en la lista de exclusión definida en la consulta (NOT IN).
	@Query("SELECT g FROM GrupoEntity g WHERE g.nombre NOT IN ('GRecr', 'Guardia Biblioteca', 'Guardias')")
    List<GrupoEntity> findAllValidGroups();
	
	
    
    List<Grupo> findAllByNumIntGrIn(List<String> numIntGr);
}
