//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package pl.mkm.webAlignSeq.biojava;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.biojava.bio.BioException;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;

public class MySubstitutionMatrix extends SubstitutionMatrix implements Serializable {
    private Map<Symbol, Integer> rowSymbols;
    private Map<Symbol, Integer> colSymbols;
    private short[][] matrix;
    private short min;
    private short max;
    private FiniteAlphabet alphabet;
    private String description;
    private String name;
    private static final String newLine = System.getProperty("line.separator");

    public MySubstitutionMatrix(FiniteAlphabet alpha, File matrixFile) throws BioException, NumberFormatException, IOException {

        super(alpha, matrixFile);
        this.name = matrixFile.getName();
        this.rowSymbols = new HashMap();
        this.colSymbols = new HashMap();
        this.matrix = this.parseMatrix(matrixFile);
    }

    public MySubstitutionMatrix(FiniteAlphabet alpha, String matrixString, String name) throws BioException, NumberFormatException, IOException {
        super(alpha, matrixString, name);
        this.name = name;
        this.rowSymbols = new HashMap();
        this.colSymbols = new HashMap();
        this.matrix = this.parseMatrix(matrixString);
    }

    public MySubstitutionMatrix(FiniteAlphabet alpha, short match, short replace) {
        super(alpha,match,replace);
//        int i = false;
//        int j = false;
        this.alphabet = alpha;
        this.description = "Identity matrix. All replaces and all matches are treated equally.";
        this.name = "IDENTITY_" + match + "_" + replace;
        this.rowSymbols = new HashMap();
        this.colSymbols = new HashMap();
        this.matrix = new short[alpha.size()][alpha.size()];
        Symbol[] sym = new Symbol[alpha.size()];
        Iterator<Symbol> iter = alpha.iterator();

        int i;
        for(i = 0; iter.hasNext(); ++i) {
            sym[i] = (Symbol)iter.next();
            this.rowSymbols.put(sym[i], new Integer(i));
            this.colSymbols.put(sym[i], new Integer(i));
        }

        for(i = 0; i < this.alphabet.size(); ++i) {
            for(int j = 0; j < this.alphabet.size(); ++j) {
                if (sym[i].getMatches().contains(sym[j])) {
                    this.matrix[i][j] = match;
                } else {
                    this.matrix[i][j] = replace;
                }
            }
        }

    }

    public MySubstitutionMatrix(File file) throws NumberFormatException, NoSuchElementException, BioException, IOException {
        this(guessAlphabet(file), file);
    }

    public static MySubstitutionMatrix getSubstitutionMatrix(BufferedReader reader) throws NumberFormatException, BioException, IOException {
        StringBuffer stringMatrix = new StringBuffer("");

        while(reader.ready()) {
            stringMatrix.append(reader.readLine());
            stringMatrix.append(newLine);
        }

        reader.close();
        String mat = stringMatrix.toString();
        FiniteAlphabet alpha = guessAlphabet(new BufferedReader(new StringReader(mat)));
        MySubstitutionMatrix matrix = new MySubstitutionMatrix(alpha, mat, "unknown");
        return matrix;
    }

    public static MySubstitutionMatrix getSubstitutionMatrix(FiniteAlphabet alphabet, BufferedReader reader) throws BioException, IOException {
        if (alphabet == null) {
            throw new NullPointerException("alphabet must not be null");
        } else if (reader == null) {
            throw new NullPointerException("reader must not be null");
        } else {
            return new MySubstitutionMatrix(alphabet, toString(reader), "unknown");
        }
    }

    private static String toString(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while(reader.ready()) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }

                sb.append(line);
                sb.append(newLine);
            }

            line = sb.toString();
        } finally {
            try {
                reader.close();
            } catch (Exception var9) {
                ;
            }

        }

        return line;
    }

    public static MySubstitutionMatrix getSubstitutionMatrix(FiniteAlphabet alphabet, BufferedReader reader, String name) throws BioException, IOException {
        if (alphabet == null) {
            throw new NullPointerException("alphabet must not be null");
        } else if (reader == null) {
            throw new NullPointerException("reader must not be null");
        } else if (name == null) {
            throw new NullPointerException("name must not be null");
        } else {
            return new MySubstitutionMatrix(alphabet, toString(reader), name);
        }
    }

    private static BufferedReader readResource(String name) {
        return new BufferedReader(new InputStreamReader(MySubstitutionMatrix.class.getResourceAsStream(name)));
    }

    private static MySubstitutionMatrix getNucleotideMatrix(String name) {
        try {
            return getSubstitutionMatrix(DNATools.getDNA(), readResource(name), name);
        } catch (BioException var2) {
            throw new RuntimeException("could not load substitution matrix " + name + " from classpath", var2);
        } catch (IOException var3) {
            throw new RuntimeException("could not load substitution matrix " + name + " from classpath", var3);
        }
    }

    private static MySubstitutionMatrix getAminoAcidMatrix(String name) {
        try {
            return getSubstitutionMatrix(ProteinTools.getTAlphabet(), readResource(name), name);
        } catch (BioException var2) {
            throw new RuntimeException("could not load substitution matrix " + name + " from classpath", var2);
        } catch (IOException var3) {
            throw new RuntimeException("could not load substitution matrix " + name + " from classpath", var3);
        }
    }



    private static FiniteAlphabet guessAlphabet(File file) throws IOException, NoSuchElementException, BioException {
        String fileName = file.getName().toLowerCase();
        return !fileName.contains("pam") && !fileName.contains("blosum") ? guessAlphabet(new BufferedReader(new FileReader(file))) : (FiniteAlphabet)AlphabetManager.alphabetForName("PROTEIN-TERM");
    }

    private static FiniteAlphabet guessAlphabet(BufferedReader reader) throws IOException, BioException {
        FiniteAlphabet alphabet = null;

        while(reader.ready()) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            String trim = line.trim();
            if (trim.length() != 0 && trim.charAt(0) != '#' && (line.charAt(0) == ' ' || line.charAt(0) == '\t')) {
                String[] alphabets = new String[]{"DNA", "RNA", "PROTEIN", "PROTEIN-TERM"};

                for(int i = 0; i < alphabets.length; ++i) {
                    alphabet = (FiniteAlphabet)AlphabetManager.alphabetForName(alphabets[i]);
                    SymbolTokenization symtok = alphabet.getTokenization("token");
                    StringTokenizer st = new StringTokenizer(trim);
                    boolean noError = true;

                    for(int var9 = 0; st.hasMoreElements(); ++var9) {
                        try {
                            symtok.parseToken(st.nextElement().toString());
                        } catch (IllegalSymbolException var11) {
                            noError = false;
                            break;
                        }
                    }

                    if (noError) {
                        return alphabet;
                    }
                }
            }
        }

        throw new BioException("Unknow alphabet used in this substitution matrix");
    }

    private short[][] parseMatrix(Object matrixObj) throws BioException, NumberFormatException, IOException {
        //int j = false;
        int rows = 0;
        int cols = 0;
        SymbolTokenization symtok = this.alphabet.getTokenization("token");
        this.min = 32767;
        this.max = -32768;
        Object reader;
        if (matrixObj instanceof File) {
            reader = new FileReader((File)matrixObj);
        } else {
            if (!(matrixObj instanceof String)) {
                return (short[][])null;
            }

            reader = new StringReader(matrixObj.toString());
        }

        BufferedReader br = new BufferedReader((Reader)reader);

        StringTokenizer st;
        String line;
        String trim;
        int j;
        while(br.ready()) {
            line = br.readLine();
            if (line == null) {
                break;
            }

            trim = line.trim();
            if (trim.length() != 0) {
                if (trim.charAt(0) == '#') {
                    this.description = this.description + line.substring(1);
                } else if (!line.startsWith(newLine)) {
                    if (line.charAt(0) != ' ' && line.charAt(0) != '\t') {
                        st = new StringTokenizer(trim);
                        if (st.hasMoreElements()) {
                            this.rowSymbols.put(symtok.parseToken(st.nextElement().toString()), rows++);
                        }
                    } else {
                        st = new StringTokenizer(trim);

                        for(j = 0; st.hasMoreElements(); ++j) {
                            this.colSymbols.put(symtok.parseToken(st.nextElement().toString()), j);
                        }

                        cols = j;
                    }
                }
            }
        }

        br.close();
        short[][] matrix = new short[rows][cols];
        rows = 0;
        if (matrixObj instanceof File) {
            reader = new FileReader((File)matrixObj);
        } else {
            if (!(matrixObj instanceof String)) {
                return (short[][])null;
            }

            reader = new StringReader(matrixObj.toString());
        }

        br = new BufferedReader((Reader)reader);

        while(br.ready()) {
            line = br.readLine();
            if (line == null) {
                break;
            }

            trim = line.trim();
            if (trim.length() != 0 && trim.charAt(0) != '#' && line.charAt(0) != ' ' && line.charAt(0) != '\t' && !line.startsWith(newLine)) {
                st = new StringTokenizer(trim);
                if (st.hasMoreElements()) {
                    st.nextElement();
                }

                for(j = 0; st.hasMoreElements(); ++j) {
                    matrix[rows][j] = (short)((int)Math.round(Double.parseDouble(st.nextElement().toString())));
                    if (matrix[rows][j] > this.max) {
                        this.max = matrix[rows][j];
                    }

                    if (matrix[rows][j] < this.min) {
                        this.min = matrix[rows][j];
                    }
                }

                ++rows;
            }
        }

        br.close();
        return matrix;
    }

    public short getValueAt(Symbol row, Symbol col) throws BioException {
        if (this.rowSymbols.containsKey(row) && this.colSymbols.containsKey(col)) {
            return this.matrix[(Integer)this.rowSymbols.get(row)][(Integer)this.colSymbols.get(col)];
        } else {
            //System.err.printf("SubstitutionMatrix: No entry for the symbols %s and %s\n", row.getName(), col.getName());
            return 0;
        }
    }

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public short getMin() {
        return this.min;
    }

    public short getMax() {
        return this.max;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public FiniteAlphabet getAlphabet() {
        return this.alphabet;
    }

    public String stringnifyMatrix() {
        int i = 0;
        StringBuffer matrixString = new StringBuffer();
        Symbol[] colSyms = new Symbol[this.colSymbols.keySet().size()];

        try {
            SymbolTokenization symtok = this.alphabet.getTokenization("default");
            matrixString.append("  ");
            Iterator colKeys = this.colSymbols.keySet().iterator();

            while(colKeys.hasNext()) {
                colSyms[i] = (Symbol)colKeys.next();
                matrixString.append(symtok.tokenizeSymbol(colSyms[i++]).toUpperCase());
                matrixString.append(' ');
            }

            matrixString.append(newLine);
            Iterator rowKeys = this.rowSymbols.keySet().iterator();

            while(rowKeys.hasNext()) {
                Symbol rowSym = (Symbol)rowKeys.next();
                matrixString.append(symtok.tokenizeSymbol(rowSym).toUpperCase());
                matrixString.append(' ');

                for(i = 0; i < colSyms.length; ++i) {
                    matrixString.append(this.getValueAt(rowSym, colSyms[i]));
                    matrixString.append(' ');
                }

                matrixString.append(newLine);
            }
        } catch (BioException var8) {
            var8.printStackTrace();
        }

        return matrixString.toString();
    }

    public String stringnifyDescription() {
        StringBuffer desc = new StringBuffer();
        StringBuffer line = new StringBuffer();
        line.append("# ");
        StringTokenizer st = new StringTokenizer(this.description, " ");

        while(st.hasMoreElements()) {
            line.append(st.nextElement().toString());
            line.append(' ');
            if (line.length() >= 60) {
                desc.append(line);
                desc.append(newLine);
                if (st.hasMoreElements()) {
                    line = new StringBuffer();
                    line.append("# ");
                }
            } else if (!st.hasMoreElements()) {
                desc.append(line);
                desc.append(newLine);
            }
        }

        return desc.toString();
    }

    public String toString() {
        StringBuffer desc = new StringBuffer();
        StringBuffer line = new StringBuffer();
        line.append("# ");
        StringTokenizer st = new StringTokenizer(this.description);

        while(st.hasMoreElements()) {
            line.append(st.nextElement().toString());
            line.append(' ');
            if (line.length() >= 60) {
                desc.append(line);
                desc.append(newLine);
                if (st.hasMoreElements()) {
                    line = new StringBuffer();
                    line.append("# ");
                }
            } else if (!st.hasMoreElements()) {
                desc.append(line);
                desc.append(newLine);
            }
        }

        desc.append(this.stringnifyMatrix());
        return desc.toString();
    }

    public void printMatrix() {
        Iterator rowKeys = this.rowSymbols.keySet().iterator();

        while(rowKeys.hasNext()) {
            Iterator<Symbol> colKeys = this.colSymbols.keySet().iterator();
            Symbol rowSym = (Symbol)rowKeys.next();
            System.out.print(rowSym.getName() + "\t");

            while(colKeys.hasNext()) {
                Symbol colSym = (Symbol)colKeys.next();
                int x = (Integer)this.rowSymbols.get(rowSym);
                int y = (Integer)this.colSymbols.get(colSym);
                System.out.print(colSym.getName() + " " + " " + x + " " + y + " " + this.matrix[x][y] + "\t");
            }

            System.out.println(newLine);
        }

        System.out.println(this.toString());
    }

    public MySubstitutionMatrix normalizeMatrix() throws BioException, NumberFormatException, IOException {
        short min = this.getMin();
        short newMax = -32768;
        short[][] mat = new short[this.matrix.length][this.matrix[this.matrix.length - 1].length];
        String name = this.getName() + "_normalized";
        String matString = this.stringnifyDescription() + "  ";
        FiniteAlphabet alphabet = this.getAlphabet();
        Map<Symbol, Integer> rowMap = this.rowSymbols;
        Map<Symbol, Integer> colMap = this.colSymbols;
        SymbolTokenization symtok = alphabet.getTokenization("default");

        int i;
        int j;
        for(i = 0; i < this.matrix.length; ++i) {
            for(j = 0; j < this.matrix[this.matrix.length - 1].length; ++j) {
                mat[i][j] = (short)(this.matrix[i][j] - min);
                if (mat[i][j] > newMax) {
                    newMax = mat[i][j];
                }
            }
        }

        for(i = 0; i < mat.length; ++i) {
            for(j = 0; j < mat[mat.length - 1].length; ++j) {
                mat[i][j] = (short)(mat[i][j] * 10 / newMax);
            }
        }

        Object[] rows = this.rowSymbols.keySet().toArray();
        Object[] cols = this.colSymbols.keySet().toArray();

        for(i = 0; i < cols.length; ++i) {
            matString = matString + symtok.tokenizeSymbol((Symbol)cols[i]) + " ";
        }

        for(i = 0; i < rows.length; ++i) {
            matString = matString + newLine + symtok.tokenizeSymbol((Symbol)rows[i]) + " ";

            for(j = 0; j < cols.length; ++j) {
                matString = matString + mat[(Integer)rowMap.get((Symbol)rows[i])][(Integer)colMap.get((Symbol)cols[j])] + " ";
            }
        }

        matString = matString + newLine;
        return new MySubstitutionMatrix(alphabet, matString, name);
    }
}
