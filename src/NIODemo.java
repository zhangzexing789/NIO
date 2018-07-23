import org.junit.Test;

import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class NIODemo {
    /**
     * 同步异步：强调的是消息通知机制，即消息返回通知的形式不同。同步就是线程进行函数调用，直至返回结果之前都不会做其他事情；异步就是线程进行函数调用会立即得到调用的返回，但不是结果，告诉
     *          线程可以先做其他事情，等到被调用者有结果了，会通过状态，通知或者毁掉函数来告诉线程可以处理结果了
     * 阻塞非阻塞：强调线程等待消息的状态。阻塞就是线程在得到消息结果返回之前会一直处于等待，被挂起的状态，不会进行后续的步骤；非阻塞就是线程在立即没有得到返回之前不会被阻塞，可以进行
     *           后续的其他步骤，再通过轮询的方式来查看返回结果
     * 同步异步　　　一定有得到消息的返回（不一定有结果）
     * 阻塞非阻塞　　不一定有消息的返回
     */
    /**
     * 1、面向缓冲（面向流）：读写数据均需要先进入缓冲区，待缓冲区处于待处理状态，才进行数据读写操作；数据在缓冲区可进行前后移动
     *                 （面向流意味着每次从流中读一个或多个字节，直至读取所有字节，它们没有被缓存在任何地方。此外，它不能前后移动流中的数据。）
     * 2、阻塞与非阻塞IO ：当线程在进行读取或者写入数据时，直至读取数据或者完全写入数据前，线程只能等待而不能其他操作，就是阻塞式；相反地，线程可进行
     *                  其他操作，就是非阻塞式。
     * 3、面向通道：  通道是双向的既可以写、也可以读；传统IO只能是单向的
     *
     */

    public void readFile(String path,String model) throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile(path,model);
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int byteRead = fileChannel.read(byteBuffer);
        while (byteRead != -1){
            byteBuffer.flip();
            System.out.println("byteRead:"+byteRead);
            while (byteBuffer.hasRemaining()){
                System.out.print((char) byteBuffer.get());
            }
            byteBuffer.clear();
            byteRead = fileChannel.read(byteBuffer);
        }
        randomAccessFile.close();
    }

    /**
     * 以下是Java NIO里关键的Buffer实现：
     *
     * ByteBuffer
     * CharBuffer
     * DoubleBuffer
     * FloatBuffer
     * IntBuffer
     * LongBuffer
     * ShortBuffer
     *
     * 关键属性
     * capacity    缓冲区大小
     * position    写数据时表示当前位置，并且会自动移至下一个可写位置（position < capacity）;读数据时，最开始会重置为0,并自动移至下一个可读位置（position < limit）
     * limit       写数据时 limit = capacity ,读数据时 limit  为写完数据的　position
     */
    public void bufferTest(){
        //设置缓冲区大小,pos = 0,limt = 1024,allocate = 1024
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //向缓冲区添加数据,pos 表示写入数据后在缓冲区的末位置
        byteBuffer.put("ByteBuffer test".getBytes());//pos = 15
        //将缓冲区切换成读模式,limt 表示缓冲区中数据量的大小,即是写入数据后的末坐标 pos
        byteBuffer.flip();//pos = 15,limt = 15
        //读数据，当pos<limt,return true
        while (byteBuffer.hasRemaining()) {
            //缓冲区拿数据
            System.out.print((char) byteBuffer.get());
        }
        //ByteBuffer test
        System.out.println();

        CharBuffer charBuffer = CharBuffer.allocate(100);
        charBuffer.put("CharBuffer test");
        charBuffer.append('d');//return put('d');
        charBuffer.flip();
        while (charBuffer.hasRemaining()){
            System.out.print(charBuffer.get());
        }
        //CharBuffer testd
    }
    /**
     * allocate() - 分配一块缓冲区　　
     * put() -  向缓冲区写数据
     * get() - 向缓冲区读数据　　
     * filp() - 将缓冲区从写模式切换到读模式　　
     * clear() - 从读模式切换到写模式，不会清空数据，但后续写数据会覆盖原来的数据，即使有部分数据没有读，也会被遗忘；　　
     * compact() - 从读数据切换到写模式，数据不会被清空，会将所有未读的数据copy到缓冲区头部，后续写数据不会覆盖，而是在这些数据之后写数据
     * mark() - 对position做出标记，配合reset使用
     * reset() - 将position置为标记值　
     * @throws Exception
     */
    public void markAndResetTest(){
        CharBuffer charBuffer = CharBuffer.allocate(100);
        charBuffer.put("markAndReset test");
        charBuffer.flip();
        while (charBuffer.hasRemaining()){
            char c = charBuffer.get();  //pos has pos++;
            if(c=='A'){
                charBuffer.mark();
            }
            System.out.print(c);
        }
        charBuffer.reset();     //this would point the next position of marked.
        System.out.println("--------"+charBuffer.get());

        //markAndReset test--------n
    }
    public void clearAndcompactTest(){
        CharBuffer charBuffer = CharBuffer.allocate(100);
        charBuffer.put("clearAndcompact Test");
        charBuffer.flip();//limit = position; position = 0;
        while (charBuffer.hasRemaining()){
            char c = charBuffer.get();  //pos has pos++;
            if(c=='A'){
                System.out.println("-----");
//                charBuffer.compact();//position(limit - position);   limit = capacity;   mark = -1
                charBuffer.clear();//position = 0;  limit = capacity;  mark = -1;
                charBuffer.put(" has update ");
                System.out.println("position:"+charBuffer.position());
                charBuffer.rewind();//仅　limit = 0;　　这里不用charBuffer.flip();　因为limit = position，当使用clear()
                while(charBuffer.hasRemaining()){
                    System.out.print(charBuffer.get());
                }
//                char[] cs = new char[24];
//                charBuffer.get(cs,0,24);
//                System.out.println(cs);

                // charBuffer.clear();  -----  has update act Test
                //charBuffer.compact(); ------ ndcompact Test has update
            }else {
                System.out.print(c);
            }
        }

    }

    public void writeFile() throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile("C:\\Users\\asnphtl\\Desktop\\testFile\\NIO_T1.txt","rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        fileChannel.write(ByteBuffer.wrap("SS".getBytes(),0,2));
        System.out.println(fileChannel.size());

    }

    /**
     * equals
     * 有相同的类型（byte、char、int等）;
     * Buffer中剩余的byte、char等的个数相等;
     * Buffer中所有剩余的byte、char等都相同。
     */
    public void equalsTest(){
        ByteBuffer b1 = ByteBuffer.allocate(10);
        ByteBuffer b2 = ByteBuffer.allocate(10);
        b1.put("abc".getBytes());
        b2.put("c".getBytes());
        b1.flip();
        b2.flip();
        while (b1.hasRemaining()){
            char c = (char) b1.get();
            if(c == 'b'){
                System.out.println(b1.equals(b2));//true   比较的是position and limit 之间的元素
            }
        }
    }

    /**
     * 第一个不相等的元素小于另一个Buffer中对应的元素。
     * 所有元素都相等，但第一个Buffer比另一个先耗尽(第一个Buffer的元素个数比另一个少)。
     */
    public void comparetoTest(){
        ByteBuffer b1 = ByteBuffer.allocate(10);
        ByteBuffer b2 = ByteBuffer.allocate(10);
        ByteBuffer b3 = ByteBuffer.allocate(10);
        b1.put("abc".getBytes());
        b2.put("bcd".getBytes());
        b3.put("abcd".getBytes());
        b1.flip();
        b2.flip();
        b3.flip();
        System.out.println(b1.compareTo(b2));//-1
        System.out.println(b1.compareTo(b3));//-1
    }

    /**
     * 将 channel 的数据分散写入到多个buffer中，规则是前一个buffer需要写满才进行下一个buffer 的写数据操作
     * 定义好的buffer放入数组，channel 依据放入的顺序写数据
     * @param path
     * @param model
     * @throws Exception
     */
    public void scatterTest(String path,String model) throws Exception{
        RandomAccessFile randomAccessFile = new RandomAccessFile(path,model);
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer b1 = ByteBuffer.allocate(3);
        ByteBuffer b2 = ByteBuffer.allocate(3);
        ByteBuffer[] buffers = new ByteBuffer[]{b1,b2};//放入数组
        long fileRead = fileChannel.read(buffers);
        while (fileRead != -1){
            b1.flip();
            b2.flip();
            while (b1.hasRemaining()){
                System.out.print((char) b1.get());
            }
            System.out.println();
            while (b2.hasRemaining()){
                System.out.print((char)b2.get());
            }
            b1.clear();
            b2.clear();
            fileRead = fileChannel.read(buffers);//清空（position = 0;limit = capacity;）buffer，读取后续数据
        }
        randomAccessFile.close();
    }

    /**
     * 将多个buffer 聚集到一个channel
     * @param path
     * @param model
     * @throws Exception
     */
    public void gatherTest(String path,String model)throws Exception{
        RandomAccessFile randomAccessFile = new RandomAccessFile(path,model);
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer b1 = ByteBuffer.allocate(3);
        ByteBuffer b2 = ByteBuffer.allocate(3);
        b1.put("efg".getBytes());
        b2.put("swt".getBytes());
        ByteBuffer[] buffers = new ByteBuffer[]{b1,b2};//放入数组
        b1.flip();
        b2.flip();
        fileChannel.write(buffers);
    }

    /**
     * transferTo　和　tranferFrom
     * 将字节从给定的可读取字节通道传输到目标通道的文件中
     * 相当于复制文件
     * @param srcPath
     * @param tagPath
     * @throws Exception
     */
    public void tranferTotTst(String srcPath,String tagPath)throws Exception{
        RandomAccessFile srcFile = new RandomAccessFile(srcPath,"rw");
        RandomAccessFile tagFile = new RandomAccessFile(tagPath,"rw");
        FileChannel srcChannel = srcFile.getChannel();
        FileChannel tagChannel = tagFile.getChannel();
        srcChannel.transferTo(0,4,tagChannel);
        srcFile.close();
        tagFile.close();
    }


    /**
     * selector
     * 实现一个线程监控多个 channel  就绪状态
     * 状态包括如下：
     * SelectionKey.OP_CONNECT：某个channel成功连接到另一个服务器
     * SelectionKey.OP_ACCEPT：一个server socket channel准备好接收新进入的连接
     * SelectionKey.OP_READ：一个有数据可读的通道
     * SelectionKey.OP_WRITE：等待写数据的通道
     *
     * @throws Exception
     */
    public void selectorTest()throws Exception{
        Selector selector = Selector.open();    //创建　selector
        SocketChannel channel = SocketChannel.open();   //channel 必须为非阻塞通道，FileChannel 不能切换非阻塞，不适用
        channel.configureBlocking(false);       //切换　channel 为非阻塞状态，
        SelectionKey key = channel.register(selector,SelectionKey.OP_READ); //将channel注册到selector上,并设置需要监听的状态
        while(true){
            //int readyChannels = selector.select();    //返回监听状态已经就绪的channel数,此时若无则处于等待阻塞状态直至有就绪channel
            int readyChannels = selector.selectNow();   //返回监听状态已经就绪的channel数,此时若无则立即返回0

            if(readyChannels==0){
                System.out.println("readyChannel count:"+readyChannels);
                continue;
            }
            Set selectKeys = selector.selectedKeys();   //返回已经监听到的就绪状态的 channel 对象
            Iterator keyIterator = selectKeys.iterator();
            while(keyIterator.hasNext()) {
                SelectionKey key1 = (SelectionKey) keyIterator.next();
                if(key.isAcceptable()) {
                    // a connection was accepted by a ServerSocketChannel.
                } else if (key.isConnectable()) {
                    // a connection was established with a remote server.
                } else if (key.isReadable()) {
                    // a channel is ready for reading
                } else if (key.isWritable()) {
                    // a channel is ready for writing
                }
                keyIterator.remove();   //Selector不会自己从已选择键集中移除SelectionKey实例。必须在处理完通道时自己移除。
                // 下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
            }
        }
    }

    @Test
    public void test()throws Exception{
        DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(9999));
        String newData = "newdvfdvs";

        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());
        buf.flip();

        int bytesSent = channel.send(buf, new InetSocketAddress("jenkov.com", 80));
        System.out.println(bytesSent);
    }

    /**
     * Java NIO中的SocketChannel是一个连接到TCP网络套接字的通道。
     * @throws Exception
     */

    public void socketTest()throws Exception{
        Socket s = new Socket(InetAddress.getByName("127.0.0.1"),10004);
        //从 channel 读数据进入　buffer
        SocketChannel socketChannel = SocketChannel.open(); //创建通道
        socketChannel.bind(new InetSocketAddress(10004));
        NIODemo.socketSendStr(s);
        ByteBuffer b1 = ByteBuffer.allocate(1024);
        int socketRead = socketChannel.read(b1);    // read()方法返回的int值表示读了多少字节进Buffer里。如果返回的是-1，表示已经读到了流的末尾（连接关闭了）。
        System.out.println("socketRead:"+socketRead);

        //从 buffer 写数据进入　channel
//        ByteBuffer b2 = ByteBuffer.allocate(1024);
//        b2.clear();
//        b2.put("hello1111".getBytes());
//        b2.flip();
//        while(b2.hasRemaining()){
//            socketChannel.write(b2);
//        }
    }

    /**
     * 可以通过以下2种方式创建SocketChannel：
     * 第一种：打开一个SocketChannel并连接到互联网上的某台服务器。
     *
     */
    public void socketChannelCreateTest1() throws Exception{

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);     //设置非阻塞
        socketChannel.connect(new InetSocketAddress("www.baidu.com",80));
        //调用 connect()建立连接，但是在成功连接之前可能就已经返回了
        //finishConnect() 确认连接是否成功建立

        while (true) {
            if (!socketChannel.finishConnect()) {
                System.out.println("连接还没建立，但是我可以做其他事情！");
            } else {
                while (socketChannel.finishConnect()) {
                    System.out.println("connect!");
                }
            }
        }
    }

    /**
     * 第二种： 一个新连接到达ServerSocketChannel时，会创建一个SocketChannel。
     * Java NIO中的 ServerSocketChannel 是一个可以监听新进来的TCP连接的通道
     * @throws Exception
     */

    public void socketChannelCreateTest2() throws Exception{
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9999));// 监听端口
        //accept()方法会一直阻塞到有新连接到达
        while(true){
            System.out.println("开始阻塞．．．");
            SocketChannel socketChannel = serverSocketChannel.accept();
            //do something with socketChannel...
        }
    }
    public void socketChannelCreateTest3() throws Exception{
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9999));// 监听端口
        serverSocketChannel.configureBlocking(false); //非阻塞状态

        System.out.println("开始监听．．．");
        while(true){
            SocketChannel socketChannel = serverSocketChannel.accept();
            if(socketChannel == null){
                System.out.println("没有连接进入，塞了，我先做其他事");
            }else{
                System.out.println("有连接");
            }
        }
    }

    /**
     * Java NIO中的DatagramChannel是一个能收发UDP包的通道。因为UDP是无连接的网络协议，所以不能像其它通道那样读取和写入。
     *
     */
    public void datagramChannelTest1()throws Exception{

        DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(10007));
        ByteBuffer b1 = ByteBuffer.allocate(1024);

        NIODemo.datagramSocketSendStr(new DatagramSocket());

        channel.receive(b1);    //receive()方法会将接收到的数据包内容复制到指定的Buffer. 如果Buffer容不下收到的数据，多出的数据将被丢弃。
        b1.flip();
        while (b1.hasRemaining()){
            System.out.println((char) b1.get());
        }

//        ByteBuffer buf = ByteBuffer.allocate(1024);
//        buf.clear();
//        buf.put("hello".getBytes());
//        buf.flip();
//        int bytesSent = channel.send(buf, new InetSocketAddress("jenkov.com", 80)); //成功与否都没有通知
//        System.out.println("发送的字节数："+bytesSent);// 事实上线程已经阻塞在建立连接那里
    }

    /**
     * 将DatagramChannel“连接”到网络中的特定地址的。由于UDP是无连接的，
     * 连接到特定地址并不会像TCP通道那样创建一个真正的连接。而是锁住DatagramChannel ，让其只能从特定地址收发数据。
     * @throws Exception
     */
    public void datagramChannelTest2()throws Exception{
        DatagramChannel channel = DatagramChannel.open();
        channel.socket().connect(new InetSocketAddress(9999));
        ByteBuffer b1 = ByteBuffer.allocate(1024);
        int readCount = channel.read(b1);

        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.clear();
        buf.put("hello".getBytes());
        buf.flip();
        int bytesSent = channel.write(buf);
        System.out.println("："+bytesSent);
    }

    public static void datagramSocketSendStr(DatagramSocket dgs) throws Exception{
        byte[] buf="hello".getBytes();
        DatagramPacket dgp=new DatagramPacket(buf, 0, buf.length,InetAddress.getByName("127.0.0.1"), 10007);
        dgs.send(dgp);
    }

    public static void socketSendStr(Socket s)throws Exception{
        ServerSocket ss = new ServerSocket(10003);
        s = ss.accept();
        OutputStream o = s.getOutputStream();
        o.write("hello".getBytes());
    }

    public static void main(String[] args) throws Exception {
        NIODemo demo1 = new NIODemo();
//        demo1.readFile("C:\\Users\\asnphtl\\Desktop\\testFile\\NIO_T1.txt","rw");
//        demo1.writeFile();
//        demo1.bufferTest();
//        demo1.markAndResetTest();
//        demo1.clearAndcompactTest();
//        demo1.equalsTest();
//        demo1.comparetoTest();
//        demo1.scatterTest("C:\\Users\\asnphtl\\Desktop\\testFile\\NIO_T1.txt","rw");
//        demo1.gatherTest("C:\\Users\\asnphtl\\Desktop\\testFile\\NIO_T1.txt","rw");
//        demo1.tranferTotTst("C:\\Users\\asnphtl\\Desktop\\testFile\\fromFile.txt","C:\\Users\\asnphtl\\Desktop\\testFile\\toFile.txt");
//        demo1.selectorTest();
        demo1.socketTest();
//        demo1.socketChannelCreateTest1();
//        demo1.socketChannelCreateTest2();
//        demo1.socketChannelCreateTest3();
//        demo1.datagramChannelTest1();
    }
}