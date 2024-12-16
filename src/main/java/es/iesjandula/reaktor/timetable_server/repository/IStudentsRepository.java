package es.iesjandula.reaktor.timetable_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.timetable_server.models.Student;
import es.iesjandula.reaktor.timetable_server.models.entities.StudentsEntity;

@Repository
public interface IStudentsRepository extends JpaRepository<StudentsEntity, Long>
{
	// filtra los estudiantes que están en el baño
	//@Query("SELECT s FROM StudentEntity s WHERE s.inBathroom = true")  
	
    public List<StudentsEntity> findAllByInBathroomTrue();
    
    // Encuentra estudiante por nombre, apellidos y curso.
    public Optional<StudentsEntity> findByNameAndLastNameAndCourse( String name, String lastName, String Course );
   
    @Query( "SELECT new es.iesjandula.reaktor.timetable_server.models.Student(s) FROM StudentsEntity s" )
    public List<Student> recuperaListadoEstudiantes();
 
    List<StudentsEntity> findByCourse(String course);
}
