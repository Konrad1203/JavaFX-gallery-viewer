package pl.edu.agh.to.reaktywni.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    Optional<ImageMetaData> findImageMetaDataById(int id);

    @Query("SELECT i.id FROM Image i LEFT JOIN Thumbnail t ON i.id = t.image.id " +
            "GROUP BY i.id having count(t.id) != :sizesCount")
    List<Integer> findAllIdsWithMissingThumbnails(int sizesCount);

    @Query("SELECT i.id FROM Image i WHERE i.directoryPath = :directoryPath")
    List<Integer> findAllIdsByDirectoryPath(String directoryPath);
}
