package es.iesjandula.reaktor.timetable_server.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*Creador: jose muñoz lara*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AulaPlanoEntity 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Largo del aula */
    @Column
    private double height;

    /** Ancho del aula */
    @Column
    private double width;

    /** Medida en el eje y */
    @Column
    private double top;

    /** Medida en el eje x derecho */
    @Column
    private double rightSide;

    /** Medida en el eje x izquierdo */
    @Column
    private double leftSide;

    /** Planta en la que se encuentra el aula */
    @Column
    private String planta;
    
    /** Aula que referencia */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numIntAu") // Esto vincula el aula con el plano
    private AulaEntity aula;
    

}
