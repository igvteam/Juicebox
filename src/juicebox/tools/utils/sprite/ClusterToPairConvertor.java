package juicebox.tools.utils.sprite;

import juicebox.HiCGlobals;
import org.broad.igv.util.ParsingUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class ClusterToPairConvertor {


    final static public Pattern tabPattern = Pattern.compile("\t");
    final static public Pattern colonPattern = Pattern.compile(":");


    public static void main(String[] args) throws IOException {
        String pairsOutput = args[1];
        convert(args[0], args[1]);
    }

    public static void convert(String path, String output) throws IOException {

        PrintWriter pw = null;
        BufferedReader reader = null;

        try {
            if (path.endsWith(".gz")) {
                InputStream fileStream = new FileInputStream(path);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
                reader = new BufferedReader(decoder, 4194304);
            } else {
                //this.reader = org.broad.igv.util.ParsingUtils.openBufferedReader(path);
                reader = new BufferedReader(new InputStreamReader(ParsingUtils.openInputStream(path)), HiCGlobals.bufferSize);
            }

            pw = new PrintWriter(new BufferedWriter(new FileWriter(output)));

            int lineCount = 1;
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                String[] tokens = tabPattern.split(nextLine.trim());
                if (tokens.length > 2 && tokens.length < 1002) {
                    float score = 2.0f / (tokens.length - 1);   // Downweighted -- todo make this a parameter
                    Location[] locs = parseTokens(tokens);
                    for (int i = 0; i < locs.length; i++) {
                        for (int j = 0; j < locs.length; j++) {
                            if (i == j) continue;
                            String chr1 = locs[i].chr;
                            String chr2 = locs[j].chr;
                            String pos1 = locs[i].position;
                            String pos2 = locs[j].position;

                            //  Format: Short with score
                            //  str1 chr1 pos1 frag1 str2 chr2 pos2 frag2 score
                            int order = chr1.compareTo(chr2);
                            if (order < 0) {
                                pw.println("0\t" + chr1 + "\t" + pos1 + "\t-1\t0\t" + chr2 + "\t" + pos2 + "\t-1\t" + score);
                            } else {
                                pw.println("0\t" + chr2 + "\t" + pos2 + "\t-1\t0\t" + chr1 + "\t" + pos1 + "\t-1\t" + score);
                            }
                        }
                    }
                }
                if(lineCount % 1000 == 0) {
                    System.out.println(lineCount);
                }
                lineCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pw.close();
            reader.close();
        }


    }

    private static Location[] parseTokens(String[] tokens) {
        Location[] locs = new Location[tokens.length - 1];
        for (int i = 1; i < tokens.length; i++) {
            String[] t = colonPattern.split(tokens[i]);
            locs[i - 1] = new Location(t[0], t[1]);
        }
        return locs;
    }


    static class Location {
        String chr;
        String position;

        public Location(String chr, String position) {
            this.chr = chr;
            this.position = position;
        }
    }

}
