import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author WangYao
 * @date 2020/9/3
 * @function
 */
public class MyServer {

    private final Charset charset = StandardCharsets.UTF_8;

    private final static String USER_EXIST = "system message: user exist, please change your name";

    private final static String USER_CONTENT_SPILIT = "#@#";

    private final static HashSet<String> users = new HashSet<>();

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

    /**
     * 处理选择到的通道
     * @param selector 选择器
     * @param server   通道
     * @param sk       选择钥匙
     * @throws IOException
     */
    public void dealWithSelectionKey(Selector selector, ServerSocketChannel server, SelectionKey sk) throws IOException {
        if(sk.isAcceptable()) //第一次接受,选择姓名
        {
            SocketChannel sc = server.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);

            sk.interestOps(SelectionKey.OP_ACCEPT);
            System.out.println("Server is listening from client :" + sc.getRemoteAddress());
            sc.write(charset.encode("Please input your name."));
        }
        //如果可读
        if(sk.isReadable())
        {
            SocketChannel sc = (SocketChannel)sk.channel();
            ByteBuffer buff = ByteBuffer.allocate(1024);
            StringBuilder content = new StringBuilder();
            try
            {
                while(sc.read(buff) > 0)
                {
                    buff.flip();
                    content.append(charset.decode(buff));

                }
                System.out.println("Server is listening from client " + sc.getRemoteAddress() + " data rev is: " + content);
                sk.interestOps(SelectionKey.OP_READ);
            }
            catch (IOException io) //出现io异常,关闭选择
            {
                sk.cancel();
                if(sk.channel() != null)
                {
                    sk.channel().close();
                }
            }

            if(content.length() > 0) //读到的信息非空
            {
                String[] arrayContent = content.toString().split(USER_CONTENT_SPILIT);
                if(arrayContent != null && arrayContent.length ==1) {
                    String name = arrayContent[0];
                    //得到姓名
                    if(users.contains(name)) {
                        sc.write(charset.encode(USER_EXIST));
                    } else {
                        users.add(name);
                        int num = OnlineNum(selector); //计算总人数的一个方法
                        String message = "welcome "+name+" to chat room! Online numbers:"+num;
                        BroadCast(selector, null, message);
                    }
                }
                else if(arrayContent != null && arrayContent.length >1){
                    String name = arrayContent[0];
                    String message = content.substring(name.length()+USER_CONTENT_SPILIT.length()); //去掉名字和分隔符
                    message = name + " 说: " + message;
                    if(users.contains(name)){
                        BroadCast(selector, sc, message);
                    }

                }
            }
        }
    }

    //计算总人数
    public static int OnlineNum(Selector selector) {
        int res = 0;
        for(SelectionKey key : selector.keys())
        {
            Channel targetChannel = key.channel();
            if(targetChannel instanceof SocketChannel)
                res++;

        }
        return res;
    }

    /**
     * 给所有非except的用户发送消息
     * @param selector 选择器
     * @param except   不给该对象发送消息
     * @param content  消息
     * @throws IOException
     */
    public void BroadCast(Selector selector, SocketChannel except, String content) throws IOException {
        for(SelectionKey key : selector.keys())
        {
            Channel targetchannel = key.channel();
            //如果except不为空，不回发给发送此内容的客户端
            if(targetchannel instanceof SocketChannel && targetchannel!=except)
            {
                SocketChannel dest = (SocketChannel)targetchannel;
                dest.write(charset.encode(content));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new MyServer().init();
    }
}
