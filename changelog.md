# ReaktywniApp

## Authors

- Mateusz Bobula
- Konrad Tendaj
- Bartosz Knapik

## 7.12.2023 - Basic Ideas

<p align="center">
    <img src="md-images/db.png" alt="alt text">
</p>

Technologies
- Spring-boot
- WebFlux
- Swing for clientSide
- webClient
- JDBC for database

<p align="center">
    <img src="md-images/diagram1.png" alt="class diagram 1">
</p>

## 13.12.2023 - First Milestone

### Changes in conceptions
- decided to use javaFX rather than Swing

### Main functionalities
- different sizes and formats of images are supported
- it is possible to send one package of images after package

### Client
- Client Frontend
    - GUI Elements
        - Button responsible for choosing files
        - Button responsible for sending files
        - Grid responsible for displaying thumbnails
    - GUI Functionalities
        - Chosing images that will be sent
        - Sending images
        - Clicking on a thumbnail opens the original image in a new window
        - Displaying ready thumbnails in a grid or loading icons
- Client Backend
    - Logic responsible for connecting with server
    - Logic responsible for sending images to server
    - Logic responsible for reactive receiving thumbnails from server
    - Logic responsible for showing big image

### Server
- Server Backend
    - DataBase
        - Created database for storing images and thumbnails data
        - Saving images in database
        - Saving thumbnails in databse
    - Logic responsible for getting images from client
    - Logic responsible for reactive and asynchronic creation of thumbnails
    - Logic responsible for reactive and asynchronic sending thumbnails to client
    - Logic responsible for resizing image

### Class diagram
<p align="center">
    <img src="md-images/diagram2.png" alt="class diagram 2">
</p>


## 20.12.2024 - First Milestone (after review fixes)

### Client
- Fixed window freeze while loading big image in BigImageView
- Showing error in BigImageView when image couldn't be loaded
- Improved ImageGalleryView initialization with alert pop-up
- Changed ImageGalleryView initialization to download thumbnails count rather than images count
- Tweaked placing image in ImageGalleryView
- Improved exception handling
    - FilesToImagesConverter throws IOException
    - ImageGalleryPresenter handles FilesToImagesConverter exception with alert pop-up
- Replaced System.out.print with loggers
- Changed ReaktywniClientApp to use on Profile "dev" and "prod" rather than excluding "test"
- Changed active profile to "dev" in application.properties


### Server
- Improved exception handling
    - createImageFromThumbnail returns Optional
    - getImage() in ImageService returns Mono with error HTTP 404 if image not found
    - getThumbnailsCount() in ImageService returns Mono with error if it cannot get thumbnails count
    - Resizer set imageStatus to FAILED if image cannot be resized
- Replaced System.out.print with loggers
  - Increased StreamReadConstraint maxStringLength to handle bigger images (tested on 80MB image)
- Transported getImage logic from ImageController to ImageService
- Added getThumbnailsCount
- Changed test checking with StepVerifier, expectNextMatches and verifyComplete

### Class diagram
<p align="center">
    <img src="md-images/diagram3.png" alt="class diagram 3">
</p>