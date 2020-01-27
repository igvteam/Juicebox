package juicebox.tools.utils.original;

import juicebox.HiCGlobals;
import org.broad.igv.util.ParsingUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class ClusterPairIterator implements PairIterator {

    final static public Pattern tabPattern = Pattern.compile("\t");
    final static public Pattern colonPattern = Pattern.compile(":");

    // Map of name -> index
    private Map<String, Integer> chromosomeOrdinals;
    private BufferedReader reader;
    private Deque<AlignmentPair> alignmentPairs = new ArrayDeque<>();

    public ClusterPairIterator(String path, Map<String, Integer> chromosomeOrdinals) throws IOException {
        if (path.endsWith(".gz")) {
            InputStream fileStream = new FileInputStream(path);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
            this.reader = new BufferedReader(decoder, 4194304);
        } else {
            //this.reader = org.broad.igv.util.ParsingUtils.openBufferedReader(path);
            this.reader = new BufferedReader(new InputStreamReader(ParsingUtils.openInputStream(path)), HiCGlobals.bufferSize);
        }
        this.chromosomeOrdinals = chromosomeOrdinals;
        advance();
    }

    /**
     * Read the next cluster
     * DPM6G7.NYBot43_Stg.Odd2Bo68.Even2Bo32.Odd2Bo82	ctg1_len_6253557:1840834	ctg1_len_6253557:4790817
     */
    private void advance() {
Set<String> missingChroms = new HashSet<>();
        boolean eof = false;
        while(!eof && alignmentPairs.isEmpty()) {
            try {
                String nextLine;
                String[] tokens = null;
                while ((nextLine = reader.readLine()) != null) {
                    //String[] tokens = Globals.singleTabMultiSpacePattern.split(nextLine);
                    String[] t = tabPattern.split(nextLine.trim());
                    if (t.length > 2) {
                        tokens = t;
                        break;
                    }
                }

                if (tokens == null) {
                    eof = true;
                } else {
                    float score = 2.0f / (tokens.length - 1);   // Downweighted -- todo make this a parameter
                    Location[] locs = parseTokens(tokens);
                    for (int i = 0; i < locs.length; i++) {
                        for (int j = 0; j < locs.length; j++) {
                            if (i == j) continue;
                            Integer c1 = this.chromosomeOrdinals.get(locs[i].chr.toUpperCase());
                            Integer c2 = this.chromosomeOrdinals.get(locs[j].chr.toUpperCase());
//                            if(c1 == null && !missingChroms.contains(locs[i].chr)) {
//                                missingChroms.add(locs[i].chr);
//                                System.out.println(locs[i].chr);
//                            }
//                            if(c2 == null&& !missingChroms.contains(locs[j].chr)) {
//                                missingChroms.add(locs[j].chr);
//                                System.out.println(locs[j].chr);
//                            }
                            if (c1 != null && c2 != null) {
                                int pos1 = locs[i].position;
                                int pos2 = locs[j].position;
                                alignmentPairs.push(new AlignmentPair(c1, pos1, c2, pos2, score));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Location[] parseTokens(String[] tokens) {
        Location[] locs = new Location[tokens.length - 1];
        for (int i = 1; i < tokens.length; i++) {
            String[] t = colonPattern.split(tokens[i]);
            locs[i - 1] = new Location(t[0], Integer.parseInt(t[1]));
        }
        return locs;
    }

    public boolean hasNext() {
        return !alignmentPairs.isEmpty();
    }

    public AlignmentPair next() {
        AlignmentPair p = alignmentPairs.pop();
        if (alignmentPairs.isEmpty()) {
            advance();
        }
        return p;

    }

    public void remove() {
        // Not implemented
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    static class Location {
        String chr;
        int position;

        public Location(String chr, int position) {
            this.chr = chr;
            this.position = position;
        }
    }

}
