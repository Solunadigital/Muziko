/*
 * Modified By Romulus U. Ts'ai
 * On Oct 6, 2008
 * 
 * Removed all Debug executions
 * 
 */

package com.muziko.tageditor.myid3;

import android.os.Environment;

import com.crashlytics.android.Crashlytics;
import com.muziko.tageditor.common.ID3FrameType;
import com.muziko.tageditor.common.ID3WriteException;
import com.muziko.tageditor.common.ID3v1Genre;
import com.muziko.tageditor.metadata.ImageData;
import com.muziko.tageditor.metadata.MusicMetadata;
import com.muziko.tageditor.metadata.MusicMetadataSet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Vector;

import static com.muziko.tageditor.metadata.MusicMetadataConstants.KEY_COMMENT;
import static com.muziko.tageditor.metadata.MusicMetadataConstants.KEY_DURATION_SECONDS;
import static com.muziko.tageditor.metadata.MusicMetadataConstants.KEY_GENRE;
import static com.muziko.tageditor.metadata.MusicMetadataConstants.KEY_GENRE_ID;
import static com.muziko.tageditor.metadata.MusicMetadataConstants.KEY_PICTURES;
import static com.muziko.tageditor.metadata.MusicMetadataConstants.KEY_TRACK_COUNT;
import static com.muziko.tageditor.metadata.MusicMetadataConstants.KEY_TRACK_NUMBER;
import static com.muziko.tageditor.myid3.MyID3v2Constants.CHAR_ENCODING_CODE_ISO_8859_1;
import static com.muziko.tageditor.myid3.MyID3v2Constants.CHAR_ENCODING_CODE_UTF_16_WITH_BOM;
import static com.muziko.tageditor.myid3.MyID3v2Constants.CHAR_ENCODING_ISO;
import static com.muziko.tageditor.myid3.MyID3v2Constants.CHAR_ENCODING_UTF_16;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v23_COMPRESSION;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v23_ENCRYPTION;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v23_FILE_ALTER_PRESERVATION;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v23_GROUPING_IDENTITY;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v23_READ_ONLY;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v23_TAG_ALTER_PRESERVATION;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v24_COMPRESSION;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v24_DATA_LENGTH_INDICATOR;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v24_ENCRYPTION;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v24_FILE_ALTER_PRESERVATION;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v24_GROUPING_IDENTITY;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v24_READ_ONLY;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v24_TAG_ALTER_PRESERVATION;
import static com.muziko.tageditor.myid3.MyID3v2Constants.FRAME_FLAG_ID3v24_UNSYNCHRONISATION;
import static com.muziko.tageditor.myid3.MyID3v2Constants.HEADER_FLAG_ID3v24_FOOTER_PRESENT;
import static com.muziko.tageditor.myid3.MyID3v2Write.FRAME_SORTER;


public class MyID3 {
    private static final int ID3v2_HEADER_LENGTH = 10;
    private boolean skipId3v1 = false;
    private boolean skipId3v2 = false;
    private boolean skipId3v2Head = false;
    private boolean skipId3v2Tail = false;

    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p/>
     *
     * @param file   File to read non-metadata (ie. song data) from. Will be
     *               overwritten with new mp3 file.
     * @param set    MusicMetadataSet, usually read from mp3 file.
     * @param values MusicMetadata, a specific group of values to write.
     * @see MusicMetadataSet , MusicMetadata
     */
    public File update(File file, MusicMetadataSet set, MusicMetadata values, boolean sdcard) {
        File temp = null;
        try {
            String prefix = file.getName();
            String suffix = ".tmp";
            File directory = file.getParentFile();

            if (!sdcard) {
                temp = new File(directory
                        + "/" + prefix + suffix);
            } else {
                temp = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                        + "/" + prefix + suffix);
            }
//			temp = File.createTempFile(prefix, suffix,directory);

            write(file, temp, set, values);
            temp.setLastModified(file.lastModified());
            if (!sdcard) {
                file.delete();
                temp.renameTo(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return temp;
        }
    }

    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p/>
     *
     * @param file     File to read non-metadata (ie. song data) from. Will be
     *                 overwritten with new mp3 file.
     * @param set      MusicMetadataSet, usually read from mp3 file.
     * @param values   MusicMetadata, a specific group of values to write.
     * @param filter   MyID3v2Write.Filter, can be used to prevent ID3v2 frames from
     *                 written on a case-by-case basis.
     * @param listener MyID3Listener, observer of the write process.
     * @see MusicMetadataSet , MusicMetadata, MyID3Listener, MyID3v2Write.Filter
     */
    public void update(File file, MusicMetadataSet set, MusicMetadata values,
                       MyID3v2Write.Filter filter, MyID3Listener listener)
            throws IOException {

        File temp = null;
        try {
            temp = File.createTempFile(file.getName(), ".tmp", file
                    .getParentFile());

            write(file, temp, set, values, filter, listener);
            temp.setLastModified(file.lastModified());
            file.delete();
            temp.renameTo(file);
        } catch (Exception e) {
            if (temp != null && temp.exists() && file.exists())
                temp.delete();
            throw e;
        }
    }

    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p/>
     *
     * @param src    File to read non-metadata (ie. song data) from.
     * @param dst    File to overwrite with new mp3 file.
     * @param set    MusicMetadataSet, usually read from mp3 file.
     * @param values MusicMetadata, a specific group of values to write.
     * @see MusicMetadataSet , MusicMetadata
     */
    public void write(File src, File dst, MusicMetadataSet set,
                      MusicMetadata values) {
        write(src, dst, set, values, null, null);
    }

    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p/>
     *
     * @param src      File to read non-metadata (ie. song data) from.
     * @param dst      File to overwrite with new mp3 file.
     * @param set      MusicMetadataSet, usually read from mp3 file.
     * @param values   MusicMetadata, a specific group of values to write.
     * @param listener MyID3Listener, observer of the write process.
     * @see MusicMetadataSet , MusicMetadata, MyID3Listener
     */
    public void write(File src, File dst, MusicMetadataSet set,
                      MusicMetadata values, MyID3Listener listener) {
        write(src, dst, set, values, null, listener);
    }

    /**
     * Write MP3 file with specific metadata, drawing song data from an existing
     * mp3 file.
     * <p/>
     *
     * @param src      File to read non-metadata (ie. song data) from.
     * @param dst      File to overwrite with new mp3 file.
     * @param set      MusicMetadataSet, usually read from mp3 file.
     * @param values   MusicMetadata, a specific group of values to write.
     * @param filter   MyID3v2Write.Filter, can be used to prevent ID3v2 frames from
     *                 written on a case-by-case basis.
     * @param listener MyID3Listener, observer of the write process.
     * @see MusicMetadataSet , MusicMetadata, MyID3Listener, MyID3v2Write.Filter
     */
    public void write(File src, File dst, MusicMetadataSet set,
                      MusicMetadata values, MyID3v2Write.Filter filter,
                      MyID3Listener listener) {
        if (values == null)


            if (listener != null)
                listener.log();

        byte id3v1Tag[] = new byte[0];
        try {
            id3v1Tag = new MyID3v1().toTag(values);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (listener != null)
            listener.log("writing id3v1Tag", id3v1Tag == null ? "null" : ""
                    + id3v1Tag.length);


        try {
            byte id3v2TailTag[] = toTag(filter, set, values);
            listener.log("writing id3v2TailTag", id3v2TailTag == null ? "null"
                    : "" + id3v2TailTag.length);

            try {
                write(src, dst, id3v1Tag, id3v2TailTag, id3v2TailTag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (listener != null)


            if (listener != null)
                listener.log();
    }

    private void checkTags(MusicMetadataSet set, Vector frames)
            throws IOException {
        if (set.id3v2Raw == null)
            return;

        Vector old_frames = set.id3v2Raw.frames;
        if (old_frames == null)
            return;

        Vector new_frame_ids = new Vector();
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = (Frame) frames.get(i);

            new_frame_ids.add(frame.longFrameID);
        }

        Vector final_frame_ids = new Vector(new_frame_ids);
        for (int i = 0; i < old_frames.size(); i++) {
            MyID3v2Frame old_frame = (MyID3v2Frame) old_frames.get(i);

            String longFrameID;
            Number frame_order;
            {
                ID3FrameType frame_type = ID3FrameType.get(old_frame.frame_id);
                if (frame_type != null) {
                    longFrameID = frame_type.long_id;
                    frame_order = frame_type.getFrameOrder();
                } else if (old_frame.frame_id.length() == 4) {
                    longFrameID = old_frame.frame_id;
                    frame_order = ID3FrameType.DEFAULT_FRAME_ORDER;
                } else {


                    continue;
                }
            }

            if (new_frame_ids.contains(longFrameID))
                continue;


            if (old_frame instanceof MyID3v2FrameText) {
                MyID3v2FrameText text_frame = (MyID3v2FrameText) old_frame;

                // ID3FrameType frame_type =
                // ID3FrameType.get(old_frame.frame_id);


                Frame frame = toFrame(longFrameID, frame_order,
                        text_frame.value, text_frame.value2);

                if (frame != null) {
                    frames.add(frame);
                    final_frame_ids.add(frame.longFrameID);
                } else {


                }
            } else if (old_frame instanceof MyID3v2FrameImage) {
                MyID3v2FrameImage imageFrame = (MyID3v2FrameImage) old_frame;


                Frame frame = toFrameImage(longFrameID, frame_order, imageFrame
                        .getImageData());

                frames.add(frame);
                final_frame_ids.add(frame.longFrameID);
            } else {
                MyID3v2FrameData data = (MyID3v2FrameData) old_frame;
                if (data.flags.getTagAlterPreservation())
                    continue;
                // if(data.flags.getTagAlterPreservation())
                // continue;
                if (data.frame_id.length() == 4) {
                    // if(data.flags.getCompression() ||
                    // data.flags.getUnsynchronisation() || )
                    // int flags = data.flags.flags;
                    Frame frame = new Frame(data.frame_id, data.data_bytes,
                            data.flags);
                    frames.add(frame);
                    final_frame_ids.add(frame.longFrameID);
                }


            }
            // if()
        }

    }

    private byte[] writeFrames(MyID3v2Write.Filter filter, Vector v)
            throws ID3WriteException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Collections.sort(v, FRAME_SORTER);

        for (int i = 0; i < v.size(); i++) {
            Frame frame = (Frame) v.get(i);

            String frame_id = frame.longFrameID;
            if (frame_id.length() != 4)
                throw new ID3WriteException("frame_id has bad length: "
                        + frame_id + " (" + frame_id.length() + ")");

            if (filter != null && filter.filter(frame_id)) {
                continue;
            }

            // baos.write(frame_id );
            baos.write((byte) frame_id.charAt(0));
            baos.write((byte) frame_id.charAt(1));
            baos.write((byte) frame_id.charAt(2));
            baos.write((byte) frame_id.charAt(3));

            int length = frame.bytes.length;

            if (3 == 4) {
                baos.write((byte) (0x7f & (length >> 21)));
                baos.write((byte) (0x7f & (length >> 14)));
                baos.write((byte) (0x7f & (length >> 7)));
                baos.write((byte) (0x7f & (length)));
            } else if (3 == 3) {
                baos.write((byte) (0xff & (length >> 24)));
                baos.write((byte) (0xff & (length >> 16)));
                baos.write((byte) (0xff & (length >> 8)));
                baos.write((byte) (0xff & (length)));
            } else
                throw new ID3WriteException("id3v2_version: " + 3);

            // int flags = frame.flags;
            int flags = 0;
            if (3 == 4) {
                if (frame.flags.getTagAlterPreservation())
                    flags |= FRAME_FLAG_ID3v24_TAG_ALTER_PRESERVATION;
                if (frame.flags.getFileAlterPreservation())
                    flags |= FRAME_FLAG_ID3v24_FILE_ALTER_PRESERVATION;
                if (frame.flags.getReadOnly())
                    flags |= FRAME_FLAG_ID3v24_READ_ONLY;
                if (frame.flags.getGroupingIdentity())
                    flags |= FRAME_FLAG_ID3v24_GROUPING_IDENTITY;
                if (frame.flags.getCompression())
                    flags |= FRAME_FLAG_ID3v24_COMPRESSION;
                if (frame.flags.getEncryption())
                    flags |= FRAME_FLAG_ID3v24_ENCRYPTION;
                if (frame.flags.getUnsynchronisation())
                    flags |= FRAME_FLAG_ID3v24_UNSYNCHRONISATION;
                if (frame.flags.getDataLengthIndicator())
                    flags |= FRAME_FLAG_ID3v24_DATA_LENGTH_INDICATOR;
            } else if (3 == 3) {
                if (frame.flags.getTagAlterPreservation())
                    flags |= FRAME_FLAG_ID3v23_TAG_ALTER_PRESERVATION;
                if (frame.flags.getFileAlterPreservation())
                    flags |= FRAME_FLAG_ID3v23_FILE_ALTER_PRESERVATION;
                if (frame.flags.getReadOnly())
                    flags |= FRAME_FLAG_ID3v23_READ_ONLY;
                if (frame.flags.getGroupingIdentity())
                    flags |= FRAME_FLAG_ID3v23_GROUPING_IDENTITY;
                if (frame.flags.getCompression())
                    flags |= FRAME_FLAG_ID3v23_COMPRESSION;
                if (frame.flags.getEncryption())
                    flags |= FRAME_FLAG_ID3v23_ENCRYPTION;
            } else
                throw new ID3WriteException("id3v2_version: " + 3);

            baos.write((byte) (0xff & (flags >> 8)));
            baos.write((byte) (0xff & (flags)));

            baos.write(frame.bytes);
        }

        return baos.toByteArray();
    }

    private void writeSynchSafeInt(byte bytes[], int start, int value)
            throws ID3WriteException {
        bytes[start + 3] = (byte) (value & 0x7f);
        value >>= 7;
        bytes[start + 2] = (byte) (value & 0x7f);
        value >>= 7;
        bytes[start + 1] = (byte) (value & 0x7f);
        value >>= 7;
        bytes[start + 0] = (byte) (value & 0x7f);

        value >>= 7;
        if (value != 0)
            throw new ID3WriteException("Value to large for synch safe int: "
                    + value);
    }

    public byte[] getHeaderFooter(int body_length, boolean is_footer)
            throws ID3WriteException {
        byte result[] = new byte[10];

        int index = 0;
        if (is_footer) {
            result[index++] = 0x33; // 3
            result[index++] = 0x44; // D
            result[index++] = 0x49; // I
        } else {
            result[index++] = 0x49; // I
            result[index++] = 0x44; // D
            result[index++] = 0x33; // 3
        }

        if (3 == 4)
            result[index++] = 0x04; // version
        else if (3 == 3)
            result[index++] = 0x03; // version
        else
            throw new ID3WriteException("id3v2_version: " + 3);

        result[index++] = 0x00;

        int flags = 0; // charles
        if (3 == 4)
            flags |= HEADER_FLAG_ID3v24_FOOTER_PRESENT;
        else if (3 == 3) {
        } else
            throw new ID3WriteException("id3v2_version: " + 3);

        result[index++] = (byte) flags;

        writeSynchSafeInt(result, index, body_length);

        return result;
    }

    public byte[] toTag(MyID3v2Write.Filter filter, MusicMetadataSet set,
                        MusicMetadata values) {
        values = new MusicMetadata(values);


        Vector frames = null;
        try {
            frames = toFrames(values);
            checkTags(set, frames);


            byte frame_bytes[] = writeFrames(filter, frames);


            byte extended_header[] = {};
            byte padding[] = {};

            int body_length = extended_header.length + frame_bytes.length
                    + padding.length;

            byte header[] = getHeaderFooter(body_length, false);


            byte footer[];
            if (3 == 4)
                footer = getHeaderFooter(body_length, true);
            else if (3 == 3)
                footer = null;
            else
                throw new ID3WriteException("id3v2_version: " + 3);


            int resultLength = header.length + extended_header.length
                    + frame_bytes.length + padding.length;
            if (footer != null)
                resultLength += footer.length;
            byte result[] = new byte[resultLength];

            int index = 0;
            System.arraycopy(header, 0, result, index, header.length);
            index += header.length;
            System.arraycopy(frame_bytes, 0, result, index, frame_bytes.length);
            if (footer != null) {
                index += frame_bytes.length;
                System.arraycopy(footer, 0, result, index, footer.length);
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }


    }

    private static class Frame {
        public final String longFrameID;
        public final Number frame_order;
        public final byte bytes[];
        public final ID3v2FrameFlags flags;

        public Frame(String longFrameID, byte[] bytes) {
            this(longFrameID, ID3FrameType.DEFAULT_FRAME_ORDER, bytes,
                    new ID3v2FrameFlags());
        }

        public Frame(String longFrameID, byte[] bytes,
                     final ID3v2FrameFlags flags) {
            this(longFrameID, ID3FrameType.DEFAULT_FRAME_ORDER, bytes, flags);
        }

        public Frame(String longFrameID, Number frame_order, byte[] bytes) {
            this(longFrameID, frame_order, bytes, new ID3v2FrameFlags());
        }

        public Frame(String longFrameID, Number frame_order, byte[] bytes,
                     final ID3v2FrameFlags flags) {
            this.longFrameID = longFrameID;
            this.frame_order = frame_order;
            // this.frame_type = frame_type;
            this.bytes = bytes;
            this.flags = flags;
        }

        public String toString() {
            return "[frame: " + longFrameID + ": " + bytes.length + "]";
        }
    }

    private boolean canEncodeStringInISO(String s) {
        byte bytes[] = new byte[0];
        try {
            bytes = s.getBytes(CHAR_ENCODING_ISO);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String check1 = null;
        try {
            check1 = new String(bytes, CHAR_ENCODING_ISO);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return check1.equals(s);
    }

    private byte[] encodeString(String s, boolean use_iso)
             {
        if (use_iso)
            try {
                return s.getBytes(CHAR_ENCODING_ISO);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        else {
            byte bytes[] = new byte[0];
            try {
                bytes = s.getBytes(CHAR_ENCODING_UTF_16);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

            // Windows Media Player can't handle UTF-16, big-endian.
            // switch to UTF-16, little-endian.
            if (((0xff & bytes[0]) == 0xFE) && ((0xff & bytes[1]) == 0xFF)) {
                // manually switch UTF 16 byte order
                for (int i = 0; i < bytes.length; i += 2) {
                    byte temp = bytes[i];
                    bytes[i] = bytes[i + 1];
                    bytes[i + 1] = temp;
                }
            }
            return bytes;
        }
    }

    private Frame toFrameText(String longFrameID, Number frame_order,
                              Object value1, Object value2) {
        String s1;
        if (value1 instanceof String)
            s1 = (String) value1;
        else if (value1 instanceof Number)
            s1 = value1.toString();
        else {


            return null;
        }
        String s2 = null;
        if (value2 instanceof String)
            s2 = (String) value2;
        else if (value2 instanceof Number)
            s2 = value2.toString();

        boolean use_iso = canEncodeStringInISO(s1);
        if (s2 != null)
            use_iso &= canEncodeStringInISO(s2);

        int char_encoding_code = use_iso ? CHAR_ENCODING_CODE_ISO_8859_1
                : CHAR_ENCODING_CODE_UTF_16_WITH_BOM;
        // : CHAR_ENCODING_CODE_UTF_8;

        byte string_1_bytes[] = encodeString(s1, use_iso);
        byte string_2_bytes[] = null;
        if (s2 != null)
            string_2_bytes = encodeString(s2, use_iso);


        // int frame_length = kFRAME_HEADER_LENGTH + string_bytes.length + 1;
        int result_length = string_1_bytes.length + 1;
        if (string_2_bytes != null)
            result_length += string_2_bytes.length + 1;
        byte result[] = new byte[result_length];
        int index = 0;

        result[index++] = (byte) char_encoding_code;
        System.arraycopy(string_1_bytes, 0, result, index,
                string_1_bytes.length);
        index += string_1_bytes.length;

        if (string_2_bytes != null) {
            result[index++] = (byte) char_encoding_code;
            System.arraycopy(string_2_bytes, 0, result, index,
                    string_2_bytes.length);
        }

        return new Frame(longFrameID, frame_order, result);
    }

    private Frame toFrameImage(String longFrameID, Number frame_order,
                               ImageData imageData)
        // byte imageData[], String mimeType, String description,
        // int pictureType)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        boolean use_iso = canEncodeStringInISO(imageData.description);

        int char_encoding_code = use_iso ? CHAR_ENCODING_CODE_ISO_8859_1
                : CHAR_ENCODING_CODE_UTF_16_WITH_BOM;

        baos.write(char_encoding_code);

        byte mimeTypeBytes[] = encodeString(imageData.mimeType, true);


        baos.write(mimeTypeBytes);
        baos.write(0);

        baos.write(0xff & imageData.pictureType);

        byte descriptionBytes[] = encodeString(imageData.description, use_iso);
        baos.write(descriptionBytes);


        baos.write(0);

        baos.write(imageData.imageData);

        byte frameBytes[] = baos.toByteArray();


        return new Frame(longFrameID, frame_order, frameBytes);
    }

    private Frame toFrameCOMM(String longFrameID, Number frame_order,
                              Object value) {
        String s;
        if (value instanceof String)
            s = (String) value;
        else {
            return null;
        }

        boolean use_iso = canEncodeStringInISO(s);

        int char_encoding_code = use_iso ? CHAR_ENCODING_CODE_ISO_8859_1
                : CHAR_ENCODING_CODE_UTF_16_WITH_BOM;
        // : CHAR_ENCODING_CODE_UTF_8;

        byte string_bytes[] = encodeString(s, use_iso);

        // int frame_length = kFRAME_HEADER_LENGTH + string_bytes.length + 1;
        int result_length = string_bytes.length + 1 + 3 + 1;

        byte result[] = new byte[result_length];
        int index = 0;

        result[index++] = (byte) char_encoding_code;
        result[index++] = (byte) 0; // language
        result[index++] = (byte) 0; // language
        result[index++] = (byte) 0; // language

        // summary
        result[index++] = (byte) 0; // divider

        System.arraycopy(string_bytes, 0, result, index, string_bytes.length);

        return new Frame(longFrameID, frame_order, result);
    }


    private Frame toFrame(String longFrameID, Number frame_order,
                          Object value1, Object value2) {
        if (longFrameID.startsWith("T")) {
            return toFrameText(longFrameID, frame_order, value1, value2);
        } else if (longFrameID.equals("COMM")) {
            return toFrameCOMM(longFrameID, frame_order, value1);
        } else {
            // TODO: should we throw an exception here?


            return null;
        }
    }

    private Frame toFrame(ID3FrameType frame_type, Object value1) {
        return toFrame(frame_type.long_id, frame_type.getFrameOrder(), value1,
                null);
    }

    private Vector toFrames(MusicMetadata values) {
        Vector result = new Vector();

        {
            Object track_count = values.get(KEY_TRACK_COUNT);
            Object track_number = values.get(KEY_TRACK_NUMBER);

            if (track_count != null || track_number != null) {
                String value = "";
                if (track_number != null)
                    value += track_number.toString();
                if (track_count != null) {
                    value += "/";
                    value += track_count.toString();
                }

                result.add(toFrame(new ID3FrameType("TRK", "TRCK",
                        "TRCK", "Track Number/Position In Set", 4), value));
            }

            values.remove(KEY_TRACK_COUNT); // charles

// charles
            values.remove(KEY_TRACK_NUMBER);
        }
        {
            String genreString = "";
            String genreIDString = null;
            Object value = values.get(KEY_GENRE);
            if (value != null) {
                genreString = value.toString();
                Number id = ID3v1Genre.get(genreString);
                if (null != id) {
                    genreIDString = id.toString();
                    genreString = "(" + id + ")" + genreString;
                }
            } else {
                value = values.get(KEY_GENRE_ID);
                if (value != null) {
                    if (genreIDString == null
                            || !genreIDString.equals(value.toString()))
                        genreString = "(" + genreIDString + ")" + genreString;
                }
            }

            if (genreString.length() > 0) {
                result.add(toFrame(ID3FrameType.CONTENTTYPE, genreString));
            }

            values.remove(KEY_GENRE); // charles
            values.remove(KEY_GENRE_ID); // charles
        }
        {
            Object value = values.get(KEY_DURATION_SECONDS);


            if (value != null) {
                Number number = (Number) value;
                number = (long) (number.intValue() * 1000);
                result.add(toFrame(ID3FrameType.SONGLEN, number.toString()));
            }

            values.remove(KEY_DURATION_SECONDS); // charles
        }

        {
            Object value = values.get(KEY_COMMENT);

            if (value != null) {
                Frame frame = null;
                frame = toFrame(ID3FrameType.COMMENT, value.toString());

                if (frame != null)
                    result.add(frame);
                else {


                }

            }

            values.remove(KEY_COMMENT); // charles
        }

        Vector keys = new Vector(values.keySet());
        for (int i = 0; i < keys.size(); i++) {
            Object key = keys.get(i);
            Object value = values.get(key);

            if (key.equals(KEY_PICTURES)) {
                Vector images = (Vector) value;
                for (int j = 0; j < images.size(); j++) {
                    ImageData imageData = (ImageData) images.get(j);
                    String longFrameID = ID3FrameType.PICTURE.long_id;
                    Number frame_order = ID3FrameType.PICTURE.getFrameOrder();
                    Frame frame = null;
                    try {
                        frame = toFrameImage(longFrameID, frame_order,
                                imageData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    result.add(frame);
                }
                // return ID3FrameType.PICTURE;
                continue;
            }

            Frame frame = null;
            try {
                frame = toFrameKey(key, value);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (frame != null)
                result.add(frame);
            else {


            }
        }

        return result;
    }

    private Frame toFrameKey(Object key, Object value)
            throws IOException {
        return toFrameKey(key, value, null);
    }

    private Frame toFrameKey(Object key, Object value1, Object value2)
            throws IOException {
        ID3FrameType frame_type = new ID3v2DataMapping().getID3FrameType(key);


        if (frame_type == null) {


            return null;
        }

        return toFrame(frame_type, value1, value2);
    }

    private Frame toFrame(ID3FrameType frame_type, Object value1, Object value2)
            throws IOException {
        return toFrame(frame_type.long_id, frame_type.getFrameOrder(), value1,
                value2);
    }

    /**
     * Removes all ID3v1 and ID3v2 tags from an mp3 file.
     * <p/>
     *
     * @param src File to read non-metadata (ie. song data) from.
     * @param dst File to overwrite with new mp3 file.
     */
    public void removeTags(File src, File dst)
            throws IOException, ID3WriteException {
        byte id3v1Tag[] = null;
        byte id3v2HeadTag[] = null;
        byte id3v2TailTag[] = null;

        write(src, dst, id3v1Tag, id3v2HeadTag, id3v2TailTag);
    }

    /**
     * Removes all ID3v1 and ID3v2 tags from an mp3 file.
     * <p/>
     *
     * @param src File to read non-metadata (ie. song data) from.
     * @param dst File to overwrite with new mp3 file.
     */
    public void rewriteTags(File src, File dst)
            throws IOException, ID3WriteException {
        byte id3v1Tag[] = null;
        ID3Tag tag = readID3v1(src);
        if (null != tag)
            id3v1Tag = tag.bytes;

        byte id3v2HeadTag[] = readID3v2Head(src);

        boolean hasId3v1 = id3v1Tag != null;
        byte id3v2TailTag[] = readID3v2Tail(src, hasId3v1);

        write(src, dst, id3v1Tag, id3v2HeadTag, id3v2TailTag);
    }

    /**
     * Configures the library to not write ID3v1 tags.
     */
    public void setSkipId3v1() {
        skipId3v1 = true;
    }

    /**
     * Configures the library to not write ID3v2 tags.
     */
    public void setSkipId3v2() {
        skipId3v2 = true;
    }

    /**
     * Configures the library to not write ID3v2 head tags.
     */
    public void setSkipId3v2Head() {
        skipId3v2Head = true;
    }

    /**
     * Configures the library to not write ID3v2 tail tags.
     */
    public void setSkipId3v2Tail() {
        skipId3v2Tail = true;
    }

    private void write(File src, File dst, byte id3v1Tag[],
                       byte id3v2HeadTag[], byte id3v2TailTag[]) throws IOException {
        if (src == null || !src.exists())


            if (!src.getName().toLowerCase().endsWith(".mp3"))


                if (dst == null)


                    if (dst.exists()) {
                        dst.delete();

                    }

        boolean hasId3v1 = doeshasID3v1(src);

//		long id3v1Length = 128;

        long id3v1Length = hasId3v1 ? 128 : 0;
        long id3v2HeadLength = findID3v2HeadLength(src);
        long id3v2TailLength = findID3v2TailLength(src, hasId3v1);

        OutputStream os = null;
        InputStream is = null;
        try {
            dst.getParentFile().mkdirs();
            os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);

            if (!skipId3v2Head && !skipId3v2 && id3v2HeadTag != null)
                os.write(id3v2HeadTag);

            is = new FileInputStream(src);
            is = new BufferedInputStream(is, 8192);

            is.skip(id3v2HeadLength);

            long total_to_read = src.length();
            total_to_read -= id3v1Length;
            total_to_read -= id3v2HeadLength;
            total_to_read -= id3v2TailLength;

            byte buffer[] = new byte[1024];
            long total_read = 0;
            while (total_read < total_to_read) {
                int remainder = (int) (total_to_read - total_read);
                int readSize = Math.min(buffer.length, remainder);
                int read = is.read(buffer, 0, readSize);
                if (read <= 0)
                    throw new IOException("unexpected EOF");

                os.write(buffer, 0, read);
                total_read += read;
            }

            if (!skipId3v2Tail && !skipId3v2 && id3v2TailTag != null)
                os.write(id3v2TailTag);
            if (!skipId3v1 && id3v1Tag != null)
                os.write(id3v1Tag);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Throwable e) {
                Crashlytics.logException(e);
            }
            try {
                if (os != null)
                    os.close();
            } catch (Throwable e) {
                Crashlytics.logException(e);
            }
        }
    }

    private byte[] readArray(InputStream is, int length)
            throws IOException {
        byte result[] = new byte[length];
        int total = 0;
        while (total < length) {
            int read = is.read(result, total, length - total);
            if (read < 0)
                throw new IOException("bad read");
            total += read;
        }
        return result;
    }

    /**
     * Reads all metadata (ID3v1 & ID3v2) from MP3 file.
     * <p/>
     *
     * @param file File to read metadata (ie. song data) from.
     * @return MusicMetadataSet, a set of MusicMetadata value collections.
     * @see MusicMetadataSet , MusicMetadata
     */
    public MusicMetadataSet read(File file) throws IOException {
        return read(file, null);
    }

    /**
     * Reads all metadata (ID3v1 & ID3v2) from MP3 file.
     * <p/>
     *
     * @param file     File to read metadata (ie. song data) from.
     * @param listener MyID3Listener, an observer.
     * @return MusicMetadataSet, a set of MusicMetadata value collections.
     * @see MusicMetadataSet , MusicMetadata
     */
    public MusicMetadataSet read(File file, MyID3Listener listener)
            throws IOException {
        try {
            if (file == null || !file.exists())
                return null;

            if (!file.getName().toLowerCase().endsWith(".mp3"))
                return null;

            ID3Tag id3v1 = readID3v1(listener, file);
            ID3Tag.V2 id3v2 = null;
            try {
                id3v2 = readID3v2(listener, file, id3v1 != null);
            } catch (Error | IOException e) {
                Crashlytics.logException(e);
            }

            MusicMetadataSet result = MusicMetadataSet.factoryMethod(id3v1,
                    id3v2, file.getName(), file.getParentFile().getName());

            return result;
        } catch (Error | IOException e) {

            Crashlytics.logException(e);
            return null;
        }
    }

    private ID3Tag readID3v1(File file) throws IOException {
        return readID3v1(null, file);
    }

    private ID3Tag readID3v1(MyID3Listener listener, File file)
            throws IOException {
        if (file == null || !file.exists())
            return null;

        long length = file.length();

        if (length < 128)
            return null;

        byte bytes[];
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            is.skip(length - 128);

            bytes = readArray(is, 128);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }

        if (bytes[0] != 'T')
            return null;
        if (bytes[1] != 'A')
            return null;
        if (bytes[2] != 'G')
            return null;

        if (null != listener)
            listener.log("ID3v1 tag found.");

        MyID3v1 id3v1 = new MyID3v1();
        MusicMetadata tags = id3v1.parseTags(listener, bytes);

        return new ID3Tag.V1(bytes, tags);
    }

    private boolean hasID3v1(File file) throws IOException {
        if (file == null || !file.exists())
            return false;

        long length = file.length();

        if (length < 128)
            return false;

        byte bytes[];
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            is.skip(length - 128);

            bytes = readArray(is, 128);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }

        if (bytes[0] != 'T')
            return false;
        return bytes[1] == 'A' && bytes[2] == 'G';

    }

    private boolean doeshasID3v1(File file) throws IOException {
        if (file == null || !file.exists())
            return false;

        long length = file.length();

        if (length < 128)
            return false;

        byte bytes[];
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            is.skip(length - 128);

            bytes = readArray(is, 128);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }

        if (bytes[0] != 'T')
            return false;
        return bytes[1] == 'A' && bytes[2] == 'G';

    }

    private byte[] readID3v2Head(File file) throws IOException {

        if (file == null || !file.exists())
            return null;

        long length = file.length();

        if (length < ID3v2_HEADER_LENGTH)
            return null;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            byte header[];
            header = readArray(is, ID3v2_HEADER_LENGTH);

            if (header[0] != 0x49) // I
                return null;
            if (header[1] != 0x44) // D
                return null;
            if (header[2] != 0x33) // 3
                return null;

            int flags = header[5];
            boolean has_footer = (flags & (1 << 4)) > 0;

            Number tagLength = MyID3v2Read.readSynchsafeInt(header, 6);
            if (tagLength == null)
                return null;

            int bodyLength = tagLength.intValue();
            if (has_footer)
                bodyLength += ID3v2_HEADER_LENGTH;

            if (ID3v2_HEADER_LENGTH + bodyLength > length)
                return null;

            byte body[] = readArray(is, bodyLength);

            byte result[] = new byte[header.length + body.length];

            System.arraycopy(header, 0, result, 0, header.length);
            System.arraycopy(body, 0, result, header.length, body.length);

            return result;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
    }

    private long findID3v2HeadLength(File file) throws IOException {
        if (file == null || !file.exists())
            return 0;

        long length = file.length();

        if (length < ID3v2_HEADER_LENGTH)
            return 0;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            byte header[];
            header = readArray(is, ID3v2_HEADER_LENGTH);

            if (header[0] != 0x49) // I
                return 0;
            if (header[1] != 0x44) // D
                return 0;
            if (header[2] != 0x33) // 3
                return 0;

            int flags = header[5];
            boolean has_footer = (flags & (1 << 4)) > 0;

            Number tagLength = MyID3v2Read.readSynchsafeInt(header, 6);
            if (tagLength == null)
                return 0;

            int totalLength = ID3v2_HEADER_LENGTH + tagLength.intValue();
            if (has_footer)
                totalLength += ID3v2_HEADER_LENGTH;

            return totalLength;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
    }

    private int findID3v2TailLength(File file, boolean hasId3v1)
            throws IOException {
        if (file == null || !file.exists())
            return 0;

        long length = file.length();

        int index = hasId3v1 ? 128 : 0;
        index += ID3v2_HEADER_LENGTH;

        if (index > length)
            return 0;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            is.skip(length - index);

            byte footer[];
            footer = readArray(is, ID3v2_HEADER_LENGTH);

            if (footer[0] != 0x33) // 3
                return 0;
            if (footer[1] != 0x44) // D
                return 0;
            if (footer[2] != 0x49) // I
                return 0;

            Number tagLength = MyID3v2Read.readSynchsafeInt(footer, 6);
            if (tagLength == null)
                return 0;

            int totalLength = ID3v2_HEADER_LENGTH + ID3v2_HEADER_LENGTH
                    + tagLength.intValue();

            return totalLength;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
    }

    private byte[] readID3v2Tail(File file, boolean hasId3v1)
            throws IOException {
        if (file == null || !file.exists())
            return null;

        long length = file.length();

        int index = hasId3v1 ? 128 : 0;
        index += ID3v2_HEADER_LENGTH;

        if (index > length)
            return null;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            is.skip(length - index);

            byte footer[];
            footer = readArray(is, ID3v2_HEADER_LENGTH);

            if (footer[2] != 0x33) // 3
                return null;
            if (footer[1] != 0x44) // D
                return null;
            if (footer[0] != 0x49) // I
                return null;

            Number tagLength = MyID3v2Read.readSynchsafeInt(footer, 6);
            if (tagLength == null)
                return null;

            int bodyLength = tagLength.intValue();
            if (index + bodyLength > length)
                return null;

            is.close();
            is = null;

            is = new FileInputStream(file);
            is = new BufferedInputStream(is, 8192);

            long skip = length;
            skip -= ID3v2_HEADER_LENGTH;
            skip -= bodyLength;
            skip -= ID3v2_HEADER_LENGTH;
            if (hasId3v1)
                skip -= 128;
            is.skip(skip);

            byte header_and_body[] = readArray(is, ID3v2_HEADER_LENGTH
                    + bodyLength + ID3v2_HEADER_LENGTH);

            byte result[] = header_and_body;

            return result;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
    }

    private ID3Tag.V2 readID3v2(MyID3Listener listener, File file,
                                boolean hasId3v1) throws IOException {
        if (file == null || !file.exists())
            return null;

        byte bytes[] = null;
        bytes = readID3v2Tail(file, hasId3v1);
        if (bytes == null)
            bytes = readID3v2Head(file);

        if (bytes == null)
            return null;

        if (null != listener)
            listener.log("ID3v2 tag found: " + bytes.length + " bytes");

        MyID3v2Read parser = new MyID3v2Read(listener,
                new ByteArrayInputStream(bytes), false);
        while (!parser.isComplete()) {
            parser.iteration();
        }
        if (parser.isError()) {
            if (listener != null)
                listener.log("id3v2 error", parser.getErrorMessage());

            parser.dump();
            return null;
        }

        if (!parser.hasTags())
            return null;

        Vector tags = parser.getTags();

        MusicMetadata values = new ID3v2DataMapping().process(tags);

        byte version_major = parser.getVersionMajor();
        byte version_minor = parser.getVersionMinor();

        if (null != listener)
            listener.log();

        return new ID3Tag.V2(version_major, version_minor, bytes, values, tags);
    }

}
