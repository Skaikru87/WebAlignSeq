package pl.mkm.webAlignSeq.service;

import lombok.Data;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    @Autowired
    AdditionalFileStorageService additionalFileStorageService;

    private File csvFile = new File("uploads/gene_info.csv");


    public List<SNPinfo> readGeneInfo() {

        List<SNPinfo> dataFromCSV = new ArrayList<>();
        try {
            //Reader reader = new FileReader(csvFile);
            InputStream in = new FileInputStream(csvFile);
            Reader reader = newReader(in);
            Iterable<CSVRecord> records = CSVFormat.newFormat(';').withFirstRecordAsHeader().parse(reader);

//            CSVFormat format = CSVFormat.newFormat(';').withFirstRecordAsHeader();
//            CSVParser records = CSVParser.parse(csvFile,StandardCharsets.UTF_8, format);

            for (CSVRecord record : records) {
                SNPinfo snpInfo = new SNPinfo();
                snpInfo.setRsNumber(record.get("Variant ID"));
                snpInfo.setAlleles(record.get("Alleles"));

                String positionInString = record.get("Location");
                String[] parts = positionInString.split(":");
                long position = Long.parseLong(parts[1]);
                snpInfo.setLocation(position);

                dataFromCSV.add(snpInfo);
            }
            reader.close();
           // records.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.getMessage();
        }
        return dataFromCSV;
    }

    public InputStreamReader newReader(final InputStream inputStream) {
        return new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8);
    }
    @Data
    public class SNPinfo {

        String rsNumber;
        long location;
        String alleles;

    }
}
