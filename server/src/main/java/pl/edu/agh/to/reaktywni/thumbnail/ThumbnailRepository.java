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
    List<Thumbnail> getThumbnailsBySizeAndImageDirectoryPath(ThumbnailSize size, String directoryPath, Pageable pageable);

    default List<Thumbnail> getThumbnailsBySizeExcludingList(ThumbnailSize size, String directoryPath, List<Integer> ids, int elemCount) {
        return getThumbnailsBySizeAndImageDirectoryPath(size, directoryPath, Pageable.ofSize(elemCount))
                .stream()
                .filter(t -> !ids.contains(t.getImage().getId()))
                .toList();
    }

    @Query("SELECT count(t) FROM Thumbnail t WHERE t.size = :size AND t.image.directoryPath = :directoryPath")
    long countBySizeAndImageDirectoryPath(ThumbnailSize size, String directoryPath);

    @Query("SELECT t FROM Thumbnail t WHERE t.image.id = :id")
    List<Thumbnail> findByImageId(int id);

    @Query("SELECT t FROM Thumbnail t WHERE t.image.id = :id AND t.size = :size")
    Thumbnail findByImageIdAndSize(int id, ThumbnailSize size);

    @Query("SELECT t FROM Thumbnail t JOIN FETCH t.image WHERE t.state = :imageState")
    Iterable<Thumbnail> findByStateWithImages(ImageState imageState);
}
