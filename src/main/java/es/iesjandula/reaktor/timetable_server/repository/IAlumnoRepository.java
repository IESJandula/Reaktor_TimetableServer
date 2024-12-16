package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.jpa.Alumnos;

@Repository
public interface IAlumnoRepository extends JpaRepository<Alumnos, Long>
{

}
