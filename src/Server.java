import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private boolean done=false;
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private ExecutorService pool;
    public Server(){
        connections=new ArrayList<>();
        done=false;
    }
    @Override
    public void run(){
        try {
            while(!done) {
                server = new ServerSocket(99);
                pool= Executors.newCachedThreadPool();
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        }
        catch(IOException e){

        }

    }
    public void broadcast(String msg){
        for(ConnectionHandler ch: connections){
            if(ch!=null){
                ch.sendMessage(msg);
            }
        }
    }
    public void shutdown(){
        done=true;
        try{
            if(!server.isClosed()){
                server.close();
            }
            for (ConnectionHandler ch : connections){
                ch.shutdown();

            }
        }catch(IOException e ){

        }


    }
    class ConnectionHandler implements Runnable{
        private String nickname;
        private Socket client;
        private BufferedReader in;
        // for client to get the stream of the socket
        private PrintWriter out;
        //if we write sth to client we use out
        public ConnectionHandler(Socket client){
            this.client=client;

        }
        @Override
        public void run(){
            try{
                out=new PrintWriter(client.getOutputStream(), true);
                in=new BufferedReader(new InputStreamReader(client.getInputStream()));
                //out.println("Hello");
                //I can say hellp to client
                // in.readLine();
                // I can read the client
                out.println("Please enter an nickname");
                nickname=in.readLine();
                System.out.println(nickname+" connected!");
                broadcast(nickname+ " joined the chat!!");
                String message;
                while((message=in.readLine())!=null){
                    if (message.startsWith("/nick")) {
                        String[] messageSplit = message.split(" ", 2);
                        if(messageSplit.length==2){
                            broadcast(nickname+ " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname+ " renamed themselves to " + messageSplit[1]);
                            nickname=messageSplit[1];
                            out.println("Succesfully changed nickname to "+ nickname);
                        }
                        else{
                            out.println("No nickname provided");
                        }

                    }  else if(message.startsWith("/quit")){
                        broadcast(nickname + "left the chat!");
                        shutdown();
                        //quit
                    }
                    else {
                        broadcast(nickname+ ": " + message);
                    }
                }

            }catch(IOException e){
                shutdown();

            }
        }
        public void sendMessage(String mesg){
            out.println(mesg);
        }
        public void shutdown(){
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e){
                //,gnlore
            }
        }




    }

    public static void main(String[] args) {
        Server server= new Server();
        server.run();
    }
}
