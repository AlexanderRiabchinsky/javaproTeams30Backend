package soialNetworkApp.repository;

import soialNetworkApp.model.entities.Friendship;
import soialNetworkApp.model.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipsRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findFriendshipBySrcPerson(Person srcPerson);

    @Query(value = "SELECT * FROM friendships WHERE (dst_person_id = :id OR src_person_id = :id)",
            nativeQuery = true)
    List<Friendship> findFriendsToDelete(@Param("id") long id);

    List<Friendship> findFriendshipsByDstPerson(Person dstPerson);
}