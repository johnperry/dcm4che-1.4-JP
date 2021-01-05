

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.dcm4che.dict.UIDs;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 15881 $
 * @since 01.11.2003
 */
public final class TranscoderMain
{

    private static ResourceBundle rb =
        ResourceBundle.getBundle(TranscoderMain.class.getName());

    public static void main(String[] args)
    {
        int c;
        LongOpt[] longopts =
            {
                new LongOpt("trunc-post-pixeldata", LongOpt.NO_ARGUMENT, null, 't'),
                new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
                new LongOpt("ivle", LongOpt.NO_ARGUMENT, null, 'd'),
                new LongOpt("evle", LongOpt.NO_ARGUMENT, null, 'e'),
                new LongOpt("evbe", LongOpt.NO_ARGUMENT, null, 'b'),
                new LongOpt("jpll", LongOpt.NO_ARGUMENT, null, 'l'),
                new LongOpt("jlsl", LongOpt.NO_ARGUMENT, null, 's'),
                new LongOpt("j2kr", LongOpt.NO_ARGUMENT, null, 'r'),
                new LongOpt("jply", LongOpt.OPTIONAL_ARGUMENT, null, 'y'),
                new LongOpt("j2ki", LongOpt.OPTIONAL_ARGUMENT, null, 'i'),
                new LongOpt("concurrent", LongOpt.OPTIONAL_ARGUMENT, null, 'c'),
                };
        // 
        Getopt g = new Getopt("dcm4chex-codec", args, "jhv", longopts, true);
        Transcoder t = new Transcoder();
        boolean concurrent = false;
        while ((c = g.getopt()) != -1)
            switch (c)
            {
                case 'd' :
                    t.setTransferSyntax(UIDs.ImplicitVRLittleEndian);
                    break;
                case 'e' :
                    t.setTransferSyntax(UIDs.ExplicitVRLittleEndian);
                    break;
                case 'b' :
                    t.setTransferSyntax(UIDs.ExplicitVRBigEndian);
                    break;
                case 'l' :
                    t.setTransferSyntax(UIDs.JPEGLossless);
                    break;
                case 's' :
                    t.setTransferSyntax(UIDs.JPEGLSLossless);
                    break;
                case 'r' :
                    t.setTransferSyntax(UIDs.JPEG2000Lossless);
                    break;
                case 'y' :
                    t.setTransferSyntax(UIDs.JPEGBaseline);
                    t.setCompressionQuality(
                        toCompressionQuality(g.getOptarg()));
                    break;
                case 'i' :
                    t.setTransferSyntax(UIDs.JPEG2000Lossy);
                    t.setEncodingRate(toEncodingRate(g.getOptarg()));
                    break;
                case 't' :
                    t.setTruncatePostPixelData(true);
                    break;
                case 'v' :
                    System.out.println(
                        MessageFormat.format(
                            rb.getString("version"),
                            new Object[] {
                                Package
                                    .getPackage("org.dcm4chex.codec")
                                    .getImplementationVersion()}));
                    return;
                case '?' :
                case 'h' :
                    System.out.println(rb.getString("usage"));
                    return;
                case 'c' :
                    concurrent  = true;
                    break;
            }
        if (!checkArgs(g.getOptind(), args))
        {
            System.out.println(rb.getString("usage"));
            return;
        }
        File dest = new File(args[args.length - 1]);
        for (int i = g.getOptind(); i + 1 < args.length; ++i)
        {
            transcode(t, new File(args[i]), dest, concurrent);
        }
    }

    private static void transcode(final Transcoder t, final File src, final File dest,
            boolean concurrent)
    {
        if (src.isDirectory())
        {
            File[] file = src.listFiles();
            for (int i = 0; i < file.length; i++)
            {
                transcode(t, file[i], dest, concurrent);
            }
        } else if (concurrent) {
            new Thread(new Runnable(){

                public void run() {
                    transcodeFile(new Transcoder(t), src, dest);
                }}).start();
        } else {
            transcodeFile(t, src, dest);
        }
    }

    private static void transcodeFile(Transcoder t, File src, File dest) {
        try
        {
            File outFile =
                dest.isDirectory() ? new File(dest, src.getName()) : dest;
            long srcLength = src.length();
            System.out.print(
                ""
                    + src
                    + " ["
                    + (srcLength >>> 10)
                    + " KB] -> "
                    + outFile);
            long begin = System.currentTimeMillis();
            t.transcode(src, outFile);
            long end = System.currentTimeMillis();
            long destLength = outFile.length();
            System.out.println(" [" + (destLength >>> 10) + " KB] ");
            System.out.println(
                "  takes "
                    + (end - begin)
                    + " ms, compression rate= "
                    + ((destLength < srcLength)
                        ? (srcLength / (float) destLength) + " : 1"
                        : "1 : " + (destLength / (float) srcLength)));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private static boolean checkArgs(int off, String[] args)
    {
        switch (args.length - off)
        {
            case 0 :
                System.out.println(rb.getString("missingArgs"));
                return false;
            case 1 :
                System.out.println(rb.getString("missingDest"));
                return false;
            case 2 :
                if (!(new File(args[off])).isDirectory())
                    break;
            default :
                if (!(new File(args[args.length - 1])).isDirectory())
                {
                    System.out.println(
                        MessageFormat.format(
                            rb.getString("needDir"),
                            new Object[] { args[args.length - 1] }));
                    return false;
                }
        }
        return true;
    }

    private static float toCompressionQuality(String s)
    {
        if (s != null)
        {
            try
            {
                int quality = Integer.parseInt(s);
                if (quality >= 0 && quality <= 100)
                {
                    return quality / 100.f;
                }
            } catch (IllegalArgumentException e)
            {}
            System.out.println(
                MessageFormat.format(
                    rb.getString("ignoreQuality"),
                    new Object[] { s }));
        }
        return .75f;
    }

    private static double toEncodingRate(String s)
    {
        if (s != null)
        {
            try
            {
                double rate = Double.parseDouble(s);
                if (rate > 0)
                {
                    return rate;
                }
            } catch (IllegalArgumentException e)
            {}
            System.out.println(
                MessageFormat.format(
                    rb.getString("ignoreRate"),
                    new Object[] { s }));
        }
        return 1.;
    }
}
