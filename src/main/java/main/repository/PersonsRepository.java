package main.repository;

import main.model.entities.City;
import main.model.entities.Friendship;
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
    boolean existsPersonByEmail(String email);
    Person findPersonById(Long id);
    Page<Person> findPersonByDstFriendships(List<Friendship> friendships, Pageable pageable);
    Optional<Person> findPersonByEmail(String email);
    Page<Person> findAllByCity(City city, Pageable page);
    @Query("FROM Person AS p " +
            "ORDER BY p.regDate DESC")
    Page<Person> findPageOrderByRegDate(Pageable page);






}
