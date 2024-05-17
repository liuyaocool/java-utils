package liuyao.utils;

import java.io.*;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.WatchEvent;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class IOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int CHANNEL_SIZE = 1024*1024*20;

    private static final Map<String, FileOutputStream> WRITE_MAP = new ConcurrentHashMap<>();

    public static WritableByteChannel getWriteChannel(String path) {
        FileOutputStream out = WRITE_MAP.get(path);
        if (null == out || !out.getChannel().isOpen()) {
            synchronized (WRITE_MAP) {
                if (null == (out = WRITE_MAP.get(path)) || !out.getChannel().isOpen()) {
                    if (null != out) close(out.getChannel(), out);
                    try {
                        WRITE_MAP.put(path, new FileOutputStream(path));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return WRITE_MAP.get(path).getChannel();
    }
    public static void closeWriteChannel(String path) {
        close(WRITE_MAP.get(path).getChannel(), WRITE_MAP.get(path));
    }

    /**
     * 写入内容到文件
     *  注意：需调用{@link IOUtils#closeWriteChannel(path)}手动关闭
     * @param path
     * @param content
     * @throws IOException
     */
    public static void writeToFile(String path, byte[] content) throws IOException {
        WritableByteChannel out = getWriteChannel(path);
        ByteBuffer buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        for (int i = 0; i < content.length; i+=DEFAULT_BUFFER_SIZE) {
            buf.put(content, i, Math.min(content.length - i, DEFAULT_BUFFER_SIZE));
            buf.flip();
            out.write(buf);
            buf.clear();
        }
    }

    public static long transferTo(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(in, "in");
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }

    public static long transferTo(ReadableByteChannel ch, OutputStream out) throws IOException {
        Objects.requireNonNull(ch, "ch");
        Objects.requireNonNull(out, "out");
        ByteBuffer buf = ByteBuffer.allocate(CHANNEL_SIZE);
        long transferred = 0;
        int read;
        while ((read = ch.read(buf)) > 0) {
            out.write(buf.array(), 0, read);
            buf.clear();
            transferred += read;
        }
        return transferred;
    }

    public static long transferTo(InputStream in, WritableByteChannel out) throws IOException {
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(in, "in");
        ByteBuffer buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long transferred = 0;
        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            buf.put(buffer, 0, read);
            buf.flip();
            out.write(buf);
            buf.clear();
            transferred += read;
        }
        return transferred;
    }

    public static long transferTo(ReadableByteChannel src, WritableByteChannel target) throws IOException {
        Objects.requireNonNull(src, "src");
        Objects.requireNonNull(target, "target");
        ByteBuffer buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        long transferred = 0;
        int read;
        while ((read = src.read(buf)) > 0) {
            buf.flip();
            target.write(buf);
            buf.clear();
            transferred += read;
        }
        return transferred;
    }

    public static void readTextFile(String path, Charset encoding, Consumer<String> callback) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            readTextFile(fis, encoding, callback);
        } finally {
            close(fis);
        }
    }

    public static void readTextFile(InputStream is, Consumer<String> callback) throws IOException {
        readTextFile(is, StandardCharsets.UTF_8, callback);
    }

    public static void readTextFile(InputStream is, Charset cs, Consumer<String> callback) throws IOException {
        BufferedReader br = null;
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(is, cs);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                callback.accept(line);
            }
        } finally {
            close(br, isr);
        }
    }

    public int findFreePort() throws IOException {
        ServerSocket serverSocket = null;
        try {
            // 这里传0将获得一个随机端口
            serverSocket = new ServerSocket(0);
            int localPort = serverSocket.getLocalPort();
            return localPort;
        } finally {
            close(serverSocket);
        }
    }

    public static void close(AutoCloseable... closes) {
        for (AutoCloseable c : closes) {
            try {
                if (null != c) c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
