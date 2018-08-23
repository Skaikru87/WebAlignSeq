package pl.mkm.webAlignSeq.service;

import org.apache.poi.ss.usermodel.*;
import org.biojava.bio.alignment.AlignmentPair;
import org.biojava.bio.alignment.SmithWaterman;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class ExcelService {

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

    private CellStyle setCellStyleDifferNucleotides(Workbook workbook, char nucleotide) {
        CellStyle style = workbook.createCellStyle();
        if (nucleotide == 'T') style.setFillForegroundColor(IndexedColors.RED1.getIndex());
        if (nucleotide == 'A') style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        if (nucleotide == 'C') style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        if (nucleotide == 'G') style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String splitRefSeqToCells(Sheet sheet, int rowRefPosition, int cellRefPosition) {
        String refDNA;
        Row row = sheet.getRow(rowRefPosition);
        Cell cell = row.getCell(cellRefPosition, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        refDNA = cell.getStringCellValue().replaceAll("\\s", "");
        char[] refDNAArr = refDNA.toCharArray();
        for (int i = 0; i < refDNAArr.length; i++) {
            cell = row.createCell(i + 1 + cellRefPosition);
            cell.setCellValue("" + refDNAArr[i]);
            cell.setCellStyle(setCellStyleDifferNucleotides(sheet.getWorkbook(), refDNAArr[i]));
        }
        addPositionOnChromosome(sheet, rowRefPosition, cellRefPosition, refDNAArr);
        return refDNA;
    }

    private void addPositionOnChromosome(Sheet sheet, int rowRefPosition, int cellRefPosition, char[] refDNAArr) {
        if (!(sheet.getRow(rowRefPosition - 1) == null)) {
            Row rowNumber = sheet.getRow(rowRefPosition - 1);
            Cell cellNumber = rowNumber.getCell(cellRefPosition, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            double positionChromosome = cellNumber.getNumericCellValue();
            for (int i = 0; i < refDNAArr.length; i++) {
                cellNumber = rowNumber.createCell(i + 1 + cellRefPosition);
                cellNumber.setCellValue(positionChromosome + i);
            }
        } else {
            Row rowNumber = sheet.createRow(rowRefPosition - 1);
            for (int i = 0; i < refDNAArr.length; i++) {
                Cell cellNumber = rowNumber.createCell(i + 1 + cellRefPosition);
                cellNumber.setCellValue(i + 1);
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
                cell.setCellStyle(setCellStyleDifferNucleotides(sheet.getWorkbook(), targedAlignedArr[k]));
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
