package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.jpa.Curso;
import es.iesjandula.reaktor.timetable_server.models.jpa.CursoId;

@Repository
public interface ICursosRepository extends JpaRepository<Curso,CursoId>
{

}
