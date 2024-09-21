package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.timetable_server.models.jpa.ProfesoresDto;

public interface IProfesorDtoRepository extends JpaRepository<ProfesoresDto, String> 
{

}
