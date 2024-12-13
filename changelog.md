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
- JDBC for data base

<p align="center">
    <img src="md-images/diagram1.png" alt="alt text">
</p>

## 13.12.2023 - First Milestone

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

### Main functionalities
- different sizes and formats of images are supported
- it is possible to send one package of images after package

### Changes in conceptions
- decided to use javaFX rather than Swing

### Class diagram

<p align="center">
    <img src="md-images/diagram2.png" alt="alt text">
</p>
