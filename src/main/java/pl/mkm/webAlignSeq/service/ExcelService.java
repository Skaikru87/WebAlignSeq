package pl.mkm.webAlignSeq.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.biojava.bio.BioException;
import org.biojava.bio.alignment.AlignmentPair;
import org.biojava.bio.alignment.SmithWaterman;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.mkm.webAlignSeq.biojava.MySubstitutionMatrix;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Service
public class ExcelService {

    @Autowired
    CsvService csvService;

    public void generateAlignmentInExcelCells(MultipartFile file, Path targetLocation) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        File fileExcel = new File(fileName);
        fileExcel.createNewFile();
        try (FileOutputStream fos = new FileOutputStream(fileExcel)) {
            fos.write(file.getBytes());
        }
        try (InputStream inp = new FileInputStream(fileExcel)) {
            Workbook workbook = WorkbookFactory.create(inp);
            try {
                for (Sheet sheet : workbook) {
                    int rowRefPosition = 3; //remember! first row is 0
                    int cellRefPosition = 2; // remember! first cell is 0
                    String refDNA = splitRefSeqToCells(sheet, rowRefPosition, cellRefPosition);
                    splitTargetToCells(refDNA, sheet, rowRefPosition, cellRefPosition);

                    addRules(sheet);
                }
            } catch (NullPointerException e) {
                System.out.println("empty sheet!, please remove empty sheet and try again");
            }
            try (OutputStream excelWriter = new FileOutputStream(fileExcel)) {
                workbook.write(excelWriter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Files.copy(fileExcel.toPath(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        Files.delete(fileExcel.toPath());
    }

    private void addRules(Sheet sheet) {
        SheetConditionalFormatting conditionalFormatting = sheet.getSheetConditionalFormatting();
        CellRangeAddress[] range= {CellRangeAddress.valueOf("A1:XFD300000")};

        ConditionalFormattingRule ruleT = conditionalFormatting.createConditionalFormattingRule(ComparisonOperator.EQUAL,"\"T\"");
        PatternFormatting formatT = ruleT.createPatternFormatting();
        formatT.setFillBackgroundColor(IndexedColors.RED1.getIndex());
        conditionalFormatting.addConditionalFormatting(range, ruleT);

        ConditionalFormattingRule ruleA = conditionalFormatting.createConditionalFormattingRule(ComparisonOperator.EQUAL,"\"A\"");
        PatternFormatting formatA = ruleA.createPatternFormatting();
        formatA.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        conditionalFormatting.addConditionalFormatting(range, ruleA);

        ConditionalFormattingRule ruleC = conditionalFormatting.createConditionalFormattingRule(ComparisonOperator.EQUAL,"\"C\"");
        PatternFormatting formatC = ruleC.createPatternFormatting();
        formatC.setFillBackgroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        conditionalFormatting.addConditionalFormatting(range, ruleC);

        ConditionalFormattingRule ruleG = conditionalFormatting.createConditionalFormattingRule(ComparisonOperator.EQUAL,"\"G\"");
        PatternFormatting formatG = ruleG.createPatternFormatting();
        formatG.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        conditionalFormatting.addConditionalFormatting(range, ruleG);
    }



    private String splitRefSeqToCells(Sheet sheet, int rowRefPosition, int cellRefPosition) {
        Row row = sheet.getRow(rowRefPosition);
        Cell cell = row.getCell(cellRefPosition, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        String refDNA = cell.getStringCellValue().replaceAll("\\s", "");
        char[] refDNAArr = refDNA.toCharArray();
        for (int i = 0; i < refDNAArr.length; i++) {
            cell = row.createCell(i + 1 + cellRefPosition);
            cell.setCellValue("" + refDNAArr[i]);
        }
        addPositionOnChromosome(sheet, rowRefPosition, cellRefPosition, refDNAArr);
        return refDNA;
    }

    private void addPositionOnChromosome(Sheet sheet, int rowRefPosition, int cellRefPosition, char[] refDNAArr) {
        if (!(sheet.getRow(rowRefPosition - 1) == null)) {
            Row rowWithPosition = sheet.getRow(rowRefPosition - 1);
            Cell cellWithPosition = rowWithPosition.getCell(cellRefPosition, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            double positionChromosome = cellWithPosition.getNumericCellValue();
            for (int i = 0; i < refDNAArr.length; i++) {
                cellWithPosition = rowWithPosition.createCell(i + 1 + cellRefPosition);
                cellWithPosition.setCellValue(positionChromosome + i);
            }
            addRsAndAllelesInfo(sheet, rowRefPosition, cellRefPosition, refDNAArr);
        } else {
            Row rowWithPosition = sheet.createRow(rowRefPosition - 1);
            for (int i = 0; i < refDNAArr.length; i++) {
                Cell cellNumber = rowWithPosition.createCell(i + 1 + cellRefPosition);
                cellNumber.setCellValue(i + 1);
            }
        }
    }

    private void addRsAndAllelesInfo(Sheet sheet, int rowRefPosition, int cellRefPosition, char[] refDNAArr) {
        List<CsvService.SNPinfo> listOfSNPinfo = csvService.readGeneInfo();
        Row rowWithAlleles = sheet.createRow(rowRefPosition - 2);
        Row rowWithRs = sheet.createRow(rowRefPosition - 3);
        for (int i = 0; i < refDNAArr.length; i++) {
            Cell cellWithAllel = rowWithAlleles.createCell(i + 1 + cellRefPosition);
            Cell cellWithRs = rowWithRs.createCell(i + 1 + cellRefPosition);
            for (CsvService.SNPinfo snp : listOfSNPinfo) {
                Row rowWithPosition2 = sheet.getRow(rowRefPosition - 1);
                if (snp.getLocation() == rowWithPosition2.getCell(i + 1 + cellRefPosition).getNumericCellValue()) {
                    cellWithAllel.setCellValue(snp.getAlleles());
                    cellWithRs.setCellValue(snp.getRsNumber());
                }
            }
        }
    }

    private void splitTargetToCells(String refDNA, Sheet sheet, int rowRefPosition, int cellRefPosition) {
        String targetDNA;
        Cell cell;
        for (int i = rowRefPosition + 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row1 = sheet.getRow(i);
            Cell cell1 = row1.getCell(cellRefPosition);
            targetDNA = cell1.getStringCellValue().replaceAll("\\s", "");
            int startNumber = getStartNumberOfAlignment(refDNA, targetDNA);
            char[] targedAlignedArr = targetDNA.toCharArray();
            for (int k = 0; k < targedAlignedArr.length; k++) {
                if (k < startNumber - 1) {
                    cell = row1.createCell(k + cellRefPosition + 1);
                    cell.setCellValue("-");
                }
                cell = row1.createCell(k + startNumber + cellRefPosition);
                cell.setCellValue("" + targedAlignedArr[k]);
            }
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
        MySubstitutionMatrix substitutionMatrix = new MySubstitutionMatrix(alphabet, match, replace);
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
        } catch (BioException e) {
            log.info(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return startNumber;
    }
}