import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private boolean done;
    private BufferedReader in;
    // for client to get the stream of the socket
    private PrintWriter out;


    @Override
    public void run() {
        try{
            Socket client=new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(),true);
            in=new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t=new Thread(inHandler);
            t.start();

            String inMsg;
            while((inMsg=in.readLine())!=null){
                System.out.println(inMsg);
            }


        }
        catch (IOException e){
    shutdown();
        }
    }
    public void shutdown(){
        done=true;
        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }

        }catch (IOException e){

        }
    }
    class InputHandler implements Runnable{
        @Override
        public void run() {
            try {
                BufferedReader inReader= new BufferedReader(new InputStreamReader(System.in));
                while(!done){
                    String msg= inReader.readLine();
                    if(msg.equals("/quit")){
                        inReader.close();
                        shutdown();
                    }
                    else{
                        out.println(msg);
                    }

                }

            }catch (IOException e){
                shutdown();


            }
        }


    }
    public static void main(String[] args) {
        Client client = new Client();
        client.run();

    }
}
