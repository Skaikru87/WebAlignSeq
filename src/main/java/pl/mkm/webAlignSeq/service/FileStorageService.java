package pl.mkm.webAlignSeq.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.biojava.bio.alignment.AlignmentPair;
import org.biojava.bio.alignment.SmithWaterman;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

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
            if(fileName.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence " + fileName);
            }
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            //start doing job with file
            String refDNA;
            String targetDNA;
            // *****from working code
            // File fileExel = new File("aligner.xlsx");
            File fileExel = new File(fileName);
            fileExel.createNewFile();
            FileOutputStream fos = new FileOutputStream(fileExel);
            fos.write(file.getBytes());
            fos.close();
            try (InputStream inp = new FileInputStream(fileExel)) {

                Workbook workbook = WorkbookFactory.create(inp);

                for (Sheet sheet : workbook) {
                    Row row = sheet.getRow(1);
                    Cell cell = row.getCell(1);
                    refDNA = cell.getStringCellValue();
                    char[] refDNAArr = refDNA.toCharArray();
                    for (int i = 0; i < refDNAArr.length; i++) {
                        cell = row.createCell(i + 2);
                        cell.setCellValue("" + refDNAArr[i]);
                    }

                    for (int i = 2; i < sheet.getLastRowNum() + 1; i++) {
                        Row row1 = sheet.getRow(i);
                        Cell cell1 = row1.getCell(1);
                        targetDNA = cell1.getStringCellValue();
                        int startNumber = getStartNumberOfAlignment(refDNA, targetDNA);

                        char[] targedAlignedArr = targetDNA.toCharArray();

                        for (int k = 0; k < targedAlignedArr.length; k++) {
                            if (k < startNumber - 1) {
                                cell = row1.createCell(k + 2);
                                cell.setCellValue("-");
                            }
                            cell = row1.createCell(k + startNumber + 1);
                            cell.setCellValue("" + targedAlignedArr[k]);
                        }
                    }
                }
                try (OutputStream exelWriter = new FileOutputStream(fileExel)) {
                    workbook.write(exelWriter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //end
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }


    private static int getStartNumberOfAlignment(String refDNA, String targetDNA) {
        FiniteAlphabet alphabet = (FiniteAlphabet) AlphabetManager.alphabetForName("DNA");
        //default values: 1,-1,2,2,2 respectively
        short match = 1;
        short replace = -1;
        short insert = 2;
        short delete = 2;
        short gapExtend = 2;
        SubstitutionMatrix substitutionMatrix;
        substitutionMatrix = new SubstitutionMatrix(alphabet, match, replace);
        Sequence ref;
        Sequence target;
        int startNumber = 0;
        SmithWaterman alignerSmith = new SmithWaterman(match, replace, insert, delete, gapExtend, substitutionMatrix);
        AlignmentPair alignmentPair;
        try {
            ref = DNATools.createDNASequence(refDNA, "refDNA");
            target = DNATools.createDNASequence(targetDNA, "targetDNA");
            alignmentPair = alignerSmith.pairwiseAlignment(target, ref);
            startNumber = alignmentPair.getSubjectStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return startNumber;
    }
}
