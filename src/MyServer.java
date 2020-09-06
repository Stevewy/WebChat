import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author WangYao
 * @date 2020/9/3
 * @function
 */
public class MyServer {

    public void init() throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel socket = ServerSocketChannel.open();
        socket.bind(new InetSocketAddress(8888));
        socket.configureBlocking(false);
        socket.register(selector, SelectionKey.OP_ACCEPT);

        while(true){
            int readyChannels = selector.select();
            if(readyChannels == 0) continue;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while(keyIterator.hasNext()){
                SelectionKey sk = keyIterator.next();
                keyIterator.remove();
                dealWithSelectionKey(selector, socket, sk);
            }
        }
    }

    public void dealWithSelectionKey(Selector selector,ServerSocketChannel socket,SelectionKey sk) throws IOException {

    }

    public static void main(String[] args) throws IOException {
        new MyServer().init();
    }
}
