import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIODemo {

    public  static RandomAccessFile randomAccessFile = null;
    public static void readFromFile(String path,String modal) throws Exception {
        //获取文件对象
        randomAccessFile = new RandomAccessFile(path,modal);
        //由文件对象 randomAccessFile 创建管道 channel 对象
        FileChannel fileChannel = randomAccessFile.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        int byteRead = fileChannel.read(byteBuffer);
        while(byteRead != -1 ){
            byteBuffer.flip();
            while(byteBuffer.hasRemaining()) {
                System.out.print((char) byteBuffer.get());
            }
            break;
//            byteBuffer.clear();
//            byteRead = fileChannel.read(byteBuffer);
        }
    }
    public static void readFromArray(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.put("ABC".getBytes());
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()){

            System.out.println((char)byteBuffer.get());

        }
    }

    public static void main(String[] args)throws Exception {
        NIODemo.readFromFile("C:\\Users\\hasee\\Desktop\\TestFile\\NIO_t1.txt","rw");
//        NIODemo.readFromArray();
    }
}
