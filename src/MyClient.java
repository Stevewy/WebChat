import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * @author WangYao
 * @date 2020/9/3
 * @function
 */
public class MyClient {
    private String name = "";
    private Charset charset = Charset.forName("UTF-8");
    private static String USER_EXIST = "system message: user exist, please change a name";
    private static String USER_CONTENT_SPILIT = "#@#";

    public void init() throws IOException{
        Selector selector = Selector.open();

        SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1",8888));
        socket.configureBlocking(false);
        socket.register(selector, SelectionKey.OP_ACCEPT);

        new Thread(new ClientThread()).start(); //开线程发送

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

        @Override
        public void run() {

        }
    }

    public static void main(String[] args) throws IOException {
        new MyClient().init();
    }
}
