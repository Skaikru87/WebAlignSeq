package pl.mkm.webAlignSeq.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CsvService {

    private File csvFile = new File("uploads/gene_info.csv");

    //headers from csv file
    private static final String VARIANT_ID = "Variant ID";
    private static final String ALLELES = "Alleles";
    private static final String LOCATION = "Location";

    public List<SNPinfo> readGeneInfo() {

        List<SNPinfo> dataFromCSV = new ArrayList<>();
        try {
            Reader reader = new FileReader(csvFile);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            log.info("reading csv file...");
            for (CSVRecord record : records) {
                //log.info(" reading csv... record number: " + record.getRecordNumber());
                SNPinfo snpInfo = new SNPinfo();

                snpInfo.setRsNumber(record.get(VARIANT_ID));
                snpInfo.setAlleles(record.get(ALLELES));
                String positionInString = record.get(LOCATION);
                String[] parts = positionInString.split(":");
                long position = Long.parseLong(parts[1]);
                snpInfo.setLocation(position);

                dataFromCSV.add(snpInfo);
            }
            reader.close();

        } catch (FileNotFoundException e) {
            System.err.println("csv file not found");
            e.getMessage();
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
