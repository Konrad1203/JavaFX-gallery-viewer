package pl.edu.agh.to.reaktywni.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    long count();
}
