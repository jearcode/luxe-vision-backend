package com.luxevision.backend.controller;
import com.luxevision.backend.entity.*;
import com.luxevision.backend.exception.StudioNameAlreadyRegisteredException;
import com.luxevision.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/studios")
public class StudioController {

    @Autowired
    private StudioService studioService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PhotographerService photographerService;

    @Autowired
    private PortfolioPhotoService portfolioPhotoService;

    @Autowired
    private StudioSpecialtyService studioSpecialtyService;

    @Autowired
    private S3Service s3Service;

    @GetMapping
    public List<Studio> getAllStudios() {
        return studioService.getAllStudios();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudio(@PathVariable Integer id) {
        try {
            studioService.deleteStudioById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/random")
    public List<Studio> getRandomStudios() {
        return studioService.getRandomStudios();
    }

    @PostMapping
    public ResponseEntity<?> saveStudio (@RequestPart("studio") Studio studio,
                                         @RequestPart("profileImage")MultipartFile profileImage,
                                         @RequestPart("portfolioImages") List<MultipartFile> portfolioImages)
            throws StudioNameAlreadyRegisteredException {

        if(studioService.existsStudioByStudioName(studio.getStudioName()) ) {
            throw new StudioNameAlreadyRegisteredException();
        }

        Location location = new Location();
        location.setCity(studio.getLocation().getCity());
        location.setState(studio.getLocation().getState());
        location.setCountry(studio.getLocation().getCountry());
        location.setAddress(studio.getLocation().getAddress());
        Location locationSaved = locationService.saveLocation(location);

        Studio studioToSave = studio;
        studioToSave.setLocation(locationSaved);

        Studio studioSaved = studioService.saveStudio(studioToSave);

        for (Photographer photographer : studio.getPhotographers()) {
            photographer.setStudio(studioSaved);
            photographerService.savePhotographer(photographer);
        }

        for (StudioSpecialty studioSpecialty : studio.getStudioSpecialties()) {
            studioSpecialty.setStudio(studioSaved);
            studioSpecialtyService.saveStudioSpecialty(studioSpecialty);
        }

        try {
            String profilePhotoUrl = s3Service.uploadFile(profileImage, studio.getStudioName(), "");
            studioSaved.setProfileImage(profilePhotoUrl);

            List<PortfolioPhoto> portfolioPhotos = new ArrayList<>();

            int index = 1;
            for (MultipartFile portfolioImage : portfolioImages) {
                PortfolioPhoto portfolioPhoto = new PortfolioPhoto();
                String photoUrl = s3Service.uploadFile(portfolioImage, studio.getStudioName(), "photography-" + index);
                portfolioPhoto.setImage(photoUrl);
                portfolioPhoto.setStudio(studioSaved);
                portfolioPhotos.add(portfolioPhotoService.savePortfolioPhoto(portfolioPhoto));
                index++;
            }

            studioSaved.setPortfolioPhotos(portfolioPhotos);
            studioService.updateStudio(studioSaved);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(studioSaved);

    }

    @GetMapping("/{id}")
    public ResponseEntity<Studio> findStudioById (@PathVariable Integer id) {
        Optional<Studio> studioFromDB = studioService.findStudioById(id);

        if(!studioFromDB.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(studioFromDB.get());
    }

}