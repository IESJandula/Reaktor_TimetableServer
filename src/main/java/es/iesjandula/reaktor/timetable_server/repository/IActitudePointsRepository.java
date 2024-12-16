package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.ActitudePoints;
import es.iesjandula.reaktor.timetable_server.models.entities.ActitudePointsEntity;

@Repository
public interface IActitudePointsRepository extends JpaRepository<ActitudePointsEntity, Long>
{
	// Mapea los datos de la entidad a la clase de ActitudePointsEntity
    @Query("SELECT new es.iesjandula.reaktor.timetable_server.models.ActitudePoints(a.points, a.description) FROM ActitudePointsEntity a")
    List<ActitudePoints> findAllActitudePoints();
    
 // Método para encontrar puntos por valor y descripción
    Optional<ActitudePoints> findByPointsAndDescription(int points, String description);

    @Query("SELECT a.valor FROM PuntosConvivencia a")
    List<Integer> findAllPuntosConvivenciaValores();
    
    @Query("SELECT a.descripcion FROM PuntosConvivencia a")
    List<String> findAllPuntosConvivenciaDescripciones();
	
}
