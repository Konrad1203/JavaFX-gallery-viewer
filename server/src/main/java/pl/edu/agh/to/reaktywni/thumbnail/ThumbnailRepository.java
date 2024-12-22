package pl.edu.agh.to.reaktywni.thumbnail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ThumbnailRepository extends JpaRepository<Thumbnail, Integer> {

    List<Thumbnail> getThumbnailsBySize(ThumbnailSize size);

    Long countBySize(ThumbnailSize size);


    Thumbnail findByImageIdAndSize(int databaseId, ThumbnailSize thumbnailSize);
}
