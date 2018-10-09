package pl.mkm.webAlignSeq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.mkm.webAlignSeq.exception.FileStorageException;
import pl.mkm.webAlignSeq.exception.MyFileNotFoundException;
import pl.mkm.webAlignSeq.property.FileStorageProperties;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static javax.management.timer.Timer.ONE_DAY;
import static javax.management.timer.Timer.ONE_MINUTE;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    ExcelService excelService;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence " + fileName);
            }
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            excelService.generateAlignmentInExcelCells(file, targetLocation);
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }finally {
          //  this.fileStorageLocation.resolve(fileName).toFile().delete();
        }
    }

    @Scheduled(fixedRate = ONE_DAY)
    public void cleanUploadsDirectory(){
        try {
            findFiles(this.fileStorageLocation.toAbsolutePath().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("cleaning uploads...");
    }
    public void findFiles(String filePath) throws IOException {
        List<File> files = Files.list(Paths.get(filePath))
                .map(path -> path.toFile())
                .collect(Collectors.toList());
        for(File file: files) {


            if(file.isDirectory()) {
                findFiles(file.getAbsolutePath());
            } else if(isFileOld(file)){
                deleteFile(file);
            }
        }
    }

    public void deleteFile(File file) {
        file.delete();
    }

    public boolean isFileOld(File file) {
        LocalDate fileDate = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate oldDate = LocalDate.now().minusDays(2);
        return fileDate.isBefore(oldDate);
    }
}
