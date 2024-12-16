package es.iesjandula.reaktor.timetable_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.jpa.VisitasServicio;
import es.iesjandula.reaktor.timetable_server.models.jpa.VisitasServicioId;

@Repository
public interface IVisitasServicioRepository extends JpaRepository<VisitasServicio,VisitasServicioId>
{

}
