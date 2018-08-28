package pl.mkm.webAlignSeq.service;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class CsvService {


    private File csvFile = new File("uploads/gene_info.csv");

    public List<SNPinfo> readGeneInfo() {

        List<SNPinfo> dataFromCSV = new ArrayList<>();
      //  final  Charset charset;
        try {
            Reader reader = new FileReader(csvFile);
            //InputStream in = new FileInputStream(csvFile);
            //Reader reader = newReader(in);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

//            CharsetDetector charsetDetector = new CharsetDetector();
//            charsetDetector.setText(in);
//            CharsetMatch charsetMatch = charsetDetector.detect();
//            charset = Charset.forName(charsetMatch.getName());
//
//            CSVFormat format = CSVFormat.TDF.withFirstRecordAsHeader();
//            CSVParser records = CSVParser.parse(csvFile, StandardCharsets.US_ASCII, format);
//            log.info("charset: " + charset);

            for (CSVRecord record : records) {
                log.info(" reading csv: " + record.getRecordNumber());
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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.getMessage();
        }
        return dataFromCSV;
    }

//    public InputStreamReader newReader(final InputStream inputStream) {
//        return new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8);
//    }

    @Data
    public class SNPinfo {

        String rsNumber;
        long location;
        String alleles;

    }
}
