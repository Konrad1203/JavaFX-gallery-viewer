package pl.edu.agh.to.reaktywni.thumbnail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.reaktywni.image.ImageState;

import java.util.List;


@Repository
public interface ThumbnailRepository extends JpaRepository<Thumbnail, Integer> {

    List<Thumbnail> getThumbnailsBySize(ThumbnailSize size);

    Long countBySize(ThumbnailSize size);

    @Query("SELECT count(t) FROM Thumbnail t WHERE t.image.id = :id")
    Long countByImageId(int id);

    @Query("SELECT t FROM Thumbnail t WHERE t.image.id = :id")
    List<Thumbnail> findByImageId(int id);

    @Query("SELECT t FROM Thumbnail t WHERE t.image.id = :id AND t.size = :size")
    Thumbnail findByImageIdAndSize(int id, ThumbnailSize size);

    @Query("SELECT t FROM Thumbnail t JOIN FETCH t.image WHERE t.state = :imageState")
    Iterable<Thumbnail> findByStateWithImages(ImageState imageState);
}
