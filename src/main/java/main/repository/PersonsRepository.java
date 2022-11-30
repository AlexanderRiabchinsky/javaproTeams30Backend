package main.repository;

import main.model.entities.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonsRepository extends JpaRepository<Person, Long> {

    Optional<Person>findPersonById(Long id);

    boolean existsPersonByEmail(String email);

    Optional<Person> findPersonByEmail(String email);

    Page<Person> findAllByCity(String city, Pageable page);

    @Query("FROM Person AS p " +
            "ORDER BY p.regDate DESC")
    Page<Person> findPageOrderByRegDate(Pageable page);

    Person findPersonByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCase(String firstName, String lastName);

    @Query(value = "SELECT id FROM persons WHERE is_deleted = true AND (select(select extract(epoch from now()) - (extract(epoch from(deleted_time)))) * 1000 >  :timeToDel)", nativeQuery = true)
    List<Long> findIdtoDelete(@Param("timeToDel") long timeToDel);

    @Query(value = "SELECT * FROM persons WHERE is_deleted = true AND (select(select extract(epoch from now()) - (extract(epoch from(deleted_time)))) * 1000 > :timeToDel)", nativeQuery = true)
    List<Person> findOldDeletes(@Param("timeToDel") long timeToDel);
}
