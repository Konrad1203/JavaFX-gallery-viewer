package pl.edu.agh.to.reaktywni.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    @Query("SELECT i.name, i.extensionType FROM Image i WHERE i.id = :id")
    Optional<ImageMetaData> findImageMetaDataById(int id);

    @Query("SELECT i.id FROM Image i")
    List<Long> findAllIds();
}
