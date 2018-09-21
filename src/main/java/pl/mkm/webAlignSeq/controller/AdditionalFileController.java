package pl.mkm.webAlignSeq.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.mkm.webAlignSeq.response.UploadAdditionalFileResponse;
import pl.mkm.webAlignSeq.service.AdditionalFileStorageService;
import pl.mkm.webAlignSeq.validator.ValidFile;

@RestController
@Validated
public class AdditionalFileController {

    @Autowired
    AdditionalFileStorageService additionalFileStorageService;

    @PostMapping("/uploadAdditionalFile")
    public UploadAdditionalFileResponse UploadAdditionalFile(@ValidFile @RequestParam("additionalFile") MultipartFile file) {
        String fileName = additionalFileStorageService.storeFile(file);
        return new UploadAdditionalFileResponse(fileName, file.getContentType(), file.getSize());

    }

}
