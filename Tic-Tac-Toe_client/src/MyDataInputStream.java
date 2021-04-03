import java.io.*;

public class MyDataInputStream extends DataInputStream {
    public MyDataInputStream(InputStream in) {
        super(in);
    }

    private byte[] bytearr = new byte[1];
    private char[] chararr = new char[1];

    public final String myReadUTF() throws IOException {
        return myReadUTF(this);
    }


    public static String myReadUTF(DataInput in) throws IOException {
        int utflen = in.readUnsignedShort(); //Считываем длину принимаемого сообщения;

        if (utflen > 50) utflen = 50; //Ограничиваем длину принимаемого соообщения;

        byte[] bytearr;
        char[] chararr;
        if (in instanceof MyDataInputStream) {
            MyDataInputStream dis = (MyDataInputStream)in;
            if (dis.bytearr.length < utflen){
                dis.bytearr = new byte[utflen*2];
                dis.chararr = new char[utflen*2];
            }
            chararr = dis.chararr;
            bytearr = dis.bytearr;
        } else {
            bytearr = new byte[utflen];
            chararr = new char[utflen];
        }

        int c, char2, char3;
        int count = 0;
        int chararr_count=0;

        in.readFully(bytearr, 0, utflen);

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127) break;
            count++;
            chararr[chararr_count++]=(char)c;
        }

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            /* 0xxxxxxx*/
            /* 110x xxxx   10xx xxxx*/
            /* 1110 xxxx  10xx xxxx  10xx xxxx */
            /* 10xx xxxx,  1111 xxxx */
            switch (c >> 4) {
                case 0, 1, 2, 3, 4, 5, 6, 7 -> {
                    count++;
                    chararr[chararr_count++] = (char) c;
                }
                case 12, 13 -> {
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    char2 = bytearr[count - 1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                                "malformed input around byte " + count);
                    chararr[chararr_count++] = (char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                }
                case 14 -> {
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    char2 = bytearr[count - 2];
                    char3 = bytearr[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                                "malformed input around byte " + (count - 1));
                    chararr[chararr_count++] = (char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            ((char3 & 0x3F)));
                }
                default -> throw new UTFDataFormatException(
                        "malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }
}
