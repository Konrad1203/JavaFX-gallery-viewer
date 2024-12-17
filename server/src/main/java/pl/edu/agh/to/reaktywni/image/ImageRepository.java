package pl.edu.agh.to.reaktywni.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    Optional<ImageMetaData> findByDatabaseId(int id);
}
