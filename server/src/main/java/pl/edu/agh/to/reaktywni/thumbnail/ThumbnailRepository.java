package pl.edu.agh.to.reaktywni.thumbnail;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.reaktywni.image.ImageState;
import java.util.List;


@Repository
public interface ThumbnailRepository extends JpaRepository<Thumbnail, Integer> {

    @Query("SELECT t FROM Thumbnail t WHERE t.size = :size AND t.image.directoryPath = :directoryPath")
    List<Thumbnail> getBySizeAndPath(ThumbnailSize size, String directoryPath, Pageable pageable);

    @Query(value = "SELECT * FROM (SELECT * FROM thumbnail LIMIT :elemCount) subquery " +
            "WHERE subquery.size = :size " +
            "AND subquery.image.directory_path = :directoryPath " +
            "AND subquery.image_id NOT IN :ids", nativeQuery = true)
    List<Thumbnail> getBySizeAndPathExcludingList(ThumbnailSize size, String directoryPath, List<Integer> ids, int elemCount);

    @Query("SELECT count(t) FROM Thumbnail t WHERE t.size = :size AND t.image.directoryPath = :directoryPath")
    long countBySizeAndPath(ThumbnailSize size, String directoryPath);

    @Query("SELECT t FROM Thumbnail t WHERE t.image.id = :id")
    List<Thumbnail> getByImageId(int id);

    @Query("SELECT t FROM Thumbnail t WHERE t.image.id = :id AND t.size = :size")
    Thumbnail getByImageIdAndSize(int id, ThumbnailSize size);

    @Query("SELECT t FROM Thumbnail t JOIN FETCH t.image WHERE t.state = :imageState")
    Iterable<Thumbnail> getByStateWithImages(ImageState imageState);
}
