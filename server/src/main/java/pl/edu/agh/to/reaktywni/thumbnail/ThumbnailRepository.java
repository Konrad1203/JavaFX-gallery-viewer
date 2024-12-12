package pl.edu.agh.to.reaktywni.thumbnail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ThumbnailRepository extends JpaRepository<Thumbnail, Integer> {

}
