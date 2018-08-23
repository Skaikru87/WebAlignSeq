package pl.mkm.webAlignSeq.response;

import lombok.Data;

@Data
public class UploadAdditionalFileResponse {
    private String fileName;
    private String fileType;
    private long size;

    public UploadAdditionalFileResponse(String fileName,String fileType, long size) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.size = size;
    }

}
