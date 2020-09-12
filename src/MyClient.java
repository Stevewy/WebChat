import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author WangYao
 * @date 2020/9/3
 * @function
 */
public class MyClient {
    private String name = "";
    private Charset charset = Charset.forName("UTF-8");
    private static String USER_EXIST = "system message: user exist, please change your name";
    private static String USER_CONTENT_SPILIT = "#@#";

    public void init() throws IOException{
        Selector selector = Selector.open();

        SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1",8888));
        socket.configureBlocking(false);
        socket.register(selector, SelectionKey.OP_READ);

        new Thread(new ClientThread(selector)).start(); //开线程发送

        Scanner in = new Scanner(System.in);
        while(in.hasNextLine())            //读取控制台信息
        {
            String line = in.nextLine();
            if("".equals(line)) continue; //不允许发空消息
            if("".equals(name)) {         //没用名字
                name = line;
                line = name+USER_CONTENT_SPILIT;
            } else {
                line = name + USER_CONTENT_SPILIT + line;
            }
            socket.write(charset.encode(line));//sc既能写也能读，这边是写

        }
    }

    private class ClientThread implements Runnable{
        private Selector selector;

        public ClientThread(Selector selector) {
            this.selector = selector;
        }

        public void run()
        {
            try
            {
                //开一个线程来读
                while(true) {
                    int readyChannels = selector.select();
                    if(readyChannels == 0) continue;
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                    while(keyIterator.hasNext()) {
                        SelectionKey sk = keyIterator.next();
                        keyIterator.remove();
                        dealWithSelectionKey(sk);
                    }
                }
            }
            catch (IOException io)
            {
                throw new RuntimeException(io);
            }
        }

        private void dealWithSelectionKey(SelectionKey sk) throws IOException {
            if(sk.isReadable())
            {
                SocketChannel sc = (SocketChannel)sk.channel();

                ByteBuffer buff = ByteBuffer.allocate(1024);
                String content = "";
                while(sc.read(buff) > 0)
                {
                    buff.flip();
                    content += charset.decode(buff);
                }

                if(USER_EXIST.equals(content)) {
                    name = "";
                }
                System.out.println(content);
                sk.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new MyClient().init();
    }
}
