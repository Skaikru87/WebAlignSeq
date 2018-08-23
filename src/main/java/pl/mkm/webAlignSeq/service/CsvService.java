package pl.mkm.webAlignSeq.service;

import lombok.Data;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    private File csvFile = new File("/uploads/gene_info.csv");

    public List<SNPinfo> readGeneInfo() {

        List<SNPinfo> dataFromCSV = new ArrayList<>();
        try {
            Reader reader = new FileReader(csvFile);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);

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
        } catch (java.io.IOException e) {
            e.getMessage();
        }
        return dataFromCSV;
    }

    @Data
    public class SNPinfo {

        String rsNumber;
        long location;
        String alleles;

    }
}
