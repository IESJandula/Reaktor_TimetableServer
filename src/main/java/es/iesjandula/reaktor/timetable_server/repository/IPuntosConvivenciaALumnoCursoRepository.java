package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.jpa.PuntosConvivenciaAlumnoCurso;
import es.iesjandula.reaktor.timetable_server.models.jpa.PuntosConvivenciaAlumnoCursoId;

@Repository
public interface IPuntosConvivenciaALumnoCursoRepository extends JpaRepository<PuntosConvivenciaAlumnoCurso, PuntosConvivenciaAlumnoCursoId>
{
	
}
