package com.sporton.SportOn.service.aswS3Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sporton.SportOn.entity.Venue;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AWSS3Service {
    private final String sportOnVenueImages = "sportonvenueimages"; // Replace with your S3 bucket name
    private final String facilityIcon = "facilityicons";
    private String accessKey="AKIA6GBMBODXYPAAOU35";

    private String secret="6un/OnXsVIg85OeX1vz5BRZVgaImJqKYvrIToo9D"; // Replace with your AWS secret key
    private final Regions region = Regions.EU_NORTH_1; // Specify your AWS region
    @Autowired
    private VenueRepository venueRepository;
    private AmazonS3 s3Client;

    public AWSS3Service() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secret);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    public List<String> uploadImages(List<MultipartFile> imageFiles) throws IOException, CommonException {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            String key = UUID.randomUUID().toString(); // Generate unique key for each image
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());
                s3Client.putObject(new PutObjectRequest(sportOnVenueImages, key, file.getInputStream(), metadata));
                String imageUrl = s3Client.getUrl(sportOnVenueImages, key).toString();
                imageUrls.add(imageUrl);
                log.info("image urls {}", imageUrls);
            } catch (AmazonServiceException e) {
                // Handle Amazon S3 service exceptions
                throw new CommonException(e.getMessage());
            } catch (SdkClientException e) {
                // Handle Amazon S3 client exceptions
                throw new CommonException(e.getMessage());
            }
        }
        return imageUrls;
    }

    public void updateVenueImages(Long venueId, List<MultipartFile> newImageFiles) throws IOException, VenueException, CommonException {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueException("Venue not found with id: " + venueId));

        // Delete existing images associated with the venue from S3 bucket
        for (String imageUrl : venue.getImages()) {
            deleteImageByUrl(imageUrl);
        }

        // Upload new images to S3 bucket and update the venue's image URLs
        List<String> newImageUrls = new ArrayList<>();
        for (MultipartFile file : newImageFiles) {
            String key = UUID.randomUUID().toString(); // Generate unique key for each image
            String imageUrl = uploadImage(file);
            newImageUrls.add(imageUrl);
        }
        venue.setImages(newImageUrls);

        // Save the updated venue with new image URLs
        venueRepository.save(venue);
    }

    public String uploadImage(MultipartFile imageFile) throws IOException, CommonException {
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String key = UUID.randomUUID().toString(); // Generate unique key for the image
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(imageFile.getSize());
                metadata.setContentType(imageFile.getContentType());
                s3Client.putObject(new PutObjectRequest(facilityIcon, key, imageFile.getInputStream(), metadata));
                imageUrl = s3Client.getUrl(facilityIcon, key).toString();
                log.info("Uploaded image with URL: {}", imageUrl);
            } catch (AmazonServiceException e) {
                // Handle Amazon S3 service exceptions
                throw new CommonException(e.getMessage());
            } catch (SdkClientException e) {
                // Handle Amazon S3 client exceptions
                throw new CommonException(e.getMessage());
            }
        } else {
            throw new CommonException("Image file is empty or null");
        }
        return imageUrl;
    }

    public void deleteImageByUrl(String imageUrl) throws CommonException {
        String key = getKeyFromImageUrl(imageUrl);
        if (key != null) {
            try {
                s3Client.deleteObject(new DeleteObjectRequest(facilityIcon, key));
                log.info("Deleted image with URL: {}", imageUrl);
            } catch (AmazonServiceException e) {
                // Handle Amazon S3 service exceptions
                throw new CommonException("Failed to delete image: " + e.getMessage());
            } catch (SdkClientException e) {
                // Handle Amazon S3 client exceptions
                throw new CommonException("Failed to delete image: " + e.getMessage());
            }
        } else {
            throw new CommonException("Invalid imageURL");
        }
    }


    public String updateImage(String imageUrl, MultipartFile updatedImage) throws IOException, CommonException {
        String key = getKeyFromImageUrl(imageUrl); // Method to retrieve the key from the imageURL

        if (key != null) {
            return updateImageByKey(key, updatedImage);
        } else {
            throw new CommonException("No matching key found for the provided imageURL");
        }
    }
    private String updateImageByKey(String key, MultipartFile updatedImage) throws IOException, CommonException {
        String imageUrl = null;
        if (updatedImage != null && !updatedImage.isEmpty()) {
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(updatedImage.getSize());
                metadata.setContentType(updatedImage.getContentType());

                // Create a PutObjectRequest without the ACL
                PutObjectRequest putObjectRequest = new PutObjectRequest(facilityIcon, key, updatedImage.getInputStream(), metadata);

                // Upload the updated object
                s3Client.putObject(putObjectRequest);

                // Get the URL of the updated image
                imageUrl = s3Client.getUrl(facilityIcon, key).toString();
                log.info("Updated image with URL: {}", imageUrl);
            } catch (AmazonServiceException e) {
                // Handle Amazon S3 service exceptions
                throw new CommonException(e.getMessage());
            } catch (SdkClientException e) {
                // Handle Amazon S3 client exceptions
                throw new CommonException(e.getMessage());
            }
        } else {
            throw new CommonException("Updated image file is empty or null");
        }
        return imageUrl;
    }

    private String getKeyFromImageUrl(String imageUrl) {
        // Split the imageURL by "/" and get the last segment
        String[] segments = imageUrl.split("/");
        if (segments.length > 0) {
            // The key is the last segment of the URL
            return segments[segments.length - 1];
        } else {
            // If the URL format is unexpected, return null or handle accordingly
            return null;
        }
    }


}
